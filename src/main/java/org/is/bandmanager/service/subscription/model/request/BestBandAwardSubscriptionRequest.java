package org.is.bandmanager.service.subscription.model.request;

import org.is.bandmanager.repository.filter.BestBandAwardFilter;
import org.is.bandmanager.util.pageable.PageableConfig;

import java.util.UUID;

public class BestBandAwardSubscriptionRequest extends SubscriptionRequest<BestBandAwardFilter> {

    public BestBandAwardSubscriptionRequest(UUID subscriptionId, BestBandAwardFilter filter, PageableConfig pageableConfig) {
        super(subscriptionId, filter, pageableConfig);
    }

}
