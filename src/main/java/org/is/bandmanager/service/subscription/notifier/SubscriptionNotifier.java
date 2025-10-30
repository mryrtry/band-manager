package org.is.bandmanager.service.subscription.notifier;

import org.is.bandmanager.service.subscription.model.Subscription;
import org.springframework.data.domain.Page;

public interface SubscriptionNotifier {

    void notifySubscriptionMeta(Subscription<?> subscription);

    void notifySubscription(Subscription<?> subscription, Page<?> data);

}
