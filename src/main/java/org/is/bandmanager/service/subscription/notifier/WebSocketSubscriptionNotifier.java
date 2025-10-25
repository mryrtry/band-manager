package org.is.bandmanager.service.subscription.notifier;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.is.bandmanager.service.subscription.model.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketSubscriptionNotifier implements SubscriptionNotifier {

    private final SimpMessagingTemplate messagingTemplate;

    private static final String DESTINATION = "/queue/subscriptions-%s";

    @Override
    public void notifySubscriptionMeta(Subscription<?> subscription) {
        try {
            messagingTemplate.convertAndSendToUser(
                    subscription.getPrincipalId(),
                    "/queue/subscription-created",
                    subscription.getSubscriptionId().toString()
            );
        } catch (Exception e) {
            log.warn("User {} not ready", subscription.getPrincipalId());
        }
    }

    @Override
    public void notifySubscription(Subscription<?> subscription, Page<?> data) {
        try {
            log.debug("Send to dest: {}", DESTINATION.formatted(subscription.getSubscriptionId()));
            messagingTemplate.convertAndSendToUser(
                    subscription.getPrincipalId(),
                    DESTINATION.formatted(subscription.getSubscriptionId()),
                    data
            );
            log.debug("Sent subscription update to session {} (subscriptionId={})",
                    subscription.getPrincipalId(), subscription.getSubscriptionId());
        } catch (Exception e) {
            log.error("Failed to send subscription update for {}", subscription.getSubscriptionId(), e);
        }
    }

}

