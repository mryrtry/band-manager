package org.is.auth.service.security;

import lombok.RequiredArgsConstructor;
import org.is.auth.model.Permission;
import org.is.auth.service.user.UserService;
import org.is.bandmanager.model.AuditableEntity;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service("securityService")
@RequiredArgsConstructor
public class SecurityServiceImpl implements SecurityService {

    private final UserService userService;
    private final ApplicationContext applicationContext;

    public boolean hasPermission(String permission) {
        try {
            Permission perm = Permission.valueOf(permission);
            return userService.authenticatedUserHasPermission(perm);
        } catch (IllegalArgumentException e) {
            throw new AccessDeniedException("Unknown permission: " + permission);
        }
    }

    public boolean hasAnyPermission(String... permissions) {
        return Arrays.stream(permissions)
                .anyMatch(this::hasPermission);
    }

    public boolean hasAllPermissions(String... permissions) {
        return Arrays.stream(permissions)
                .allMatch(this::hasPermission);
    }

    public boolean canCreate() {
        return hasPermission("ENTITY_CREATE");
    }

    public boolean canReadEntity() {
        return hasPermission("ENTITY_READ");
    }

    public boolean canUpdateEntity(Long entityId, String entityType) {
        return hasPermission("ALL_ENTITY_UPDATE") ||
                (hasPermission("OWN_ENTITY_UPDATE") && isOwnerOrSystem(entityId, entityType));
    }

    public boolean canDeleteEntity(Long entityId, String entityType) {
        return hasPermission("ALL_ENTITY_DELETE") ||
                (hasPermission("OWN_ENTITY_DELETE") && isOwnerOrSystem(entityId, entityType));
    }

    public boolean canBulkDelete(String entityType) {
        return hasPermission("ALL_ENTITY_BULK_DELETE") ||
                hasPermission("OWN_ENTITY_BULK_DELETE");
    }

    private boolean isOwnerOrSystem(Long entityId, String entityType) {
        AuditableEntity entity = getEntity(entityId, entityType);
        if (entity == null) return true;
        String currentUsername = userService.getAuthenticatedUser().getUsername();
        String entityOwner = entity.getCreatedBy();

        return entityOwner == null || "system".equals(entityOwner) || currentUsername.equals(entityOwner);
    }

    private AuditableEntity getEntity(Long entityId, String entityType) {
        JpaRepository<?, Long> repository = getRepository(entityType);
        return (AuditableEntity) repository.findById(entityId).orElse(null);
    }

    @SuppressWarnings("unchecked")
    private JpaRepository<?, Long> getRepository(String entityType) {
        String repositoryBeanName = entityType + "Repository";
        try {
            return (JpaRepository<?, Long>) applicationContext.getBean(repositoryBeanName);
        } catch (Exception e) {
            throw new AccessDeniedException("Repository not found for entity: " + entityType);
        }
    }

}