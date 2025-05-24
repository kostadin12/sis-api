package com.kostadin.sis.notification;

import com.kostadin.sis.subscriptions.model.Subscription;
import com.kostadin.sis.absence.model.Absence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AbsenceNotificationService {
    private final EmailNotificationService emailNotificationService;

    public void sendEmailsOnAbsenceCreate(Absence absence, List<Subscription> userSubscribers) {
        log.info("Entered Absence notification service - sending notification for a new absence.");
        emailNotificationService.sendEmailsOnAbsenceCreate(absence, userSubscribers);
    }

    public void sendEmailsOnAbsenceUpdate(Absence oldAbsence, Absence absence, List<Subscription> userSubscriptions) {
        log.info("Entered Absence notification service - sending notification for an updated absence.");
        emailNotificationService.sendEmailsOnAbsenceUpdate(oldAbsence, absence, userSubscriptions);
    }

    public void sendEmailsOnAbsenceDelete(Absence absence, List<Subscription> userSubscriptions) {
        log.info("Entered Absence notification service - sending notification for a deleted absence.");
        emailNotificationService.sendEmailsOnAbsenceDelete(absence, userSubscriptions);
    }
}
