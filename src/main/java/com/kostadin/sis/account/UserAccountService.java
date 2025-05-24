package com.kostadin.sis.account;

import com.kostadin.sis.auth.response.SignupRequest;
import com.kostadin.sis.common.exception.UserBadRequestException;
import com.kostadin.sis.common.exception.UserNotFoundException;
import com.kostadin.sis.exception.ErrorCode;
import com.kostadin.sis.exception.custom.CustomResponseStatusException;
import com.kostadin.sis.label.LabelRepository;
import com.kostadin.sis.label.model.Label;
import com.kostadin.sis.mapper.UserMapper;
import com.kostadin.sis.subscriptions.SubscriptionRepository;
import com.kostadin.sis.user.UserRepository;
import com.kostadin.sis.user.color.UserColorPalette;
import com.kostadin.sis.user.color.UserColorResponse;
import com.kostadin.sis.user.color.UserColorService;
import com.kostadin.sis.user.model.User;
import com.kostadin.sis.user.model.UserRole;
import com.kostadin.sis.user.model.response.UserAccount;
import com.kostadin.sis.userproject.UserProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

import static com.kostadin.sis.label.model.LabelScope.SYSTEM;
import static com.kostadin.sis.user.model.UserRole.ROLE_ADMIN;
import static com.kostadin.sis.user.model.UserRole.ROLE_EMPLOYEE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserAccountService {
    private final UserColorService userColorService;
    private final UserRepository userRepository;
    private final LabelRepository labelRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserProjectRepository userProjectRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserAccount getUserAccount(String email){

        log.info("Fetching account for user with email {}", email);

        var user=  userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(()->new UserNotFoundException(email));

        var userSubscriptions = subscriptionRepository.findAllBySubscriberEmployeeNumber(user.getEmployeeNumber());

        return userMapper.toAccount(user, userSubscriptions);
    }

    /**
     * Creates a new user with email and password
     */
    public User createUserWithEmail(SignupRequest signupRequest) {
        log.info("Creating new user with email {}", signupRequest.getEmail());

        // Check if email exists
        if (userRepository.existsByEmailIgnoreCase(signupRequest.getEmail())) {
            throw new UserBadRequestException("Error: Email is already taken!");
        }

        // Check if employee number exists
        if (userRepository.existsByEmployeeNumber(signupRequest.getEmployeeNumber())) {
            throw new UserBadRequestException("Error: Employee number is already in use!");
        }

        // Create new user
        User user = new User()
                .setFirstName(signupRequest.getFirstName())
                .setLastName(signupRequest.getLastName())
                .setEmployeeNumber(signupRequest.getEmployeeNumber())
                .setEmail(signupRequest.getEmail())
                .setPassword(passwordEncoder.encode(signupRequest.getPassword()))
                .setPhone(signupRequest.getPhone())
                .setCompany(signupRequest.getCompany())
                .setRole(signupRequest.getRole() != null ? signupRequest.getRole() : ROLE_EMPLOYEE)
                .setColor("#ffffff");

        // Assign default label if needed based on company or role
        if (signupRequest.getCompany() != null && !signupRequest.getCompany().isBlank()) {
            user.setLabels(new HashSet<>(List.of(selectSystemLabel(signupRequest.getCompany()))));
        }

        log.info("Created new user with employee number {}", signupRequest.getEmployeeNumber());

        return userRepository.save(user);
    }

    public Label selectSystemLabel(String jobTitle) {
        return labelRepository.findSystemLabelByNameIgnoreCase(jobTitle)
                .orElseGet(() -> createNewJobTitleLabel(jobTitle));
    }

    private Label createNewJobTitleLabel(String jobTitle) {
        return labelRepository.save(
                new Label()
                        .setName(jobTitle)
                        .setScope(SYSTEM)
        );
    }

    private UserRole checkRole(String employeeNumber){
        return ROLE_ADMIN;
    }

    public UserColorResponse getRandomColor(String employeeNumber) {
        log.info("Generating random color.");

        var userColorsWithinProjects = userProjectRepository.findAllColorsWithinProjectsByEmployeeNumber(employeeNumber);

        return new UserColorResponse(userColorService.generateColor(userColorsWithinProjects));
    }

    public UserColorPalette getColorPalette(int count, String employeeNumber) {
        log.info("Generating a color palette.");
        if (count < 1 || count > 30) {
            throw new CustomResponseStatusException(BAD_REQUEST, ErrorCode.URI_VARIABLE_BAD_REQUEST.getErrorCode(), ErrorCode.URI_VARIABLE_BAD_REQUEST.getReason(), "Color count must be between 1 and 30.");
        }
        var userColorsWithinProjects = userProjectRepository.findAllColorsWithinProjectsByEmployeeNumber(employeeNumber);

        return new UserColorPalette(userColorService.generateColorPalette(count, userColorsWithinProjects));
    }
}
