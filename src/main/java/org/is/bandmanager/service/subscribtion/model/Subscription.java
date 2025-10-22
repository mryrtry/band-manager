package org.is.bandmanager.service.subscribtion.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.is.bandmanager.repository.filter.EntityFilter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class Subscription<T> {

    private final UUID id;

    private final Class<T> entityType;

    private final EntityFilter filter;

    private final int page;

    private final int size;

    private final List<String> sort;

    private final String direction;

    private final String sessionId;

    private final Instant createdAt;

    private Instant lastActivity;

    public void touch() {
        this.lastActivity = Instant.now();
    }

}