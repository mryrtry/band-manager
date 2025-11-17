package org.is.bandmanager.config;

import org.is.bandmanager.model.BestBandAward;
import org.is.util.pageable.sort.SortConfig;
import org.springframework.context.annotation.Configuration;

import java.util.Set;


@Configuration
public class BestBandAwardConfig implements SortConfig {

    @Override
    public Class<?> getEntityClass() {
        return BestBandAward.class;
    }

    @Override
    public Set<String> getAllowedSortFields() {
        return Set.of("id", "band.name", "band.id", "genre", "createdAt");
    }

    @Override
    public String getDefaultSortField() {
        return "id";
    }

}
