package org.is.bandmanager.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.is.bandmanager.service.subscription.SubscriptionService;
import org.is.bandmanager.service.subscription.model.request.BestBandAwardSubscriptionRequest;
import org.is.bandmanager.service.subscription.model.request.MusicBandSubscriptionRequest;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.Objects;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class SubscriptionWebSocketController {

    // todo: Защита веб сокета

    private final SubscriptionService subscriptionService;

    @MessageMapping("/subscriptions/music-band.create")
    public void createMusicBandSubscription(
            @Payload MusicBandSubscriptionRequest request,
            StompHeaderAccessor accessor
    ) {
        String principalId = Objects.requireNonNull(accessor.getUser()).getName();
        log.debug("Creating MusicBand subscription for session {}: {}", principalId, request);
        subscriptionService.createSubscription(principalId, request);
    }

    @MessageMapping("/subscriptions/music-band.update")
    public void updateMusicBandSubscription(
            @Payload MusicBandSubscriptionRequest request,
            StompHeaderAccessor accessor
    ) {
        String principalId = Objects.requireNonNull(accessor.getUser()).getName();
        log.debug("Updating MusicBand subscription for session {}: {}", principalId, request);
        subscriptionService.updateSubscription(principalId, request);
    }

    @MessageMapping("/subscriptions/award.create")
    public void createAwardSubscription(
            @Payload BestBandAwardSubscriptionRequest request,
            StompHeaderAccessor accessor
    ) {
        String principalId = Objects.requireNonNull(accessor.getUser()).getName();
        log.debug("Creating BestBandAward subscription for session {}: {}", principalId, request);
        subscriptionService.createSubscription(principalId, request);
    }

    @MessageMapping("/subscriptions/award.update")
    public void updateAwardSubscription(
            @Payload BestBandAwardSubscriptionRequest request,
            StompHeaderAccessor accessor
    ) {
        String principalId = Objects.requireNonNull(accessor.getUser()).getName();
        log.debug("Updating BestBandAward subscription for session {}: {}", principalId, request);
        subscriptionService.updateSubscription(principalId, request);
    }

    @MessageMapping("/subscriptions.cancel.{id}")
    public void cancelSubscription(
            @DestinationVariable UUID id,
            StompHeaderAccessor accessor
    ) {
        String principalId = Objects.requireNonNull(accessor.getUser()).getName();
        log.debug("Cancelling subscription {} for session {}", id, principalId);
        subscriptionService.cancelSubscription(principalId, id);
    }

    @Async("subscriptionTaskExecutor")
    @EventListener(SessionDisconnectEvent.class)
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String principalId = Objects.requireNonNull(accessor.getUser()).getName();
        if (principalId != null) {
            log.info("WebSocket session {} disconnected — cleaning up subscriptions", principalId);
            subscriptionService.cancelAllPrincipalSubscriptions(principalId);
        }
    }

    @EventListener
    public void handleSessionSubscribeEvent(SessionSubscribeEvent event) {
        try {
            SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
            String destination = headers.getDestination();
            if (destination != null && destination.matches("/user/queue/subscriptions-.*")) {
                subscriptionService.notifySubscription(UUID.fromString(destination.split("-", 2)[1]));
            }
        } catch (Exception ignored) {
        }
    }

}