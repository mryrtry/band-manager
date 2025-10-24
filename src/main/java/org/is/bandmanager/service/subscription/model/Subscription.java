package org.is.bandmanager.service.subscription.model;

import lombok.Builder;
import lombok.Getter;
import org.is.bandmanager.repository.specifications.EntityFilter;
import org.is.bandmanager.service.pageable.PageableConfig;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class Subscription<T extends EntityFilter> {

    private UUID subscriptionId;

    private String sessionId;

    private T filter;

    private PageableConfig pageableConfig;

    private Instant createdAt;

    private Instant touchedAt;

}
