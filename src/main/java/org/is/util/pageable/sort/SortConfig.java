package org.is.util.pageable.sort;

import java.util.Set;

public interface SortConfig {

    Class<?> getEntityClass();

    Set<String> getAllowedSortFields();

    String getDefaultSortField();

}
