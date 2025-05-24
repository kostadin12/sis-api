package com.kostadin.sis.user;

import com.kostadin.sis.absence.AbsenceRepository;
import com.kostadin.sis.absence.model.response.AbsenceDTO;
import com.kostadin.sis.common.exception.*;
import com.kostadin.sis.label.LabelRepository;
import com.kostadin.sis.label.model.Label;
import com.kostadin.sis.label.model.LabelScope;
import com.kostadin.sis.mapper.AbsenceMapper;
import com.kostadin.sis.mapper.UserMapper;
import com.kostadin.sis.project.ProjectRepository;
import com.kostadin.sis.project.model.Project;
import com.kostadin.sis.subscriptions.SubscriptionRepository;
import com.kostadin.sis.user.model.User;
import com.kostadin.sis.user.model.request.*;
import com.kostadin.sis.user.model.response.UserDTO;
import com.kostadin.sis.user.model.response.UserNameEmployeeNumber;
import com.kostadin.sis.user.model.response.UserWithSystemLabelsAndCompany;
import com.kostadin.sis.userproject.ProjectMemberService;
import com.kostadin.sis.userproject.UserProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.collections4.MapUtils.isEmpty;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final ProjectMemberService projectMemberService;
    private final UserRepository userRepository;
    private final AbsenceRepository absenceRepository;
    private final ProjectRepository projectRepository;
    private final LabelRepository labelRepository;
    private final UserProjectRepository userProjectRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserMapper userMapper;
    private final AbsenceMapper absenceMapper;

    /**
     * Saves a new User to the DB.
     * <p>
     * It streams the body's labels included as Strings and searches for them in the DB.
     * For those labels that are found, they are added to the newly created User.
     * @param saveUserCommand Body with valid User fields, including a list of labels as Strings.
     * @return Returns the saved User mapped to a {@link UserDTO}.
     */
    public UserDTO saveUser(SaveUserCommand saveUserCommand) {
        log.info("Saving new user {}",saveUserCommand);

        var user = userMapper.toEntity(saveUserCommand);

        if (isNotEmpty(saveUserCommand.getLabels())){
            var labels = labelRepository.findSystemLabelsIn(saveUserCommand.getLabels());

            user
                    .setLabels(labels);

            var savedLabels = labels.stream().map(Label::getId).toList();
            var labelsNotFound = saveUserCommand.getLabels().stream().filter(l -> !savedLabels.contains(l)).toList();

            if (isNotEmpty(labelsNotFound)){
                log.info("Some of the saved labels were not found and added to User {}. Label IDs: {}", user.getEmployeeNumber(), labelsNotFound);
            }
        }

        var savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    /**
     * Receives all Users from the DB.
     * @return List of Users mapped to {@link UserDTO}.
     */
    public List<UserNameEmployeeNumber> getUsers() {
        log.info("Receiving list of all users from the DB.");
        var users = userMapper.toResponseBody(userRepository.findAll());
        if (users.isEmpty()){
            log.warn("Receiving an empty user list.");
        }
        return users;
    }

    /**
     * Receives a specific User from the database by his employee number.
     * @param employeeId Employee number of the wanted User.
     * @return Returns found User mapped to {@link UserDTO}.
     */
    public UserDTO getUser(String employeeId) {
        log.info("Receiving a user with employee number {}",employeeId);

        var user = userRepository.findByEmployeeNumberIgnoreCase(employeeId)
                .orElseThrow(() -> new UserNotFoundException("User with employee id " + employeeId + " not found."));

        return userMapper.toDto(user);
    }

    /**
     * Updates an existing User.
     * <p>
     *     If the given {@link UpdateUserCommand labels} is empty or <code>null</code>, user's labels will be deleted.
     *
     * @param employeeId Employee number of the User up to be updated.
     * @param updateUserCommand Body with the updated fields, including Labels as Strings.
     * @return  the updated User mapped to {@link UserDTO}, never <code>null</code>.
     * @see UpdateUserCommand
     */
    public UserDTO updateUser(String employeeId, UpdateUserCommand updateUserCommand) {
        log.info("Updating user with employee number {}", employeeId);

        var user = userRepository
                .findByEmployeeNumberIgnoreCase(employeeId)
                .orElseThrow(() -> new UserNotFoundException("User with employee id " + employeeId + " not found."));

        user
                .setFirstName(updateUserCommand.getFirstName())
                .setLastName(updateUserCommand.getLastName())
                .setEmail(updateUserCommand.getEmail() != null ? updateUserCommand.getEmail() : user.getEmail())
                .setSecondaryEmail(updateUserCommand.getSecondaryEmail())
                .setPhone(updateUserCommand.getPhone() != null ? updateUserCommand.getPhone() : user.getPhone())
                .setSecondaryPhone(updateUserCommand.getSecondaryPhone())
                .setColor(updateUserCommand.getColor() != null ? updateUserCommand.getColor() : user.getColor());

        if (updateUserCommand.getRole() != null) {
            try {
                user.setRole(updateUserCommand.getRole());
                log.info("Updated role for user {} to {}", employeeId, updateUserCommand.getRole());
            } catch (IllegalArgumentException e) {
                throw new UserBadRequestException("Invalid role: " + updateUserCommand.getRole());
            }
        }

        if(isNotEmpty(updateUserCommand.getLabels())){
            var labels = labelRepository
                    .findSystemLabelsIn(updateUserCommand.getLabels());

            user
                    .getLabels().addAll(labels);

            var savedLabels = labels.stream().map(Label::getId).toList();
            var labelsNotFound = updateUserCommand.getLabels().stream().filter(l -> !savedLabels.contains(l)).toList();

            if (isNotEmpty(labelsNotFound)){
                log.info("Some of the updated labels were not found and added to User {}. Label IDs: {}", user.getEmployeeNumber(), labelsNotFound);
            }

            addUserSystemLabelsToProject(user, labels);
        }

        updateCapacitiesInProjects(updateUserCommand, user.getId());

        return userMapper.toDto(userRepository.save(user));
    }

    private void updateCapacitiesInProjects(UpdateUserCommand updateUserCommand, long employeeId) {
        if (!isEmpty(updateUserCommand.getProjectCapacity())) {
            updateUserCommand.getProjectCapacity().forEach((key, value) -> {
                if (value < 0 || value > 100) {
                    throw new UserBadRequestException("Project capacity percentage invalid.");
                }
                var userProject = userProjectRepository.findByUserAndProjectId(employeeId, key)
                        .orElseThrow(() -> new UserNotFoundException("User with employee id " + employeeId + " not found in project " + key + "."));

                userProjectRepository.save(userProject.setCapacity(value));
            });
        }
    }

    private void addUserSystemLabelsToProject(User user, Set<Label> labels) {
        if (isNotEmpty(user.getProjects())){
            user.getProjects().forEach(p -> labels.forEach(label -> {
                if(!p.getProject().getLabels().contains(label)){
                    p.getProject().addLabel(label);
                }
                projectRepository.save(p.getProject());
            }));
        }
    }

    /**
     * Deletes a User from the DB.
     * <p>
     * Throws an exception if the User is non-existent.
     * @param employeeId employeeNumber of deleted User.
     */
    public void deleteUser(String employeeId) {
        var user = userRepository
                .findByEmployeeNumberIgnoreCaseWithOwnedProjects(employeeId)
                .orElseThrow(() -> new UserNotFoundException("User with employee id " + employeeId + " not found."));

        if (isNotEmpty(user.getOwnedProjects())){
            throw new UserBadRequestException("User " + user.getEmployeeNumber() + " cannot be deleted as he is a project owner to: " + user.getOwnedProjects().stream().map(Project::getName).toList() + ".");
        }

        if(isNotEmpty(user.getProjects())){
            projectMemberService.checkIfSystemsLabelsCanBeDeleted(new HashSet<>(user.getProjects()));
        }

        var userSubscriptions = subscriptionRepository.findAllByUserId(user.getId());
        if (isNotEmpty(userSubscriptions)) {
            subscriptionRepository.deleteAll(userSubscriptions);
        }

        userRepository.deleteById(user.getId());

        log.info("User deleted successfully.");
    }

    /**
     * Receives all User's Absences by employee number.
     * @param employeeId User employee number
     * @return Returns all User's absences mapped to a List of {@link AbsenceDTO}.
     */
    public List<AbsenceDTO> getAbsencesByUserBB(String employeeId) {
        log.info("Receiving user {}'s absences.", employeeId);

        var user = userRepository
                .findByEmployeeNumberIgnoreCase(employeeId)
                .map(userMapper::toDto)
                .orElseThrow(() -> new UserNotFoundException("User with employee id " + employeeId + " not found."));

        return absenceMapper.toDto(absenceRepository.findAllByUserId(user.getId()));
    }

    /**
     * Assigns a SYSTEM scope label to a User.
     * <p>
     * Method requests 2 parameters - User employee number and Label name.
     * It finds both passed parameters in the DB as Objects. Then it calls the assignSystemLabel() method from the User entity class.
     * Finally, if the User's projects collection is not empty, it goes through all projects and assigns that label to the project (if not contained already)
     * @param requestBody containing User's employee number and the name of the label we want to assign.
     * @return Returns an updated User mapped to {@link UserDTO}.
     */
    public UserDTO assignSystemLabel(AssignRemoveSystemLabelCommand requestBody) {
        log.info("Assigning label {} to user {}", requestBody.getLabelName(), requestBody.getEmployeeNumber());

        var user = userRepository
                .findByEmployeeNumberIgnoreCaseWithProjects(requestBody.getEmployeeNumber())
                .orElseThrow(() -> new UserNotFoundException("User with employee id " + requestBody.getEmployeeNumber() + " not found."));

        var label = labelRepository
                .findSystemLabelByNameIgnoreCase(requestBody.getLabelName())
                .orElseThrow(() -> new LabelNotFoundException("Label " + requestBody.getLabelName() + " not found."));

        user.assignSystemLabel(label);

        if (user.getProjects() != null){
            user.getProjects().forEach(p -> {
                if(!p.getProject().getLabels().contains(label)){
                    p.getProject().addLabel(label);
                }
            });
        }

        log.info("Successfully assigned label to user.");
        return userMapper.toDto(userRepository.save(user));
    }

    /**
     * Assigns a PROJECT scope label to a User. <p>
     * Method requests 3 parameters - Project ID, User employee number and Label name.
     * Method throws an exception if any of the parameters are not found in the DB.
     * Then it checks whether the Project contains the assigned label - this condition must be true.
     * Finally, it calls the assignProjectLabel method from the User entity class and saves the updated User.
     * @param requestBody containing User's employee number, Project ID, and the label name we want to assign
     * @return Returns an updated User mapped to {@link UserDTO}.
     */
    public UserDTO assignProjectLabel(AssignRemoveProjectLabelCommand requestBody) {
        if (requestBody.getProjectId() == null){
            throw new ProjectBadRequestException("projectId: Project ID is mandatory.");
        }
        log.info("Assigning label {} to user {} in project {}", requestBody.getLabelName(), requestBody.getEmployeeNumber(),requestBody.getProjectId());

        var project = projectRepository
                .findById(requestBody.getProjectId())
                .orElseThrow(() -> new ProjectNotFoundException(requestBody.getProjectId()));

        var user = userRepository.findByEmployeeNumberIgnoreCase(requestBody.getEmployeeNumber())
                .orElseThrow(() -> new UserNotFoundException("User " + requestBody.getEmployeeNumber() + " not found."));

        var label = labelRepository.findProjectScopeLabelInProjectByName(requestBody.getLabelName(),requestBody.getProjectId())
                .orElseThrow(() -> new LabelNotFoundException("Label " + requestBody.getLabelName() + " not found in project."));

        if (!project.getLabels().contains(label) || label.getScope() == LabelScope.SYSTEM){
            throw new LabelNotFoundException("Project scope label " + label.getName() + " does not exist in current project.");
        }

        user.assignProjectLabel(label);
        log.info("Label {} assigned successfully to user {}",requestBody.getLabelName(), requestBody.getEmployeeNumber());
        return userMapper.toDto(userRepository.save(user));
    }

    /**
     * Removes Label from a User (User for both PROJECT and SYSTEM scope labels).
     * <p>
     * Method requests 2 parameters - User Employee number and Label name.
     * Throws exception if any of those are not found in the DB.
     * Then we call the removeLabel method from the User entity class.
     * @param requestBody containing User's employee number and name of the label we want to remove
     * @return Returns an updated User mapped to {@link UserDTO}.
     */
    public UserDTO removeProjectLabel(AssignRemoveProjectLabelCommand requestBody){
        log.info("Removing PROJECT label {} from user {} in project {}", requestBody.getLabelName(), requestBody.getEmployeeNumber(), requestBody.getProjectId());
        var user = userRepository.findByEmployeeNumberIgnoreCase(requestBody.getEmployeeNumber())
                .orElseThrow(() -> new UserNotFoundException("User " + requestBody.getEmployeeNumber() + " not found."));

        var label = labelRepository
                .findProjectScopeLabelInProjectByName(requestBody.getLabelName(),requestBody.getProjectId())
                .orElseThrow(() -> new LabelNotFoundException("PROJECT scope label " + requestBody.getLabelName() + " not found in project with ID " + requestBody.getProjectId()));

        user.removeLabel(label);
        log.info("Successfully removed PROJECT label.");
        return userMapper.toDto(userRepository.save(user));
    }

    public UserDTO removeSystemLabel(AssignRemoveSystemLabelCommand requestBody){
        var user = userRepository.findByEmployeeNumberIgnoreCase(requestBody.getEmployeeNumber())
                .orElseThrow(() -> new UserNotFoundException("User " + requestBody.getEmployeeNumber() + " not found."));

        log.info("Removing SYSTEM label {} from user {}", requestBody.getLabelName(), requestBody.getEmployeeNumber());
        var label = labelRepository.findSystemLabelByNameIgnoreCase(requestBody.getLabelName())
                .orElseThrow(() -> new LabelNotFoundException("SYSTEM scope label " + requestBody.getLabelName() + " not found."));

        var savedUser = userRepository.save(user.removeLabel(label));

        if (isNotEmpty(user.getProjects())){
            projectMemberService.checkIfSystemsLabelsCanBeDeleted(user.getProjects());
        }

        log.info("Successfully removed SYSTEM label.");
        return userMapper.toDto(savedUser);
    }

    /**
     * Autocomplete feature that returns Users from the DB, whose names or employee numbers match the requested parameter (filter).
     * @param filter String which we use for filtering Users whose names or employee number match it.
     * @return Returns List of Users mapped to a {@link UserNameEmployeeNumber}, containing only the Names and the Employee Number.
     */
    public List<UserNameEmployeeNumber> findUsersLike(String filter, String absentUserEmployeeNumber) {
        if(absentUserEmployeeNumber == null){
            log.info("Finding users by autocomplete. Filter: {}", filter);
            var usersInDb = userMapper.toResponseBody(userRepository.findUsersWithNameOrEmployeeNumberLike(filter));
            return usersInDb;
        }
        log.info("Finding substitutes available for user {}. Filter: {}",absentUserEmployeeNumber, filter);
        return userMapper.toResponseBody(userRepository.findUsersInProjectWithNameOrEmployeeNumberLike(absentUserEmployeeNumber, filter));
    }

    public List<UserNameEmployeeNumber> findBeneficiaryCandidatesInProject(String filter, long projectId){
        log.info("Finding beneficiary candidates in project " + projectId);
        return userMapper.toResponseBody(userRepository.findBeneficiaryCandidatesInProject(filter, projectId));
    }

    public Set<String> findCompanies() {
        return userRepository.getAllCompanies();
    }

    /**
     * Filters users in DB by company names and/or system label names.
     * @param command Request body with company names and system label names
     * @return List of UserNameEmployeeNumber, containing only name and employee number.
     * @see UserNameEmployeeNumber
     */
    public Set<UserWithSystemLabelsAndCompany> filterUsersByCompaniesAndSystemLabels(FilterUsersForReportCommand command) {
        if (command.getCompany() != null && command.getSystemLabel() == null) {
            return userMapper.toUserWithSystemLabelsAndCompany(userRepository.findUsersWithCompany(command.getCompany()));
        } else if (command.getCompany() == null && command.getSystemLabel() != null) {
            return userMapper.toUserWithSystemLabelsAndCompany(userRepository.findUsersWithSystemLabel(command.getSystemLabel()));
        }
        return userMapper.toUserWithSystemLabelsAndCompany(userRepository.findUsersWithCompany(command.getCompany())
                .stream().filter(user -> user.getLabels().stream().filter(l -> l.getScope() == LabelScope.SYSTEM).map(Label::getName).sorted().toList().contains(command.getSystemLabel())
        ).collect(Collectors.toSet()));
    }
}
