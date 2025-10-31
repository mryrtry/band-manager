package org.is.bandmanager.service.subscription.storage;

import lombok.RequiredArgsConstructor;
import org.is.bandmanager.constants.SubscriptionsConstants;
import org.is.exception.ServiceException;
import org.is.bandmanager.repository.filter.EntityFilter;
import org.is.bandmanager.service.subscription.model.Subscription;
import org.is.bandmanager.service.subscription.model.request.SubscriptionRequest;
import org.springframework.stereotype.Service;
import org.is.bandmanager.util.pageable.PageableConfig;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static org.is.exception.message.BandManagerErrorMessage.MUST_BE_NOT_NULL;
import static org.is.exception.message.BandManagerErrorMessage.SOURCE_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class InMemorySubscriptionStorage implements SubscriptionStorage {

    private final ConcurrentMap<UUID, Subscription<? extends EntityFilter>> subscriptions = new ConcurrentHashMap<>();
    private final ConcurrentMap<Class<? extends EntityFilter>, Set<UUID>> typeIndex = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Set<UUID>> sessionIndex = new ConcurrentHashMap<>();

    @Override
    public <T extends EntityFilter> Subscription<T> createSubscription(String principalId, SubscriptionRequest<T> request) {
        UUID id = UUID.randomUUID();

        Subscription<T> sub = Subscription.<T>builder()
                .subscriptionId(id)
                .principalId(principalId)
                .filter(request.getFilter())
                .pageableConfig(Optional.ofNullable(request.getPageableConfig()).orElse(new PageableConfig()))
                .createdAt(Instant.now())
                .touchedAt(Instant.now())
                .build();

        subscriptions.put(id, sub);
        typeIndex.computeIfAbsent(request.getFilter().getClass(), k -> ConcurrentHashMap.newKeySet()).add(id);
        sessionIndex.computeIfAbsent(principalId, k -> ConcurrentHashMap.newKeySet()).add(id);

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
                .principalId(existing.getPrincipalId())
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
            sessionIndex.getOrDefault(removed.getPrincipalId(), Collections.emptySet()).remove(subscriptionId);
        }
    }

    @Override
    public List<UUID> deleteAllPrincipalSubscriptions(String sessionId) {
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
        Instant threshold = Instant.now().minusSeconds(SubscriptionsConstants.DEAD_SUBSCRIPTION_LIMIT_SECONDS);
        List<UUID> removed = new ArrayList<>();
        for (Map.Entry<UUID, Subscription<? extends EntityFilter>> entry : subscriptions.entrySet()) {
            if (entry.getValue().getTouchedAt().isBefore(threshold)) {
                UUID id = entry.getKey();
                Subscription<?> sub = subscriptions.remove(id);
                if (sub != null) {
                    typeIndex.getOrDefault(sub.getFilter().getClass(), Collections.emptySet()).remove(id);
                    sessionIndex.getOrDefault(sub.getPrincipalId(), Collections.emptySet()).remove(id);
                    removed.add(id);
                }
            }
        }
        return removed;
    }

}
