package org.is.bandmanager.service.subscribtion;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.is.bandmanager.event.EntityEvent;
import org.is.bandmanager.repository.filter.BestBandAwardFilter;
import org.is.bandmanager.repository.filter.MusicBandFilter;
import org.is.bandmanager.repository.util.PageableUtil;
import org.is.bandmanager.service.BestBandAwardService;
import org.is.bandmanager.service.MusicBandService;
import org.is.bandmanager.service.subscribtion.model.Subscription;
import org.is.bandmanager.service.subscribtion.model.SubscriptionRequest;
import org.is.bandmanager.service.subscribtion.storage.SubscriptionManager;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionManager subscriptionManager;
    private final MusicBandService musicBandService;
    private final BestBandAwardService bestBandAwardService;
    private final SimpMessagingTemplate messagingTemplate;

    private static final String SUBSCRIPTION_DATA_DESTINATION = "/queue/subscription-data";

    @Override
    public <T> UUID createSubscription(SubscriptionRequest<T> request) {
        UUID subscriptionId = subscriptionManager.createSubscription(request);
        log.info("Subscription created: {} for {}", subscriptionId, request.getEntityType().getSimpleName());
        sendSubscriptionDataAsync(subscriptionId);
        return subscriptionId;
    }

    @Override
    public <T> UUID updateSubscription(SubscriptionRequest<T> request) {
        UUID subscriptionId = request.getSubscriptionId();
        subscriptionManager.updateSubscription(subscriptionId, request);
        subscriptionManager.updateSubscription(subscriptionId, request);
        log.debug("Subscription updated: {}", subscriptionId);
        sendSubscriptionDataAsync(subscriptionId);
        return subscriptionId;
    }

    @Override
    public void cancelSubscription(UUID subscriptionId) {
        subscriptionManager.deleteSubscription(subscriptionId);
        log.info("Subscription cancelled: {}", subscriptionId);
    }

    @Override
    public void cancelSessionSubscriptions(String sessionId) {
        List<UUID> cancelledIds = subscriptionManager.deleteSubscriptionsBySession(sessionId);
        if (!cancelledIds.isEmpty()) {
            log.info("Cancelled {} subscriptions for session: {}", cancelledIds.size(), sessionId);
        }
    }

    @Scheduled(fixedRate = 1800000) // 30 минут
    public void cancelDeadSubscriptions() {
        List<UUID> removedIds = subscriptionManager.deleteDeadSubscriptions();
        if (!removedIds.isEmpty()) {
            log.info("Cleaned up {} dead subscriptions", removedIds.size());
        }
    }

    @Async("subscriptionTaskExecutor")
    @EventListener(EntityEvent.class)
    public <T> void handleEntityEvent(EntityEvent<T> event) {
        if (event.getEntities().isEmpty()) {
            log.debug("Empty entity event received: {}", event.getEventType());
            return;
        }
        @SuppressWarnings("unchecked")
        Class<T> entityType = (Class<T>) event.getEntities().get(0).getClass();
        List<Subscription<T>> subscriptions = subscriptionManager.getSubscriptionsByType(entityType);
        if (subscriptions.isEmpty()) {
            log.debug("No subscriptions for entity type: {}", entityType.getSimpleName());
            return;
        }
        log.debug("Processing {} event for {} subscriptions", event.getEventType(), subscriptions.size());
        subscriptions.parallelStream().forEach(subscription ->
                sendSubscriptionDataAsync(subscription.getId())
        );
    }

    @Async("subscriptionTaskExecutor")
    protected void sendSubscriptionDataAsync(UUID subscriptionId) {
        try {
            Subscription<?> subscription = subscriptionManager.getSubscription(subscriptionId);
            if (subscription == null) return;
            Page<?> data = getSubscriptionData(subscription);
            if (data == null) {
                log.warn("No data for subscription: {}", subscriptionId);
                return;
            }
            messagingTemplate.convertAndSendToUser(
                    subscription.getSessionId(),
                    SUBSCRIPTION_DATA_DESTINATION,
                    data
            );
            log.trace("Data sent for subscription: {}", subscriptionId);
        } catch (Exception e) {
            log.error("Failed to send data for subscription: {}", subscriptionId, e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Page<T> getSubscriptionData(Subscription<T> subscription) {
        try {
            return switch (subscription.getEntityType().getSimpleName()) {
                case "MusicBandDto" -> {
                    Pageable pageable = PageableUtil.createMusicBandPageable(
                            subscription.getPage(), subscription.getSize(),
                            subscription.getSort(), subscription.getDirection()
                    );
                    yield (Page<T>) musicBandService.getAll(
                            (MusicBandFilter) subscription.getFilter(), pageable
                    );
                }
                case "BestBandAwardDto" -> {
                    Pageable pageable = PageableUtil.createBestBandAwardPageable(
                            subscription.getPage(), subscription.getSize(),
                            subscription.getSort(), subscription.getDirection()
                    );
                    yield (Page<T>) bestBandAwardService.getAll(
                            (BestBandAwardFilter) subscription.getFilter(), pageable
                    );
                }
                default -> {
                    log.error("Unsupported entity type: {}", subscription.getEntityType().getSimpleName());
                    yield null;
                }
            };
        } catch (Exception e) {
            log.error("Failed to fetch data for subscription: {}", subscription.getId(), e);
            return null;
        }
    }

}