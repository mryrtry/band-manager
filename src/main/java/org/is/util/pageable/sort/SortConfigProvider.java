package org.is.util.pageable.sort;

public interface SortConfigProvider {

    SortConfig getSortConfig(Class<?> entityClass);

}
