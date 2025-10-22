package org.is.bandmanager.service.subscribtion;

import org.is.bandmanager.service.subscribtion.model.SubscriptionRequest;

import java.util.UUID;

public interface SubscriptionService {

    <T> UUID createSubscription(SubscriptionRequest<T> request);

    <T> UUID updateSubscription(SubscriptionRequest<T> request);

    void cancelSubscription(UUID subscriptionId);

    void cancelSessionSubscriptions(String sessionId);

}