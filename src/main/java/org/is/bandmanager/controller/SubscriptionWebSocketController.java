package org.is.bandmanager.controller;

import lombok.RequiredArgsConstructor;
import org.is.bandmanager.service.subscribtion.SubscriptionService;
import org.is.bandmanager.service.subscribtion.model.SubscriptionRequest;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class SubscriptionWebSocketController {

    private final SubscriptionService subscriptionService;

    @MessageMapping("/subscriptions/create")
    @SendToUser("/queue/subscriptions")
    public SubscriptionResponse createSubscription(
            @Payload SubscriptionRequest<?> request,
            SimpMessageHeaderAccessor headerAccessor) {
        System.err.println(headerAccessor.getMessageHeaders());
        String sessionId = headerAccessor.getSessionId();
        try {
            request.setSessionId(sessionId);
            UUID subscriptionId = subscriptionService.createSubscription(request);
            return new SubscriptionResponse(subscriptionId, "CREATED");
        } catch (Exception e) {
            return new SubscriptionResponse(null, "ERROR");
        }
    }

    @MessageMapping("/subscriptions/update")
    @SendToUser("/queue/subscriptions")
    public SubscriptionResponse updateSubscription(
            @Payload SubscriptionRequest<?> request,
            SimpMessageHeaderAccessor headerAccessor) {
        System.err.println(headerAccessor.getMessageHeaders());
        String sessionId = headerAccessor.getSessionId();
        try {
            request.setSessionId(sessionId);
            UUID updateSubscriptionId = subscriptionService.updateSubscription(request);
            return new SubscriptionResponse(updateSubscriptionId, "UPDATED");
        } catch (Exception e) {
            return new SubscriptionResponse(null, "ERROR");
        }
    }

    @MessageMapping("/subscriptions/cancel")
    public void cancelSubscription(
            @Payload UUID subscriptionId) {
        subscriptionService.cancelSubscription(subscriptionId);
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        subscriptionService.cancelSessionSubscriptions(event.getSessionId());
    }

    public record SubscriptionResponse(
            UUID subscriptionId,
            String status
    ) {
    }

}