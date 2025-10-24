package org.is.bandmanager.service.subscription.storage;

import org.is.bandmanager.exception.ServiceException;
import org.is.bandmanager.repository.specifications.EntityFilter;
import org.is.bandmanager.service.pageable.PageableConfig;
import org.is.bandmanager.service.subscription.model.Subscription;
import org.is.bandmanager.service.subscription.model.request.SubscriptionRequest;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static org.is.bandmanager.exception.message.ServiceErrorMessage.MUST_BE_NOT_NULL;
import static org.is.bandmanager.exception.message.ServiceErrorMessage.SOURCE_NOT_FOUND;

public class InMemorySubscriptionStorage implements SubscriptionStorage {

    private final ConcurrentMap<UUID, Subscription<? extends EntityFilter>> subscriptions = new ConcurrentHashMap<>();
    private final ConcurrentMap<Class<? extends EntityFilter>, Set<UUID>> typeIndex = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Set<UUID>> sessionIndex = new ConcurrentHashMap<>();

    @Override
    public <T extends EntityFilter> Subscription<T> createSubscription(String sessionId, SubscriptionRequest<T> request) {
        UUID id = UUID.randomUUID();

        Subscription<T> sub = Subscription.<T>builder()
                .subscriptionId(id)
                .sessionId(sessionId)
                .filter(request.getFilter())
                .pageableConfig(Optional.ofNullable(request.getPageableConfig()).orElse(new PageableConfig()))
                .createdAt(Instant.now())
                .touchedAt(Instant.now())
                .build();

        subscriptions.put(id, sub);
        typeIndex.computeIfAbsent(request.getFilter().getClass(), k -> ConcurrentHashMap.newKeySet()).add(id);
        sessionIndex.computeIfAbsent(sessionId, k -> ConcurrentHashMap.newKeySet()).add(id);

        return sub;
    }

    @Override
    public <T extends EntityFilter> Subscription<T> updateSubscription(SubscriptionRequest<T> request) {
        if (request.getSubscriptionId() == null) {
            throw new ServiceException(MUST_BE_NOT_NULL, "Subscription.ID");
        }

        @SuppressWarnings("unchecked")
        Subscription<T> existing = (Subscription<T>) subscriptions.get(request.getSubscriptionId());
        if (existing == null) {
            throw new ServiceException(SOURCE_NOT_FOUND, "Subscription", request.getSubscriptionId());
        }

        if (!existing.getFilter().getClass().equals(request.getFilter().getClass())) {
            typeIndex.get(existing.getFilter().getClass()).remove(existing.getSubscriptionId());
            typeIndex.computeIfAbsent(request.getFilter().getClass(), k -> ConcurrentHashMap.newKeySet()).add(existing.getSubscriptionId());
        }

        Subscription<T> updated = Subscription.<T>builder()
                .subscriptionId(existing.getSubscriptionId())
                .sessionId(existing.getSessionId())
                .filter(request.getFilter())
                .pageableConfig(Optional.ofNullable(request.getPageableConfig()).orElse(new PageableConfig()))
                .createdAt(existing.getCreatedAt())
                .touchedAt(Instant.now())
                .build();

        subscriptions.put(updated.getSubscriptionId(), updated);
        return updated;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends EntityFilter> Optional<Subscription<T>> getSubscription(UUID id) {
        return Optional.ofNullable((Subscription<T>) subscriptions.get(id));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends EntityFilter> List<Subscription<T>> getSubscriptionsByType(Class<T> filterType) {
        Set<UUID> ids = typeIndex.getOrDefault(filterType, Collections.emptySet());
        return ids.stream()
                .map(subscriptions::get)
                .filter(Objects::nonNull)
                .map(s -> (Subscription<T>) s)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteSubscription(UUID subscriptionId) {
        Subscription<?> removed = subscriptions.remove(subscriptionId);
        if (removed != null) {
            typeIndex.getOrDefault(removed.getFilter().getClass(), Collections.emptySet()).remove(subscriptionId);
            sessionIndex.getOrDefault(removed.getSessionId(), Collections.emptySet()).remove(subscriptionId);
        }
    }

    @Override
    public List<UUID> deleteSessionSubscriptions(String sessionId) {
        Set<UUID> ids = sessionIndex.remove(sessionId);
        if (ids == null) return List.of();
        List<UUID> deleted = new ArrayList<>();
        for (UUID id : ids) {
            Subscription<?> sub = subscriptions.remove(id);
            if (sub != null) {
                typeIndex.getOrDefault(sub.getFilter().getClass(), Collections.emptySet()).remove(id);
                deleted.add(id);
            }
        }
        return deleted;
    }

    @Override
    public List<UUID> deleteDeadSubscriptions() {
        Instant threshold = Instant.now().minusSeconds(3600);
        List<UUID> removed = new ArrayList<>();
        for (Map.Entry<UUID, Subscription<? extends EntityFilter>> entry : subscriptions.entrySet()) {
            if (entry.getValue().getTouchedAt().isBefore(threshold)) {
                UUID id = entry.getKey();
                Subscription<?> sub = subscriptions.remove(id);
                if (sub != null) {
                    typeIndex.getOrDefault(sub.getFilter().getClass(), Collections.emptySet()).remove(id);
                    sessionIndex.getOrDefault(sub.getSessionId(), Collections.emptySet()).remove(id);
                    removed.add(id);
                }
            }
        }
        return removed;
    }

}
