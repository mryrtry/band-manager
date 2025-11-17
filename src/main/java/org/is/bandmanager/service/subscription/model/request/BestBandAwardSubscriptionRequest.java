package org.is.bandmanager.service.subscription.model.request;

import org.is.bandmanager.repository.filter.BestBandAwardFilter;
import org.is.util.pageable.PageableRequest;

import java.util.UUID;

public class BestBandAwardSubscriptionRequest extends SubscriptionRequest<BestBandAwardFilter> {

    public BestBandAwardSubscriptionRequest(UUID subscriptionId, BestBandAwardFilter filter, PageableRequest pageableRequest) {
        super(subscriptionId, filter, pageableRequest);
    }

}
