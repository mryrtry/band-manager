package org.is.bandmanager.service.subscribtion.storage;

import org.is.bandmanager.service.subscribtion.model.Subscription;
import org.is.bandmanager.service.subscribtion.model.SubscriptionRequest;

import java.util.List;
import java.util.UUID;

public interface SubscriptionManager {

    <T> UUID createSubscription(SubscriptionRequest<T> request);

    <T> Subscription<T> getSubscription(UUID subscriptionId);

    <T> List<Subscription<T>> getSubscriptionsByType(String entityType);

    <T> void updateSubscription(UUID id, SubscriptionRequest<T> request);

    void deleteSubscription(UUID id);

    List<UUID> deleteSubscriptionsBySession(String sessionId);

    List<UUID> deleteDeadSubscriptions();

}
