package com.kostadin.sis.subscriptions;

import com.kostadin.sis.common.exception.SubscriptionBadRequest;
import com.kostadin.sis.common.exception.UserNotFoundException;
import com.kostadin.sis.mapper.UserMapper;
import com.kostadin.sis.subscriptions.model.Subscription;
import com.kostadin.sis.user.UserRepository;
import com.kostadin.sis.user.model.request.SubscribeToCommand;
import com.kostadin.sis.user.model.request.UnsubscribeFromCommand;
import com.kostadin.sis.user.model.response.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubscriptionService {
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserMapper userMapper;

    public List<UserDTO> subscribeTo(SubscribeToCommand command) {
        log.info("Subscribing user {} to {}", command.getUserEmployeeNumber(), command.getSubscribeToUserEmployeeNumbers());

        var employeeNumber = command.getUserEmployeeNumber();
        var addedSubscriptions = new ArrayList<Subscription>();

        for (String subscribeToEmployeeNumber : command.getSubscribeToUserEmployeeNumbers()) {
            if (subscriptionRepository.existsByEmployeeNumberSubscribedTo(employeeNumber, subscribeToEmployeeNumber)) {
                throw new SubscriptionBadRequest("User " + employeeNumber + " is already subscribed to " + subscribeToEmployeeNumber + ".");
            }
            addedSubscriptions.add(generateSubscription(employeeNumber, subscribeToEmployeeNumber));
        }

        var savedSubscriptions = subscriptionRepository.saveAll(addedSubscriptions);

        log.info("Successfully subscribed.");
        return userMapper.toDto(savedSubscriptions.stream().map(Subscription::getUser).toList());
    }

    private Subscription generateSubscription(String employeeNumber, String subscribeToEmployeeNumber) {
        var user = userRepository.findByEmployeeNumberIgnoreCase(employeeNumber)
                .orElseThrow(() -> new UserNotFoundException("User " + employeeNumber + " not found."));
        var subscribeTo = userRepository.findByEmployeeNumberIgnoreCase((subscribeToEmployeeNumber))
                .orElseThrow(() -> new UserNotFoundException("User " + subscribeToEmployeeNumber + " not found."));

        return new Subscription(subscribeTo, user);
    }

    public List<UserDTO> unsubscribeFrom(UnsubscribeFromCommand command){
        log.info("Unsubscribing user {} from {}", command.getUserEmployeeNumber(), command.getUnsubscribeFromUserEmployeeNumbers());

        var lowerUnsubscribeFromEmployeeNumbers = command.getUnsubscribeFromUserEmployeeNumbers().stream().map(String::toLowerCase).toList();
        var userSubscriptions = subscriptionRepository.findAllBySubscriberEmployeeNumberSubscribedTo(command.getUserEmployeeNumber(), lowerUnsubscribeFromEmployeeNumbers);

        if (isEmpty(userSubscriptions)) {
            throw new SubscriptionBadRequest("User " + command.getUserEmployeeNumber() + " is not subscribed to any given user.");
        }

        subscriptionRepository.deleteAll(userSubscriptions);

        log.info("Unsubscribed successfully.");
        return userMapper.toDto(userSubscriptions.stream().map(Subscription::getUser).toList());
    }
}
