package org.is.bandmanager.service.subscription.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.is.bandmanager.repository.filter.EntityFilter;
import org.is.bandmanager.util.pageable.PageableConfig;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionRequest<T extends EntityFilter> {

    private UUID subscriptionId;

    private T filter;

    private PageableConfig pageableConfig;

}
