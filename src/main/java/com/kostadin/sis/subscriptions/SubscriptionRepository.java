package com.kostadin.sis.subscriptions;

import com.kostadin.sis.subscriptions.model.Subscription;
import com.kostadin.sis.subscriptions.model.SubscriptionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, SubscriptionId> {
    @Query("""
            SELECT EXISTS (
            SELECT 1 FROM Subscription s
            WHERE LOWER(s.subscriber.employeeNumber) = LOWER(:employeeNumber)
            AND LOWER(s.user.employeeNumber) = LOWER(:subscribeToEmployeeNumber)
            )
            """)
    boolean existsByEmployeeNumberSubscribedTo(String employeeNumber, String subscribeToEmployeeNumber);

    @Query("""
            FROM Subscription s
            WHERE LOWER(s.subscriber.employeeNumber) = LOWER(:userEmployeeNumber)
            AND LOWER(s.user.employeeNumber) IN :unsubscribeFromUserEmployeeNumbers
            """)
    List<Subscription> findAllBySubscriberEmployeeNumberSubscribedTo(String userEmployeeNumber, List<String> unsubscribeFromUserEmployeeNumbers);

    @Query("""
            FROM Subscription s
            WHERE LOWER(s.user.employeeNumber) = LOWER(:employeeNumber)
            """)
    List<Subscription> findAllSubscribersByEmployeeNumber(String employeeNumber);

    @Query("""
            FROM Subscription s
            WHERE LOWER(s.subscriber.employeeNumber) = LOWER(:employeeNumber)
            """)
    List<Subscription> findAllBySubscriberEmployeeNumber(String employeeNumber);

    @Query("""
            SELECT DISTINCT s
            FROM Subscription s
            WHERE s.id.userId = :id
            OR s.id.subscriberId = :id
            """)
    List<Subscription> findAllByUserId(long id);
}
