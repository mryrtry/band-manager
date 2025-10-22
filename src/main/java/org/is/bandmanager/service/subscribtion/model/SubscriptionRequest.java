package org.is.bandmanager.service.subscribtion.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.is.bandmanager.repository.filter.EntityFilter;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public final class SubscriptionRequest<T> {

    private final UUID subscriptionId;

    private final String entityType;

    @Valid
    private final EntityFilter filter;

    private final int page;

    private final int size;

    private final List<String> sort;

    @NotBlank
    private final String direction;

    @NotBlank
    private String sessionId;

}
