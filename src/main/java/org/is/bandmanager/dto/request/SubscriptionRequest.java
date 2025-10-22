package org.is.bandmanager.dto.request;

import jakarta.validation.Valid;
import org.is.bandmanager.repository.filter.EntityFilter;

public class SubscriptionRequest {



    @Valid
    EntityFilter filter;

}
