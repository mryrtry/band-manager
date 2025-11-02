package org.is.bandmanager.service.subscription.storage;

import org.is.util.pageable.EntityFilter;
import org.is.bandmanager.service.subscription.model.Subscription;
import org.is.bandmanager.service.subscription.model.request.SubscriptionRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionStorage {

    <T extends EntityFilter> Subscription<T> createSubscription(String principalId, SubscriptionRequest<T> subscriptionRequest);

    <T extends EntityFilter> Subscription<T> updateSubscription(SubscriptionRequest<T> subscriptionRequest);

    <T extends EntityFilter> Optional<Subscription<T>> getSubscription(UUID id);

    <T extends EntityFilter> List<Subscription<T>> getSubscriptionsByType(Class<T> filterType);

    void deleteSubscription(UUID subscriptionId);

    List<UUID> deleteAllPrincipalSubscriptions(String principalId);

    List<UUID> deleteDeadSubscriptions();

}
