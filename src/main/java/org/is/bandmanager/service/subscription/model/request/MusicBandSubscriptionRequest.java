package org.is.bandmanager.service.subscription.model.request;

import org.is.bandmanager.repository.filter.MusicBandFilter;
import org.is.util.pageable.PageableRequest;

import java.util.UUID;

public class MusicBandSubscriptionRequest extends SubscriptionRequest<MusicBandFilter> {

    public MusicBandSubscriptionRequest(UUID subscriptionId, MusicBandFilter filter, PageableRequest pageableRequest) {
        super(subscriptionId, filter, pageableRequest);
    }

}
