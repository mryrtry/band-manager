package org.is.auth.model;

import lombok.Getter;

import java.util.Set;

@Getter
public enum Role {
    ROLE_USER(Set.of(
            Permission.ENTITY_CREATE,
            Permission.OWN_ENTITY_READ,
            Permission.ALL_ENTITY_READ,
            Permission.OWN_ENTITY_UPDATE,
            Permission.OWN_ENTITY_DELETE,
            Permission.OWN_ENTITY_BULK_DELETE
    )),
    ROLE_ADMIN(Set.of(
            Permission.ENTITY_CREATE,
            Permission.OWN_ENTITY_READ,
            Permission.ALL_ENTITY_READ,
            Permission.OWN_ENTITY_UPDATE,
            Permission.ALL_ENTITY_UPDATE,
            Permission.OWN_ENTITY_DELETE,
            Permission.ALL_ENTITY_DELETE,
            Permission.OWN_ENTITY_BULK_DELETE,
            Permission.ALL_ENTITY_BULK_DELETE
    ));

    private final Set<Permission> permissions;

    Role(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }

}
