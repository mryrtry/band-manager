package org.is.event;

import lombok.Getter;

import java.time.Instant;
import java.util.List;


@Getter
public class EntityEvent<T> {

    private final EventType eventType;

    private final Class<?> entityType;

    private final List<T> entities;

    private final Instant timestamp;

    public EntityEvent(EventType eventType, T entity) {
        this.eventType = eventType;
        this.entities = List.of(entity);
        this.entityType = entity.getClass();
        this.timestamp = Instant.now();
    }

    @SafeVarargs
    public EntityEvent(EventType eventType, T... entities) {
        this.eventType = eventType;
        this.entities = List.of(entities);
        this.entityType = entities.length > 0 ? entities[0].getClass() : null;
        this.timestamp = Instant.now();
    }

}
