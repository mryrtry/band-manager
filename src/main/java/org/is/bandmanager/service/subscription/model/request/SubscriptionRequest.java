package org.is.bandmanager.service.subscription.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.is.util.pageable.EntityFilter;
import org.is.util.pageable.PageableConfig;

import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionRequest<T extends EntityFilter> {

    private UUID subscriptionId;

    private T filter;

    private PageableConfig pageableConfig;

}
