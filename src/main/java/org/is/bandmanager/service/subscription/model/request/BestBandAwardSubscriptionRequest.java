package org.is.bandmanager.service.subscription.model.request;

import org.is.bandmanager.dto.request.BestBandAwardFilter;
import org.is.bandmanager.service.pageable.PageableConfig;

import java.util.UUID;

public class BestBandAwardSubscriptionRequest extends SubscriptionRequest<BestBandAwardFilter> {

    public BestBandAwardSubscriptionRequest(UUID subscriptionId, BestBandAwardFilter filter, PageableConfig pageableConfig) {
        super(subscriptionId, filter, pageableConfig);
    }

}
