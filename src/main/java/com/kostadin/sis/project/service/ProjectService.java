package com.kostadin.sis.project.service;

import com.kostadin.sis.auth.security.services.UserDetailsImpl;
import com.kostadin.sis.common.exception.ProjectBadRequestException;
import com.kostadin.sis.common.exception.ProjectNotFoundException;
import com.kostadin.sis.common.exception.UserNotFoundException;
import com.kostadin.sis.label.LabelRepository;
import com.kostadin.sis.label.model.Label;
import com.kostadin.sis.label.model.LabelScope;
import com.kostadin.sis.mapper.ProjectMapper;
import com.kostadin.sis.project.ProjectRepository;
import com.kostadin.sis.project.model.Project;
import com.kostadin.sis.project.model.ProjectStatus;
import com.kostadin.sis.project.model.request.SaveProjectCommand;
import com.kostadin.sis.project.model.request.UpdateProjectCommand;
import com.kostadin.sis.project.model.response.ProjectDTO;
import com.kostadin.sis.project.model.response.ProjectName;
import com.kostadin.sis.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.kostadin.sis.label.model.LabelScope.PROJECT;
import static com.kostadin.sis.label.model.LabelScope.SYSTEM;
import static com.kostadin.sis.user.model.UserRole.*;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.logging.log4j.util.Strings.isNotBlank;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final LabelRepository labelRepository;
    private final ProjectMapper projectMapper;

    /**
     * Receiving all the projects from the DB.
     *
     * @return Return value is mapped to a List of {@link ProjectName}, containing only Project name and id.
     */
    @Transactional(readOnly = true)
    public List<ProjectName> getProjects() {
        log.info("Receiving all projects.");
        return projectRepository.findAll().stream().map(projectMapper::toProjectName).toList();
    }

    /**
     * Receives a Project from the DB by id.
     * <p>
     * Throws an exception if such project doesn't exist.
     * In the method body, the project users are filtered so their labels correspond with the labels the project holds.
     *
     * @param id Project id
     * @return Return value is mapped to {@link ProjectDTO}.
     */
    @Transactional(readOnly = true)
    public ProjectDTO getProjectById(long id) {
        log.info("Receiving a project with id = {}", id);

        var project = projectRepository
                .findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));

        log.info("Received project {}", project);
        return projectMapper.toDto(project);
    }

    /**
     * Autocomplete feature used for finding projects by name.
     *
     * @param input Project names in the DB are filtered by this input.
     * @return Found projects are mapped to a List of {@link ProjectName}, containing only the ID and Name fields.
     */
    public List<ProjectName> findProjects(String input) {
        return projectRepository.findAllByNameLike(input).stream().map(projectMapper::toProjectName).toList();
    }

    /**
     * Saving a new Project to the DB.
     * Method sets the Project status to ACTIVE.
     *
     * @param saveProjectCommand Body containing only the name, description, startDate.
     * @return Returns saved Project mapped to {@link ProjectDTO}.
     */
    public ProjectDTO saveProject(SaveProjectCommand saveProjectCommand) {
        log.info("Saving a new project.");
        var project = projectMapper.toEntity(saveProjectCommand
                .setProjectStatus(ProjectStatus.ACTIVE));

        var projectOwner = userRepository.findByEmployeeNumberIgnoreCase(saveProjectCommand.getInitialProjectOwner())
                .orElseThrow(() -> new UserNotFoundException("User " + saveProjectCommand.getInitialProjectOwner() + " not found."));

        project.setProjectOwner(projectOwner);
        project.addUsers(List.of(projectOwner));
        projectOwner.getLabels()
                .stream()
                .filter(label -> label.getScope() == LabelScope.SYSTEM)
                .filter(label -> !project.getLabels().contains(label))
                .forEach(project::addLabel);

        log.info("Saved project {}", projectMapper.toDto(project));
        return projectMapper.toDto(projectRepository.save(project));
    }

    /**
     * Deletes a Project from the DB. Throws exception if such Project is non-existent.
     *
     * @param id of Project to be deleted
     */
    public void deleteProject(long id, String projectOwnerEmployeeNumber) {
        log.info("Deleting project with id = {}", id);
        var project = projectRepository.findByIdAndProjectOwnerEmployeeNumberWithLabels(id, projectOwnerEmployeeNumber)
                .orElseThrow(() -> new ProjectNotFoundException(id));

        if (isNotEmpty(project.getLabels())) {
            List<Label> deletedProjectLabels = new ArrayList<>();
            project.getLabels().forEach(label -> {
                if (label.getScope() == PROJECT) {
                    deletedProjectLabels.add(label);
                }
            });
            if (isNotEmpty(deletedProjectLabels)) {
                labelRepository.deleteAll(deletedProjectLabels);
            }
        }

        projectRepository.delete(project);
    }

    /**
     * Updates an existing Project.
     * Throws an exception if a project with the passed id is non-existent.
     *
     * @param id                   ID of Project to be updated.
     * @param updateProjectCommand body with the updated Project fields.
     * @return Returns updated Project mapped to {@link ProjectDTO}.
     */
    public ProjectDTO updateProject(long id, String projectOwnerEmployeeNumber, UpdateProjectCommand updateProjectCommand) {
        log.info("Updating project with id = {}", id);

        if (updateProjectCommand.isDateRangeInvalid()) {
            throw new ProjectBadRequestException("Project start date cannot be after the end date.");
        }

        var project = projectRepository
                .findByIdAndProjectOwnerEmployeeNumberWithLabels(id, projectOwnerEmployeeNumber)
                .orElseThrow(() -> new ProjectNotFoundException(id));

        if (project.isCapacityMode() != updateProjectCommand.isCapacityMode()) {
            log.info("Changing capacity mode. Old value: {}, New value: {}", project.isCapacityMode(), updateProjectCommand.isCapacityMode());
        }

        project
                .setName(updateProjectCommand.getName())
                .setDescription(updateProjectCommand.getDescription())
                .setStartDate(updateProjectCommand.getStartDate())
                .setEndDate(updateProjectCommand.getEndDate())
                .setProjectStatus(updateProjectCommand.getProjectStatus())
                .setCapacityMode(updateProjectCommand.isCapacityMode());

        project = checkForUpdatedLabels(project, updateProjectCommand);

        project = checkForUpdatedOwner(project, updateProjectCommand);

        return projectMapper.toDto(projectRepository.save(project));
    }

    /**
     * Checks if the UpdatedProjectCommand contains a beneficiary and if it does, it sets him as the new project owner.
     *
     * @param project              Updated project
     * @param updateProjectCommand Request body containing the new project owner's employee number
     * @return Returns the updated Project.
     */
    private Project checkForUpdatedOwner(Project project, UpdateProjectCommand updateProjectCommand) {
        if (isNotBlank(updateProjectCommand.getNewBeneficiary())) {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();

            String employeeNumber = currentUser.getEmployeeNumber();
            List<String> roles = currentUser.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            boolean isAdmin = roles.contains("ROLE_ADMIN");
            boolean isCurrentProjectOwner = employeeNumber.equals(project.getProjectOwner().getEmployeeNumber());

            if (!isAdmin && !isCurrentProjectOwner) {
                throw new ProjectBadRequestException("Only ex-project owner and administrators can change the project owner.");
            }

            var beneficiary = userRepository.findBeneficiaryByEmployeeNumberIgnoreCase(
                    updateProjectCommand.getNewBeneficiary(), project.getId())
                    .orElseThrow(() -> new UserNotFoundException("User not found."));

            if (!List.of(ROLE_ADMIN, ROLE_MANAGER, ROLE_REPORT).contains(beneficiary.getRole())) {
                throw new ProjectBadRequestException("New project owner must be a manager/admin.");
            }

            return project.setProjectOwner(beneficiary);
        }
        return project;
    }

    /**
     * Checks if labels could be updated when updating a project. Labels passed in the UpdateProjectCommand should contain all the updated labels.
     * If project doesn't contain any of them, the label(s) would be created and added to the project's label collection.
     * But if the project contains labels that are not included in the UpdateProjectCommand, they would be removed and deleted from the DB.
     *
     * @param project              Updated project
     * @param updateProjectCommand Request body with updated values, only the project labels List is used in the method.
     * @return Returns an updated project with the new labels.
     */

    private Project checkForUpdatedLabels(Project project, UpdateProjectCommand updateProjectCommand) {
        if (updateProjectCommand.getProjectLabels() != null) {
            log.info("Updating labels for project " + project.getName() + ".");
            var newProjectLabels = updateProjectCommand.getProjectLabels();

            var labelsAlreadyInProject = labelRepository.findProjectLabelsIn(newProjectLabels, project.getId())
                    .stream().map(Label::getName).toList();

            var labelsToBeDeleted = project.getLabels().stream().filter(label -> !newProjectLabels.contains(label.getName()) && label.getScope() != SYSTEM).toList();
            var labelsToBeAdded = newProjectLabels.stream().filter(label -> !labelsAlreadyInProject.contains(label)).toList();

            if (isNotEmpty(labelsToBeDeleted)) {
                project.removeLabels(labelsToBeDeleted);
                labelRepository.deleteAll(labelsToBeDeleted);
                log.info("Deleted " + labelsToBeDeleted.size() + " from project " + project.getName() + ".\nLabels deleted: " + labelsToBeDeleted.stream().map(Label::getName).toList());
            }

            if (isNotEmpty(labelsToBeAdded)) {
                List<Label> newLabels = new ArrayList<>();
                labelsToBeAdded.forEach(newLabelName -> {
                    if (labelRepository.existsSystemLabelWithName(newLabelName)) {
                        log.info("Label " + newLabelName + " not added. It is already created as a SYSTEM label.");
                    } else {
                        newLabels.add(new Label()
                                .setName(newLabelName)
                                .setScope(PROJECT));
                    }
                });
                var newLabelsSaved = labelRepository.saveAll(newLabels);
                log.info("Saved " + newLabels.size() + " new labels to project " + project.getName() + ".\nAdded labels: " + newLabels.stream().map(Label::getName).toList());

                return project.addProjectLabels(newLabelsSaved);
            }
        }
        return project;
    }
}
