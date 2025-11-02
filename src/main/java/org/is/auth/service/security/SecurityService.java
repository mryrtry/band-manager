package org.is.auth.service.security;

public interface SecurityService {

    boolean hasPermission(String permission);

    boolean hasAnyPermission(String... permissions);

    boolean hasAllPermissions(String... permissions);

    boolean canCreate();

    boolean canReadEntity();

    boolean canUpdateEntity(Long entityId, String entityType);

    boolean canDeleteEntity(Long entityId, String entityType);

    boolean canBulkDelete(String entityType);

}
