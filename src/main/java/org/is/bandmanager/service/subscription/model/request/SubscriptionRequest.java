package org.is.bandmanager.service.subscription.model.request;

import lombok.*;
import org.is.bandmanager.repository.specifications.EntityFilter;
import org.is.bandmanager.service.pageable.PageableConfig;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionRequest<T extends EntityFilter> {

    private UUID subscriptionId;

    private T filter;

    private PageableConfig pageableConfig;

}
