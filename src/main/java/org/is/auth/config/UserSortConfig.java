package org.is.auth.config;

import org.is.auth.model.User;
import org.is.util.pageable.sort.SortConfig;
import org.springframework.context.annotation.Configuration;

import java.util.Set;


@Configuration
public class UserSortConfig implements SortConfig {

    @Override
    public Class<?> getEntityClass() {
        return User.class;
    }

    @Override
    public Set<String> getAllowedSortFields() {
        return Set.of("id", "username", "createdAt", "updatedAt");
    }

    @Override
    public String getDefaultSortField() {
        return "id";
    }

}
