package org.is.bandmanager.service.subscription.eventListener;

import lombok.RequiredArgsConstructor;
import org.is.bandmanager.event.EntityEvent;
import org.is.bandmanager.service.subscription.SubscriptionService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubscriptionEventListener {

    private final SubscriptionService subscriptionService;

    @Async("subscriptionTaskExecutor")
    @EventListener(EntityEvent.class)
    public void handleEntityEvent(EntityEvent<?> entityEvent) {
        subscriptionService.handleEntityEventInternal(entityEvent);
    }

}