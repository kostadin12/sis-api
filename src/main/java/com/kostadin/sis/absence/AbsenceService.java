package com.kostadin.sis.absence;

import com.kostadin.sis.absence.model.Absence;
import com.kostadin.sis.absence.model.request.SaveAbsenceCommand;
import com.kostadin.sis.absence.model.response.AbsenceDTO;
import com.kostadin.sis.notification.AbsenceNotificationService;
import com.kostadin.sis.subscriptions.SubscriptionRepository;
import com.kostadin.sis.common.exception.AbsenceBadRequestException;
import com.kostadin.sis.common.exception.AbsenceNotFoundException;
import com.kostadin.sis.common.exception.UserNotFoundException;
import com.kostadin.sis.mapper.AbsenceMapper;
import com.kostadin.sis.user.UserRepository;
import com.kostadin.sis.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AbsenceService {
    private final AbsenceRepository absenceRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final AbsenceNotificationService absenceNotificationService;
    private final AbsenceMapper mapper;

    /**
     * Creates an absence to the DB and assigns it to a user.
     * <p>
     * Calls a validation method which checks for incorrect Absence values and throws an exception if that is the case.
     * Checks if a user with the passed employee number exists and throws an exception if there is no such user.
     * Also checks whether the absence is overlapping any other user absences and if it does - it throws an exception.
     * @param saveAbsenceCommand containing Start/End dates and Absence type (reason)
     * @return Maps saved Absence to {@link AbsenceDTO}.
     */
    @Transactional(propagation = REQUIRES_NEW)
    public AbsenceDTO saveAbsence(SaveAbsenceCommand saveAbsenceCommand) {
        log.info("Saving new absence to the DB: {}", saveAbsenceCommand);
        validateAbsence(saveAbsenceCommand);

        var user = userRepository
                .findByEmployeeNumberIgnoreCaseWithProjects(saveAbsenceCommand.getEmployeeNumber())
                .orElseThrow(() -> new UserNotFoundException("User with employee number " + saveAbsenceCommand.getEmployeeNumber() + " not found."));

        if (absenceRepository.checkInvalidAbsenceDates(user.getId(), saveAbsenceCommand.getStartDate(), saveAbsenceCommand.getEndDate())){
            throw new AbsenceBadRequestException("Absence overlaps another one of user " + user.getEmployeeNumber() + "'s absences.");
        }

        Absence absence = mapper.toEntity(saveAbsenceCommand)
                .setUser(user)
                .setSubstitute(getAbsenceSubstitute(saveAbsenceCommand.getSubstituteEmployeeNumber(), user))
                .setBookedDate(LocalDate.now());

        var savedAbsence = absenceRepository.save(absence);

        log.info("Absence saved successfully.");

        var userSubscribers = subscriptionRepository.findAllSubscribersByEmployeeNumber(user.getEmployeeNumber());

        absenceNotificationService.sendEmailsOnAbsenceCreate(absence, userSubscribers);

        return mapper.toDto(savedAbsence);
    }

    /**
     * Receives all absences from the DB and filters them by the employee number passed as a request parameter.
     * @param employeeNumber Request parameter, which absences are filtered by.
     * @return Maps returned value to a list of {@link AbsenceDTO}
     */
    public List<AbsenceDTO> getAbsences(String employeeNumber) {
        log.info("Receiving all absences from the DB.");
        return absenceRepository.findAllByUserEmployeeNumber(employeeNumber)
                .stream().map(mapper::toDto)
                .toList();
    }

    /**
     * Receives an Absence from the DB by its id.
     * @param id Absence id
     * @return Maps returned Absence to {@link AbsenceDTO}
     */
    public AbsenceDTO getAbsenceById(long id) {
        log.info("Receiving absence with id {}",id);
        return absenceRepository
                .findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new AbsenceNotFoundException("Absence with id " + id + " not found."));
    }

    /**
     * Updates an Absence.
     * <p>
     * Method takes an Employee Number - for changing the holder of the Absence (if needed).
     * Calls a validation method which checks for incorrect Absence values and throws an exception if that is the case.
     * Throws an exception if such absence (with passed id) or such user (with passed employeeNumber) does not exist.
     * Also validates newly created absence and checks if it overlaps any of the user's absences.
     * @param id ID of the updated Absence
     * @param saveAbsenceCommand Request body, containing the new (updated) absence values
     * @return Maps updated Absence to {@link AbsenceDTO}.
     */
    public AbsenceDTO updateAbsence(long id, SaveAbsenceCommand saveAbsenceCommand) {
        log.info("Updating absence with id {}",id);
        validateAbsence(saveAbsenceCommand);

        var absence = absenceRepository
                .findByIdAndEmployeeNumber(id, saveAbsenceCommand.getEmployeeNumber())
                .orElseThrow(() -> new AbsenceNotFoundException("Absence with id " + id + " not found."));

        if (absenceRepository.existsAnotherUserAbsenceWithinTimeRangeIgnoringId(absence.getUser().getId(), saveAbsenceCommand.getStartDate(), saveAbsenceCommand.getEndDate(),absence.getId())){
            throw new AbsenceBadRequestException("Absence overlaps another one of user " + absence.getUser().getEmployeeNumber() + "'s absences.");
        }

        var oldAbsence = new Absence()
                .setId(absence.getId())
                .setAbsenceType(absence.getAbsenceType())
                .setStartDate(absence.getStartDate())
                .setEndDate(absence.getEndDate())
                .setUser(absence.getUser())
                .setSubstitute(absence.getSubstitute());

        absence.setSubstitute(getAbsenceSubstitute(saveAbsenceCommand.getSubstituteEmployeeNumber(), absence.getUser()));

        absence
                .setStartDate(saveAbsenceCommand.getStartDate())
                .setEndDate(saveAbsenceCommand.getEndDate())
                .setAbsenceType(saveAbsenceCommand.getAbsenceType());

        var savedAbsence = absenceRepository.save(absence);
        log.info("Successfully updated absence with id {}", absence.getId());

        var userSubscribers = subscriptionRepository.findAllSubscribersByEmployeeNumber(saveAbsenceCommand.getEmployeeNumber());
        absenceNotificationService.sendEmailsOnAbsenceUpdate(oldAbsence,absence, userSubscribers);
        return mapper.toDto(savedAbsence);
    }

    /**
     * Deletes an absence from the DB.
     * Throws an exception if such Absence is non-existent.
     * @param id Absence id
     */
    public void deleteAbsence(long id, String employeeNumber) {
        log.info("Deleting absence with id {}",id);

        var absence = absenceRepository.findByIdAndEmployeeNumber(id, employeeNumber)
                .orElseThrow(() -> new AbsenceNotFoundException("Absence with id " + id + " not found."));

        absenceRepository.delete(absence);
        log.info("Absence with id {} deleted successfully.", absence.getId());

        var userSubscribers = subscriptionRepository.findAllSubscribersByEmployeeNumber(employeeNumber);
        absenceNotificationService.sendEmailsOnAbsenceDelete(absence, userSubscribers);
    }

    /**
     * Method for validating a SaveAbsenceCommand.
     * <p>
     * Called when saving or updating absences.
     * Throws an exception if the start and end dates are incorrect (start date is after end date).
     * @param saveAbsenceCommand Body with checked values.
     */
    private void validateAbsence (SaveAbsenceCommand saveAbsenceCommand){
        if (saveAbsenceCommand.isEmployeeSubstituteToHimself()) {
            throw new AbsenceBadRequestException("User cannot be a substitute to himself.");
        }

        if (saveAbsenceCommand.isStartDateAfterEndDate()) {
            throw new AbsenceBadRequestException("Absence start date cannot be after the end date.");
        }

        if (saveAbsenceCommand.isAbsencePeriodTooLong()) {
            throw new AbsenceBadRequestException("Absence period is too long.");
        }

        if (saveAbsenceCommand.isAbsenceTooFarBackOrTooFarAhead()) {
            throw new AbsenceBadRequestException("Invalid absence period: Absence must be no more than 2 weeks in the past OR 1 year in the future.");
        }
    }

    private User getAbsenceSubstitute(String substituteEmployeeNumber, User user) {
        if (substituteEmployeeNumber == null) {
            return null;
        }

        if (isEmpty(user.getProjects())) {
            throw new AbsenceBadRequestException("User not included in projects.");
        }

        return userRepository.findSubstituteIfAvailable(user.getEmployeeNumber(), substituteEmployeeNumber)
                .orElseThrow(() -> new AbsenceBadRequestException("Substitute must be in the same project team(s) as the absent user."));
    }
}
