package org.is.bandmanager.service.subscription.scheduled;

import lombok.RequiredArgsConstructor;
import org.is.bandmanager.constants.SubscriptionsConstants;
import org.is.bandmanager.service.subscription.SubscriptionService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubscriptionScheduledService {

    private final SubscriptionService subscriptionService;

    @Async("subscriptionTaskExecutor")
    @Scheduled(fixedRate = SubscriptionsConstants.DEAD_SUBSCRIPTION_CHECK_INTERVAL_MILLISECONDS)
    public void cancelDeadSubscriptions() {
        subscriptionService.cleanupDeadSubscriptionsInternal();
    }

}