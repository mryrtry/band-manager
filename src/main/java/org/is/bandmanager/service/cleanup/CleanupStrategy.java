package org.is.bandmanager.service.cleanup;

import java.util.List;

public interface CleanupStrategy<T, D> {

    List<T> findUnusedEntities();

    void deleteEntities(List<T> entities);

    List<D> convertToDto(List<T> entities);

    String getEntityName();

}