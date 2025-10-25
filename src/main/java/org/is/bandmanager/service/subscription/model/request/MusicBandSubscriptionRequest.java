package org.is.bandmanager.service.subscription.model.request;

import org.is.bandmanager.dto.request.MusicBandFilter;
import org.is.bandmanager.service.pageable.PageableConfig;

import java.util.UUID;

public class MusicBandSubscriptionRequest extends SubscriptionRequest<MusicBandFilter> {

    public MusicBandSubscriptionRequest(UUID subscriptionId, MusicBandFilter filter, PageableConfig pageableConfig) {
        super(subscriptionId, filter, pageableConfig);
    }

}
