package org.is.auth.model;

import lombok.Getter;

import java.util.Set;

@Getter
public enum Role {
    ROLE_USER(Set.of(
            Permission.ENTITY_CREATE,
            Permission.ENTITY_READ,
            Permission.OWN_ENTITY_UPDATE,
            Permission.OWN_ENTITY_DELETE,
            Permission.OWN_ENTITY_BULK_DELETE,
            Permission.IMPORT_ENTITY,
            Permission.READ_OWN_IMPORT
    )),
    ROLE_ADMIN(Set.of(
            Permission.ALL_ENTITY_UPDATE,
            Permission.ALL_ENTITY_DELETE,
            Permission.ALL_ENTITY_BULK_DELETE,
            Permission.ALL_USER_READ,
            Permission.ALL_USER_UPDATE,
            Permission.ALL_USER_DELETE,
            Permission.ALL_USER_BULK_DELETE,
            Permission.READ_ALL_IMPORT
    ));

    private final Set<Permission> permissions;

    Role(Set<Permission> permissions) {
        this.permissions = permissions;
    }

}
