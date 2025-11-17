package org.is.bandmanager.config;

import org.is.bandmanager.service.imports.model.ImportOperation;
import org.is.util.pageable.sort.SortConfig;
import org.springframework.context.annotation.Configuration;

import java.util.Set;


@Configuration
public class ImportOperationSortConfig implements SortConfig {

    @Override
    public Class<?> getEntityClass() {
        return ImportOperation.class;
    }

    @Override
    public Set<String> getAllowedSortFields() {
        return Set.of("id", "filename", "createdEntitiesCount", "errorMessage", "startedAt", "completedAt", "status", "user.username");
    }

    @Override
    public String getDefaultSortField() {
        return "id";
    }

}
