package org.is.bandmanager.service.subscription;

import org.is.bandmanager.event.EntityEvent;
import org.is.bandmanager.service.subscription.model.request.SubscriptionRequest;

import java.util.UUID;

public interface SubscriptionService {

    void createSubscription(String principalId, SubscriptionRequest<?> subscriptionRequest);

    void updateSubscription(String principalId, SubscriptionRequest<?> subscriptionRequest);

    void cancelSubscription(String principalId, UUID subscriptionId);

    void cancelAllPrincipalSubscriptions(String principalId);

    void cancelDeadSubscriptions();

    void handleEntityEvent(EntityEvent<?> entityEvent);

    void notifySubscription(UUID subscriptionId);

}
