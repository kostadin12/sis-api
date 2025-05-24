package com.kostadin.sis.userproject;

import com.kostadin.sis.absence.AbsenceRepository;
import com.kostadin.sis.common.exception.ProjectBadRequestException;
import com.kostadin.sis.common.exception.ProjectNotFoundException;
import com.kostadin.sis.common.exception.UserNotFoundException;
import com.kostadin.sis.exception.custom.CustomResponseStatusException;
import com.kostadin.sis.label.LabelRepository;
import com.kostadin.sis.label.model.Label;
import com.kostadin.sis.label.model.LabelScope;
import com.kostadin.sis.label.model.response.LabelDTO;
import com.kostadin.sis.mapper.ProjectMapper;
import com.kostadin.sis.mapper.ProjectMemberMapper;
import com.kostadin.sis.project.ProjectRepository;
import com.kostadin.sis.project.model.Project;
import com.kostadin.sis.project.model.request.GetUsersAbsencesByLabelsCommand;
import com.kostadin.sis.project.model.request.RemoveUserFromProjectCommand;
import com.kostadin.sis.project.model.response.ProjectName;
import com.kostadin.sis.user.UserRepository;
import com.kostadin.sis.user.model.User;
import com.kostadin.sis.userproject.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.kostadin.sis.exception.ErrorCode.USER_NOT_FOUND;
import static com.kostadin.sis.user.model.UserRole.*;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.logging.log4j.util.Strings.isNotBlank;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProjectMemberService {
    private final UserProjectRepository userProjectRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final LabelRepository labelRepository;
    private final AbsenceRepository absenceRepository;
    private final ProjectMemberMapper projectMemberMapper;
    private final ProjectMapper projectMapper;

    /**
     * Loads information about the users that collaborate in a given project.
     *
     * @param projectId to load its members
     * @return the given project members
     * @implNote User labels are filtered to correspond with the labels included on the Project.
     * @throws ProjectNotFoundException  if a project with the given passed id is non-existent.
     * @see ProjectMember
     */
    public List<ProjectMember> loadProjectMembers(long projectId) {
        log.info("Receiving a list of all participants in project {}", projectId);

        if (!projectRepository.existsById(projectId)) {
            throw new ProjectNotFoundException(projectId);
        }

        return userProjectRepository.findAllById_ProjectId(projectId).stream()
                .map(projectMemberMapper::toProjectMember)
                .toList();
    }

    /**
     * Add Users in a Project.
     * <p>
     * Adds a list of users to a project by given id and list of employee numbers.
     * @param projectId       id of Project we want to add users to.
     * @param employeeNumbers List of Strings (employee numbers) that are going to be added to the Project.
     * @return Returns a ProjectMember user list with all newly added project members.
     * @see ProjectMember
     * @throws ProjectNotFoundException if project with given ID is not found.
     * @throws ProjectBadRequestException if project already contains one of the given users.
     */
    public List<ProjectMember> addUsers(long projectId, List<String> employeeNumbers) {
        log.info("Adding {} participants to project {}", employeeNumbers.size(), projectId);

        var project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        List<User> users = new ArrayList<>(userRepository
                .findUsersIn(employeeNumbers
                        .stream()
                        .map(String::toUpperCase)
                        .toList())
                .stream().toList());

        var foundInDbEmployeeNumbers = users.stream().map(User::getEmployeeNumber).toList();

        addNewlyAddedUserLabels(project, users);
        var savedUserProjects = createUserProjectsIfUsersNotContained(project, users);
        userProjectRepository.saveAll(savedUserProjects);

        return projectMemberMapper.toProjectMember(savedUserProjects);
    }

    private List<UserProject> createUserProjectsIfUsersNotContained(Project project, List<User> users) {
        var response = new ArrayList<UserProject>();

        for (User user : users) {
            if (userProjectRepository.existsById(new UserProjectId(user.getId(), project.getId()))) {
                throw new ProjectBadRequestException("User " + user.getEmployeeNumber() + " already included in project.");
            }
            response.add(new UserProject(new UserProjectId(user.getId(), project.getId()),user, project, 100));
        }

        return response;
    }

    public void addNewlyAddedUserLabels(Project project, List<User> users) {
        for (User user : users) {
            user.getLabels()
                    .stream()
                    .filter(label -> label.getScope() == LabelScope.SYSTEM && !project.getLabels().contains(label))
                    .forEach(project::addLabel);
        }
        projectRepository.save(project);
    }

    /**
     * Removes a User from a Project.
     * <p>
     * Removes a project member from project.
     * Checks if a new beneficiary for project owner is selected (if removed user is project owner and beneficiary matches the criteria)
     * Deletes the UserProject entity from the DB.
     * @param projectId ID of project we are removing the user from
     * @return Project members left in the project.
     * @RequestBody Removed user's employee number.
     */
    public List<ProjectMember> removeUser(long projectId, RemoveUserFromProjectCommand removeUserFromProjectCommand) {
        log.info("Removing user {} from project {}", removeUserFromProjectCommand.getEmployeeId(), projectId);

        var userProject = userProjectRepository.findByUserEmployeeNumberAndProjectId(removeUserFromProjectCommand.getEmployeeId(), projectId)
                .orElseThrow(() -> new CustomResponseStatusException(NOT_FOUND, USER_NOT_FOUND.getErrorCode(), USER_NOT_FOUND.getReason(), "User " + removeUserFromProjectCommand.getEmployeeId() + " not found in project."));

        checkIfNewProjectOwnerIsSelected(userProject, removeUserFromProjectCommand);

        userProjectRepository.delete(userProject);
        log.info("Successfully removed user from project.");

        checkIfSystemsLabelsCanBeDeleted(Set.of(userProject));

        return projectRepository.findByIdWithUsers(projectId).orElseThrow(() -> new ProjectNotFoundException(projectId))
                .getUsers().stream().map(projectMemberMapper::toProjectMember).toList();
    }

    private void checkIfNewProjectOwnerIsSelected(UserProject userProject, RemoveUserFromProjectCommand removeUserFromProjectCommand) {
        var employeeNumber ="";
        var roles ="";

        Project project = userProject.getProject();

        if (isNotBlank(removeUserFromProjectCommand.getBeneficiaryManagerEmployeeNumber())) {
            var isRemovedUserNotTheProjectOwner = !Objects.equals(userProject.getUser().getEmployeeNumber(), project.getProjectOwner().getEmployeeNumber());
            if (isRemovedUserNotTheProjectOwner) {
                throw new ProjectBadRequestException("If beneficiary employee number is passed, removed user must be a project owner.");
            }
            var isBeneficiaryProjectOwnerTheSameAsTheRemovedUser = Objects.equals(removeUserFromProjectCommand.getEmployeeId(), removeUserFromProjectCommand.getBeneficiaryManagerEmployeeNumber());
            if (isBeneficiaryProjectOwnerTheSameAsTheRemovedUser) {
                throw new ProjectBadRequestException("Removed project owner and beneficiary cannot be the same person.");
            }
            var isLoggedUserNotAdminOrProjectOwner = !roles.contains("ROLE_sis-admin") && !employeeNumber.toUpperCase().equals(userProject.getUser().getEmployeeNumber());
            if (isLoggedUserNotAdminOrProjectOwner) {
                throw new ProjectBadRequestException("Only project owner and administrators can remove this manager from the project.");
            }

            var beneficiary = userRepository.findBeneficiaryByEmployeeNumberIgnoreCase(removeUserFromProjectCommand.getBeneficiaryManagerEmployeeNumber(), project.getId())
                    .orElseThrow(() -> new UserNotFoundException("User with employee number " + removeUserFromProjectCommand.getBeneficiaryManagerEmployeeNumber() + " not found."));

            if (!List.of(ROLE_ADMIN, ROLE_MANAGER, ROLE_REPORT).contains(userProject.getUser().getRole())) {
                throw new ProjectBadRequestException("Beneficiary must be a manager or an administrator.");
            }

            project.setProjectOwner(beneficiary);
        } else {
            if (Objects.equals(userProject.getProject().getProjectOwner().getEmployeeNumber(), userProject.getUser().getEmployeeNumber())) {
                throw new ProjectBadRequestException("Project owner can't be removed without choosing a beneficiary.");
            }
        }
    }

    public void checkIfSystemsLabelsCanBeDeleted(Set<UserProject> userProjects) {
        for (UserProject userProject : userProjects) {
            Project project = userProject.getProject();
            var userLabelNames = userProject.getUser().getLabels().stream().map(Label::getName).toList();
            var ignoredUserEmployeeNumber = userProject.getUser().getEmployeeNumber();

            project.getLabels().removeIf(label -> userLabelNames.contains(label.getName()) && !labelRepository.isLabelFoundInAnyUsersInProject(project.getId(), label.getName(), ignoredUserEmployeeNumber));
            projectRepository.save(project);
        }
    }

    /**
     * Receives Users from a Project, with their Labels for the current Project, and their Absences filtered by the requested start and end date parameters.
     * <p>
     * @throws ProjectNotFoundException if a Project with the given ID doesn't exist.
     * Filters all Absences from the DB that are between the passed start/end date parameters.
     * Collects all project members and filters their Absences to correspond with the start/end date parameters,
     * also filters the labels to correspond with the labels that the Project holds.
     * Finally, if the request parameter labels is not empty, it filters the users by them.
     *
     * @return Returns ProjectMemberWithAbsences body, including their name, employeeNumber, labels and absences.
     * @see ProjectMemberWithAbsences
     * @params Start and end date for the period of time we want the Absences from, Project ID and List of label names to sort from.
     */
    public List<ProjectMemberWithAbsences> getUsersAbsencesByLabels(GetUsersAbsencesByLabelsCommand getUsersAbsencesByLabelsCommand) {
        log.info("Returning users in Project {} with absences between {} and {}.", getUsersAbsencesByLabelsCommand.getProjectId(), getUsersAbsencesByLabelsCommand.getStartDate(), getUsersAbsencesByLabelsCommand.getEndDate());
        var userProjects  = userProjectRepository
                .findAllById_ProjectId(getUsersAbsencesByLabelsCommand.getProjectId());

        if (isEmpty(userProjects)) {
            throw new ProjectNotFoundException(getUsersAbsencesByLabelsCommand.getProjectId());
        }

        var absences = absenceRepository.findAbsencesBetween(getUsersAbsencesByLabelsCommand.getStartDate(), getUsersAbsencesByLabelsCommand.getEndDate());

        var projectMembersWithAbsences = userProjects.stream().map(up -> projectMemberMapper.toProjectMemberWithAbsences(up, absences)).toList();

        if (isNotEmpty(getUsersAbsencesByLabelsCommand.getLabels())) {
            log.info("Filtering users by labels: {}", getUsersAbsencesByLabelsCommand.getLabels());
            projectMembersWithAbsences = projectMembersWithAbsences.stream()
                    .filter(u -> u.labels().stream().map(LabelDTO::getName).anyMatch(getUsersAbsencesByLabelsCommand.getLabels()::contains))
                    .toList();
        }

        return projectMembersWithAbsences;
    }

    public ProjectMemberWithCapacities getProjectMemberWithCapacities(String employeeNumber) {
        log.info("Receiving project capacities for user {}", employeeNumber);
        var userProjects = userProjectRepository.findAllByUserEmployeeNumber(employeeNumber);
        if (isEmpty(userProjects)) {
            return new ProjectMemberWithCapacities(employeeNumber, List.of());
        }

        return projectMemberMapper.toProjectMemberWithCapacities(userProjects, employeeNumber);
    }

    /**
     * Receives all Projects a User is included in.
     * <p>
     * Throws an exception if a User with passed employee number is non-existent.
     * @param employeeId User employee number
     * @return Returns User's projects mapped to a List of {@link ProjectName} (including only Project name and ID)
     */
    public List<ProjectName> getProjectsByUserBB(String employeeId) {
        log.info("Receiving a list of all projects for user {}.", employeeId);

        var user = userRepository
                .findByEmployeeNumberIgnoreCase(employeeId)
                .orElseThrow(() -> new UserNotFoundException("User with employee id " + employeeId + " not found."));

        return projectRepository.findProjectsByUserBB(user.getEmployeeNumber())
                .stream().map(projectMapper::toProjectName)
                .toList();
    }
}
