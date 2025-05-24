package com.kostadin.sis.notification;

import com.kostadin.sis.absence.model.Absence;
import com.kostadin.sis.config.mail.EmailRequestBody;
import com.kostadin.sis.config.mail.EmailSenderService;
import com.kostadin.sis.config.mail.template.EmailTemplateRepository;
import com.kostadin.sis.exception.ErrorCode;
import com.kostadin.sis.exception.custom.CustomResponseStatusException;
import com.kostadin.sis.project.model.Project;
import com.kostadin.sis.subscriptions.model.Subscription;
import com.kostadin.sis.user.model.User;
import com.kostadin.sis.userproject.model.UserProject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.kostadin.sis.config.mail.template.TemplateId.*;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("dd/MM/yyyy");

    private static final String CONFIGURATION_NOT_FOUND = "Configuration not found.";
    private final EmailSenderService emailSenderService;

    private final EmailTemplateRepository emailTemplateRepository;


    /**
     * Sending emails when ab Absence is created.
     * There are 3 mailing cases - sending to the User who holds the absence, the Users that are in the same teams as the Absence holder, and the substitute.
     * Method builds a new EmailRequestBody and sends it to the sendEmail method, which uses WebClient to send the request.
     * @param absence Newly created Absence
     */
    public void sendEmailsOnAbsenceCreate(Absence absence, List<Subscription> userSubscribers) {
        User user = absence.getUser();

        var emailTemplateAbsentUser = emailTemplateRepository.findByTemplateId(ABSENCE_CREATE_EMAIL_ABSENT_USER.toString())
                .orElseThrow(() -> new CustomResponseStatusException(HttpStatus.NOT_FOUND, ErrorCode.CONFIGURATION_NOT_FOUND.getErrorCode(), ErrorCode.CONFIGURATION_NOT_FOUND.getReason(),CONFIGURATION_NOT_FOUND));

        sendEmail(new EmailRequestBody()
                .setSendTo(user.getSecondaryEmail() != null ? user.getEmail().concat(", ").concat(user.getSecondaryEmail()) : user.getEmail())
                .setSentFrom(emailTemplateAbsentUser.getSentFrom())
                .setSubject(emailTemplateAbsentUser.getSubject())
                .setBodyContentType(emailTemplateAbsentUser.getContentType())
                .setBody(emailTemplateAbsentUser.getBody()
                        .replace("$absentUser",user.getFirstName().concat(" ").concat(user.getLastName()))
                        .replace("$startDate",DATE_FORMATTER.format(absence.getStartDate()))
                        .replace("$endDate",DATE_FORMATTER.format(absence.getEndDate()))
                )
        );

        log.info("Email to Absence holder ({}) sent.", user.getEmployeeNumber());

        if(isNotEmpty(user.getProjects())){
            var subscribedUsers = userSubscribers.stream().map(Subscription::getSubscriber).toList();

            var usersToEmail = user.getProjects()
                    .stream()
                    .map(UserProject::getProject)
                    .map(Project::getUsers)
                    .flatMap(Collection::stream)
                    .filter(u -> !u.getUser().getEmployeeNumber().equals(user.getEmployeeNumber()))
                    .filter(u -> subscribedUsers.contains(u.getUser()))
                    .distinct()
                    .toList();

            if (isNotEmpty(usersToEmail)){
                var emails = usersToEmail.stream().map(UserProject::getUser).map(User::getEmail).filter(Objects::nonNull).collect(Collectors.joining(", "));
                var secondaryEmails = usersToEmail.stream().map(UserProject::getUser).map(User::getSecondaryEmail).filter(Objects::nonNull).collect(Collectors.joining(", "));

                var emailTemplate = emailTemplateRepository.findByTemplateId(ABSENCE_CREATE_EMAIL_TEAM.toString())
                        .orElseThrow(() -> new CustomResponseStatusException(HttpStatus.NOT_FOUND, ErrorCode.CONFIGURATION_NOT_FOUND.getErrorCode(), ErrorCode.CONFIGURATION_NOT_FOUND.getReason(),CONFIGURATION_NOT_FOUND));

                sendEmail(new EmailRequestBody()
                        .setSendTo(isBlank(secondaryEmails) ? emails : emails.concat(", ").concat(secondaryEmails))
                        .setSentFrom(emailTemplate.getSentFrom())
                        .setSubject(emailTemplate.getSubject())
                        .setBodyContentType(emailTemplate.getContentType())
                        .setBody(emailTemplate.getBody()
                                .replace("$absentUser",user.getFirstName().concat(" ").concat(user.getLastName()))
                                .replace("$startDate",DATE_FORMATTER.format(absence.getStartDate()))
                                .replace("$endDate",DATE_FORMATTER.format(absence.getEndDate()))
                        )
                );

                log.info("Emails to team members included in the same project as {} sent.",user.getEmployeeNumber());
            }
            log.info("Emails sent successfully.");
        }


        if (absence.getSubstitute() != null){
            User substitute = absence.getSubstitute();

            var emailTemplateSubstitute = emailTemplateRepository.findByTemplateId(ABSENCE_CREATE_EMAIL_SUBSTITUTE.toString())
                    .orElseThrow(() -> new CustomResponseStatusException(HttpStatus.NOT_FOUND, ErrorCode.CONFIGURATION_NOT_FOUND.getErrorCode(), ErrorCode.CONFIGURATION_NOT_FOUND.getReason(),CONFIGURATION_NOT_FOUND));

            sendEmail(new EmailRequestBody()
                    .setSendTo(substitute.getSecondaryEmail() != null ? substitute.getEmail().concat(", ").concat(substitute.getSecondaryEmail()) : substitute.getEmail())
                    .setSentFrom(emailTemplateSubstitute.getSentFrom())
                    .setSubject(emailTemplateSubstitute.getSubject())
                    .setBodyContentType(emailTemplateSubstitute.getContentType())
                    .setBody(emailTemplateSubstitute.getBody()
                            .replace("$substitute",substitute.getFirstName().concat(" ").concat(substitute.getLastName()))
                            .replace("$absentUser",user.getFirstName().concat(" ").concat(user.getLastName()))
                            .replace("$startDate",DATE_FORMATTER.format(absence.getStartDate()))
                            .replace("$endDate",DATE_FORMATTER.format(absence.getEndDate()))
                    )
            );

            log.info("Email to {}'s substitute - {}, sent successfully.",user.getEmployeeNumber(),substitute.getEmployeeNumber());
        }
    }

    /**
     * Sending emails to Absence holder, Users in the same teams as the Absence holder, and the substitute.
     * We check to see if the old Absence or the new one has null for a substitute. Then we email the new and old (if any) substitutes.
     * @param oldAbsence Old Absence - before it is updated.
     * @param absence The updated Absence
     */
    public void sendEmailsOnAbsenceUpdate(Absence oldAbsence, Absence absence, List<Subscription> userSubscribers){
        User user = absence.getUser();

        var emailTemplateAbsentUser = emailTemplateRepository.findByTemplateId(ABSENCE_UPDATE_EMAIL_ABSENT_USER.toString())
                .orElseThrow(() -> new CustomResponseStatusException(HttpStatus.NOT_FOUND, ErrorCode.CONFIGURATION_NOT_FOUND.getErrorCode(), ErrorCode.CONFIGURATION_NOT_FOUND.getReason(),CONFIGURATION_NOT_FOUND));

        sendEmail(new EmailRequestBody()
                .setSendTo(user.getSecondaryEmail() != null ? user.getEmail().concat(", ").concat(user.getSecondaryEmail()) : user.getEmail())
                .setSentFrom(emailTemplateAbsentUser.getSentFrom())
                .setSubject(emailTemplateAbsentUser.getSubject())
                .setBodyContentType(emailTemplateAbsentUser.getContentType())
                .setBody(emailTemplateAbsentUser.getBody()
                        .replace("$absentUser",user.getFirstName().concat(" ").concat(user.getLastName()))
                        .replace("$startDate",DATE_FORMATTER.format(absence.getStartDate()))
                        .replace("$endDate",DATE_FORMATTER.format(absence.getEndDate()))
                        .replace("$oldStartDate",DATE_FORMATTER.format(oldAbsence.getStartDate()))
                        .replace("$oldEndDate",DATE_FORMATTER.format(oldAbsence.getEndDate()))
                )
        );

        log.info("Email to Absence holder ({}) sent.", user.getEmployeeNumber());

        if(isNotEmpty(user.getProjects())){
            var subscribedUsers = userSubscribers.stream().map(Subscription::getSubscriber).toList();

            var usersToEmail = user.getProjects()
                    .stream()
                    .map(UserProject::getProject)
                    .map(Project::getUsers)
                    .flatMap(Collection::stream)
                    .filter(u -> !u.getUser().getEmployeeNumber().equals(user.getEmployeeNumber()))
                    .filter(u -> subscribedUsers.contains(u.getUser()))
                    .distinct()
                    .toList();

            if (isNotEmpty(usersToEmail)){
                var emails = usersToEmail.stream().map(UserProject::getUser).map(User::getEmail).filter(Objects::nonNull).collect(Collectors.joining(", "));
                var secondaryEmails = usersToEmail.stream().map(UserProject::getUser).map(User::getSecondaryEmail).filter(Objects::nonNull).collect(Collectors.joining(", "));

                var emailTemplate = emailTemplateRepository.findByTemplateId(ABSENCE_UPDATE_EMAIL_TEAM.toString())
                        .orElseThrow(() -> new CustomResponseStatusException(HttpStatus.NOT_FOUND, ErrorCode.CONFIGURATION_NOT_FOUND.getErrorCode(), ErrorCode.CONFIGURATION_NOT_FOUND.getReason(),CONFIGURATION_NOT_FOUND));

                sendEmail(new EmailRequestBody()
                        .setSendTo(isBlank(secondaryEmails) ? emails : emails.concat(", ").concat(secondaryEmails))
                        .setSentFrom(emailTemplate.getSentFrom())
                        .setSubject(emailTemplate.getSubject())
                        .setBodyContentType(emailTemplate.getContentType())
                        .setBody(emailTemplate.getBody()
                                .replace("$absentUser",user.getFirstName().concat(" ").concat(user.getLastName()))
                                .replace("$startDate",DATE_FORMATTER.format(absence.getStartDate()))
                                .replace("$endDate",DATE_FORMATTER.format(absence.getEndDate()))
                                .replace("$oldStartDate",DATE_FORMATTER.format(oldAbsence.getStartDate()))
                                .replace("$oldEndDate",DATE_FORMATTER.format(oldAbsence.getEndDate()))
                        )
                );

                log.info("Emails to team members included in the same project as {} sent.",user.getEmployeeNumber());
            }
        }

        if (oldAbsence.getSubstitute() == absence.getSubstitute() && oldAbsence.getSubstitute() != null){
            User substitute = absence.getSubstitute();

            var emailTemplateSubstitute = emailTemplateRepository.findByTemplateId(ABSENCE_UPDATE_EMAIL_SUBSTITUTE.toString())
                    .orElseThrow(() -> new CustomResponseStatusException(HttpStatus.NOT_FOUND, ErrorCode.CONFIGURATION_NOT_FOUND.getErrorCode(), ErrorCode.CONFIGURATION_NOT_FOUND.getReason(),CONFIGURATION_NOT_FOUND));

            sendEmail(new EmailRequestBody()
                    .setSendTo(substitute.getSecondaryEmail() != null ? substitute.getEmail().concat(", ").concat(substitute.getSecondaryEmail()) : substitute.getEmail())
                    .setSentFrom(emailTemplateSubstitute.getSentFrom())
                    .setSubject(emailTemplateSubstitute.getSubject())
                    .setBodyContentType(emailTemplateSubstitute.getContentType())
                    .setBody(emailTemplateSubstitute.getBody()
                            .replace("$substitute",substitute.getFirstName().concat(" ").concat(substitute.getLastName()))
                            .replace("$absentUser",user.getFirstName().concat(" ").concat(user.getLastName()))
                            .replace("$startDate",DATE_FORMATTER.format(absence.getStartDate()))
                            .replace("$endDate",DATE_FORMATTER.format(absence.getEndDate()))
                            .replace("$oldStartDate",DATE_FORMATTER.format(oldAbsence.getStartDate()))
                            .replace("$oldEndDate",DATE_FORMATTER.format(oldAbsence.getEndDate()))
                    )
            );

            log.info("Email to {}'s substitute - {}, sent successfully.",user.getEmployeeNumber(),substitute.getEmployeeNumber());
        } else {

            if(oldAbsence.getSubstitute() != null){
                User oldSubstitute = oldAbsence.getSubstitute();

                var emailTemplateOldSubstitute = emailTemplateRepository.findByTemplateId(ABSENCE_UPDATE_EMAIL_OLD_SUBSTITUTE.toString())
                        .orElseThrow(() -> new CustomResponseStatusException(HttpStatus.NOT_FOUND, ErrorCode.CONFIGURATION_NOT_FOUND.getErrorCode(), ErrorCode.CONFIGURATION_NOT_FOUND.getReason(),CONFIGURATION_NOT_FOUND));


                sendEmail(new EmailRequestBody()
                        .setSendTo(oldSubstitute.getSecondaryEmail() != null ? oldSubstitute.getEmail().concat(", ").concat(oldSubstitute.getSecondaryEmail()) : oldSubstitute.getEmail())
                        .setSentFrom(emailTemplateOldSubstitute.getSentFrom())
                        .setSubject(emailTemplateOldSubstitute.getSubject())
                        .setBodyContentType(emailTemplateOldSubstitute.getContentType())
                        .setBody(emailTemplateOldSubstitute.getBody()
                                .replace("$oldSubstitute",oldSubstitute.getFirstName().concat(" ").concat(oldSubstitute.getLastName()))
                                .replace("$absentUser",user.getFirstName().concat(" ").concat(user.getLastName()))
                                .replace("$startDate",DATE_FORMATTER.format(absence.getStartDate()))
                                .replace("$endDate",DATE_FORMATTER.format(absence.getEndDate()))
                                .replace("$oldStartDate",DATE_FORMATTER.format(oldAbsence.getStartDate()))
                                .replace("$oldEndDate",DATE_FORMATTER.format(oldAbsence.getEndDate()))
                        )
                );
            }

            if(absence.getSubstitute() != null){
                User newSubstitute = absence.getSubstitute();

                var emailTemplateNewSubstitute = emailTemplateRepository.findByTemplateId(ABSENCE_UPDATE_EMAIL_NEW_SUBSTITUTE.toString())
                        .orElseThrow(() -> new CustomResponseStatusException(HttpStatus.NOT_FOUND, ErrorCode.CONFIGURATION_NOT_FOUND.getErrorCode(), ErrorCode.CONFIGURATION_NOT_FOUND.getReason(),CONFIGURATION_NOT_FOUND));


                sendEmail(new EmailRequestBody()
                        .setSendTo(newSubstitute.getSecondaryEmail() != null ? newSubstitute.getEmail().concat(", ").concat(newSubstitute.getSecondaryEmail()) : newSubstitute.getEmail())
                        .setSentFrom(emailTemplateNewSubstitute.getSentFrom())
                        .setSubject(emailTemplateNewSubstitute.getSubject())
                        .setBodyContentType(emailTemplateNewSubstitute.getContentType())
                        .setBody(emailTemplateNewSubstitute.getBody()
                                .replace("$substitute",newSubstitute.getFirstName().concat(" ").concat(newSubstitute.getLastName()))
                                .replace("$absentUser",user.getFirstName().concat(" ").concat(user.getLastName()))
                                .replace("$startDate",DATE_FORMATTER.format(absence.getStartDate()))
                                .replace("$endDate",DATE_FORMATTER.format(absence.getEndDate()))
                                .replace("$oldStartDate",DATE_FORMATTER.format(oldAbsence.getStartDate()))
                                .replace("$oldEndDate",DATE_FORMATTER.format(oldAbsence.getEndDate()))
                        )
                );
            }
        }
        log.info("Emails sent successfully.");
    }

    /**
     * Sending emails when an Absence is being deleted. We send the email to all 3 (if existent) users - the Absence holder, his team(s) members, and his substitute.
     * @param absence Absence being deleted.
     */
    public void sendEmailsOnAbsenceDelete(Absence absence, List<Subscription> userSubscribers){
        User user = absence.getUser();

        var emailTemplateAbsentUser = emailTemplateRepository.findByTemplateId(ABSENCE_DELETE_EMAIL_ABSENT_USER.toString())
                .orElseThrow(() -> new CustomResponseStatusException(HttpStatus.NOT_FOUND, ErrorCode.CONFIGURATION_NOT_FOUND.getErrorCode(), ErrorCode.CONFIGURATION_NOT_FOUND.getReason(),CONFIGURATION_NOT_FOUND));

        sendEmail(new EmailRequestBody()
                .setSendTo(user.getSecondaryEmail() != null ? user.getEmail().concat(", ").concat(user.getSecondaryEmail()) : user.getEmail())
                .setSentFrom(emailTemplateAbsentUser.getSentFrom())
                .setSubject(emailTemplateAbsentUser.getSubject())
                .setBodyContentType(emailTemplateAbsentUser.getContentType())
                .setBody(emailTemplateAbsentUser.getBody()
                        .replace("$absentUser",user.getFirstName().concat(" ").concat(user.getLastName()))
                        .replace("$oldStartDate",DATE_FORMATTER.format(absence.getStartDate()))
                        .replace("$oldEndDate",DATE_FORMATTER.format(absence.getEndDate()))
                )
        );

        log.info("Email to Absence holder ({}) sent.", user.getEmployeeNumber());

        if(isNotEmpty(user.getProjects())){
            var subscribedUsers = userSubscribers.stream().map(Subscription::getSubscriber).toList();

            Set<User> usersToEmail = new HashSet<>();
            user.getProjects().forEach(project ->
                    usersToEmail.addAll(project.getProject().getUsers().stream().map(UserProject::getUser)
                            .filter(uUser -> !uUser.getEmployeeNumber().equals(user.getEmployeeNumber()))
                            .filter(subscribedUsers::contains)
                            .toList()));

            if (isNotEmpty(usersToEmail)){
                var emails = usersToEmail.stream().map(User::getEmail).filter(Objects::nonNull).collect(Collectors.joining(", "));
                var secondaryEmails = usersToEmail.stream().map(User::getSecondaryEmail).filter(Objects::nonNull).collect(Collectors.joining(", "));

                var emailTemplate = emailTemplateRepository.findByTemplateId(ABSENCE_DELETE_EMAIL_TEAM.toString())
                        .orElseThrow(() -> new CustomResponseStatusException(HttpStatus.NOT_FOUND, ErrorCode.CONFIGURATION_NOT_FOUND.getErrorCode(), ErrorCode.CONFIGURATION_NOT_FOUND.getReason(),CONFIGURATION_NOT_FOUND));

                sendEmail(new EmailRequestBody()
                        .setSendTo(isBlank(secondaryEmails) ? emails : emails.concat(", ").concat(secondaryEmails))
                        .setSentFrom(emailTemplate.getSentFrom())
                        .setSubject(emailTemplate.getSubject())
                        .setBodyContentType(emailTemplate.getContentType())
                        .setBody(emailTemplate.getBody()
                                .replace("$absentUser",user.getFirstName().concat(" ").concat(user.getLastName()))
                                .replace("$oldStartDate",DATE_FORMATTER.format(absence.getStartDate()))
                                .replace("$oldEndDate",DATE_FORMATTER.format(absence.getEndDate()))
                        )
                );

                log.info("Emails to team members included in the same project as {} sent.",user.getEmployeeNumber());
            }
        }


        if (absence.getSubstitute() != null){
            User substitute = absence.getSubstitute();

            var emailTemplateSubstitute = emailTemplateRepository.findByTemplateId(ABSENCE_DELETE_EMAIL_SUBSTITUTE.toString())
                    .orElseThrow(() -> new CustomResponseStatusException(HttpStatus.NOT_FOUND, ErrorCode.CONFIGURATION_NOT_FOUND.getErrorCode(), ErrorCode.CONFIGURATION_NOT_FOUND.getReason(),CONFIGURATION_NOT_FOUND));

            sendEmail(new EmailRequestBody()
                    .setSendTo(substitute.getSecondaryEmail() != null ? substitute.getEmail().concat(", ").concat(substitute.getSecondaryEmail()) : substitute.getEmail())
                    .setSentFrom(emailTemplateSubstitute.getSentFrom())
                    .setSubject(emailTemplateSubstitute.getSubject())
                    .setBodyContentType(emailTemplateSubstitute.getContentType())
                    .setBody(emailTemplateSubstitute.getBody()
                            .replace("$substitute",substitute.getFirstName().concat(" ").concat(substitute.getLastName()))
                            .replace("$absentUser",user.getFirstName().concat(" ").concat(user.getLastName()))
                            .replace("$oldStartDate",DATE_FORMATTER.format(absence.getStartDate()))
                            .replace("$oldEndDate",DATE_FORMATTER.format(absence.getEndDate()))
                    )
            );

            log.info("Email to {}'s substitute - {}, sent successfully.",user.getEmployeeNumber(),substitute.getEmployeeNumber());
        }
        log.info("Emails sent successfully.");
    }

    /**
     * A method which takes an EmailRequestBody and sends a WebClient POST request to the external API.
     * @param emailRequestBody Email Body, including the HTML template, sendTo, replyTo, subject etc.
     */

    private void sendEmail(EmailRequestBody emailRequestBody) {
        emailSenderService.sendEmail(emailRequestBody);
    }
}
