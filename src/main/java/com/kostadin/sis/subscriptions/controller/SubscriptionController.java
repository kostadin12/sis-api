package com.kostadin.sis.subscriptions.controller;

import com.kostadin.sis.subscriptions.SubscriptionService;
import com.kostadin.sis.user.model.request.SubscribeToCommand;
import com.kostadin.sis.user.model.request.UnsubscribeFromCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.ACCEPTED;

@RestController
@RequestMapping("/sis/subscriptions/v1.0.0")
@RequiredArgsConstructor
public class SubscriptionController implements SubscriptionOperations {
    private final SubscriptionService subscriptionService;

    @Override
    @PostMapping("/subscribe")
    @ResponseStatus(ACCEPTED)
    public void subscribeTo(@Valid @RequestBody SubscribeToCommand subscribeToCommand) {
        subscriptionService.subscribeTo(subscribeToCommand);
    }

    @Override
    @PostMapping("/unsubscribe")
    @ResponseStatus(ACCEPTED)
    public void unsubscribeFrom(@Valid @RequestBody UnsubscribeFromCommand unsubscribeFromCommand) {
        subscriptionService.unsubscribeFrom(unsubscribeFromCommand);
    }
}
