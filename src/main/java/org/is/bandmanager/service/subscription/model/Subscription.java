package org.is.bandmanager.service.subscription.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.is.bandmanager.repository.filter.EntityFilter;
import org.is.util.pageable.PageableRequest;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@ToString
public class Subscription<T extends EntityFilter> {

    private UUID subscriptionId;

    private String principalId;

    private T filter;

    private PageableRequest pageableRequest;

    private Instant createdAt;

    private Instant touchedAt;

}
