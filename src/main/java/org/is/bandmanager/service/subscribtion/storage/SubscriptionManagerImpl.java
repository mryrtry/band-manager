package org.is.bandmanager.service.subscribtion.storage;

import lombok.extern.slf4j.Slf4j;
import org.is.bandmanager.exception.ServiceException;
import org.is.bandmanager.service.subscribtion.model.Subscription;
import org.is.bandmanager.service.subscribtion.model.SubscriptionRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.is.bandmanager.exception.message.ServiceErrorMessage.SOURCE_NOT_FOUND;

@Slf4j
@Service
public class SubscriptionManagerImpl implements SubscriptionManager {

    private final Map<UUID, Subscription<?>> subscriptions = new ConcurrentHashMap<>();
    private final Map<String, List<UUID>> sessionSubscriptions = new ConcurrentHashMap<>();

    private Subscription<?> findById(UUID id) {
        Subscription<?> subscription = subscriptions.get(id);
        if (subscription == null) {
            throw new ServiceException(SOURCE_NOT_FOUND, "Subscription", id);
        }
        return subscription;
    }

    @Override
    public <T> UUID createSubscription(SubscriptionRequest<T> request) {
        UUID subscriptionId = UUID.randomUUID();

        Subscription<T> subscription = new Subscription<>(
                subscriptionId,
                request.getEntityType(),
                request.getFilter(),
                request.getPage(),
                request.getSize(),
                request.getSort(),
                request.getDirection(),
                request.getSessionId(),
                Instant.now(),
                Instant.now()
        );

        subscriptions.put(subscriptionId, subscription);

        sessionSubscriptions
                .computeIfAbsent(request.getSessionId(), key -> new CopyOnWriteArrayList<>())
                .add(subscriptionId);

        log.debug("Subscription stored: {} for {}", subscriptionId, request.getEntityType());
        return subscriptionId;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Subscription<T> getSubscription(UUID subscriptionId) {
        Subscription<?> subscription = findById(subscriptionId);
        subscription.touch();
        return (Subscription<T>) subscription;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void updateSubscription(UUID subscriptionId, SubscriptionRequest<T> request) {
        Subscription<T> subscription = (Subscription<T>) findById(subscriptionId);

        Subscription<T> updatedSubscription = new Subscription<>(
                subscriptionId,
                request.getEntityType(),
                request.getFilter(),
                request.getPage(),
                request.getSize(),
                request.getSort(),
                request.getDirection(),
                request.getSessionId(),
                subscription.getCreatedAt(),
                Instant.now()
        );

        subscriptions.put(subscriptionId, updatedSubscription);
        log.debug("Subscription updated: {}", subscriptionId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<Subscription<T>> getSubscriptionsByType(String entityType) {
        return subscriptions.values().stream()
                .filter(subscription -> entityType.equals(subscription.getEntityType()))
                .map(subscription -> (Subscription<T>) subscription)
                .toList();
    }

    @Override
    public void deleteSubscription(UUID subscriptionId) {
        Subscription<?> removedSubscription = subscriptions.remove(subscriptionId);

        if (removedSubscription != null) {
            String sessionId = removedSubscription.getSessionId();
            List<UUID> sessionSubs = sessionSubscriptions.get(sessionId);
            if (sessionSubs != null) {
                sessionSubs.remove(subscriptionId);
                if (sessionSubs.isEmpty()) {
                    sessionSubscriptions.remove(sessionId);
                }
            }
            log.debug("Subscription deleted: {}", subscriptionId);
        }
    }

    @Override
    public List<UUID> deleteSubscriptionsBySession(String sessionId) {
        List<UUID> sessionSubs = sessionSubscriptions.remove(sessionId);
        if (sessionSubs != null) {
            sessionSubs.forEach(subscriptions::remove);
            log.debug("Deleted {} subscriptions for session: {}", sessionSubs.size(), sessionId);
            return sessionSubs;
        }
        return List.of();
    }

    @Override
    public List<UUID> deleteDeadSubscriptions() {
        Instant cutOff = Instant.now().minus(30, ChronoUnit.MINUTES);
        List<UUID> deadSubscriptionIds = new ArrayList<>();
        subscriptions.entrySet().removeIf(entry -> {
            if (entry.getValue().getLastActivity().isBefore(cutOff)) {
                deadSubscriptionIds.add(entry.getKey());

                // Также удаляем из sessionSubscriptions
                String sessionId = entry.getValue().getSessionId();
                List<UUID> sessionSubs = sessionSubscriptions.get(sessionId);
                if (sessionSubs != null) {
                    sessionSubs.remove(entry.getKey());
                    if (sessionSubs.isEmpty()) {
                        sessionSubscriptions.remove(sessionId);
                    }
                }
                return true;
            }
            return false;
        });
        return deadSubscriptionIds;
    }

}