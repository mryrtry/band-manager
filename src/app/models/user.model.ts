export interface User {
  id: number;
  username: string;
  roles: Role[];
  createdAt: string;
  updatedAt: string;
}

export enum Role {
  ROLE_USER = 'ROLE_USER',
  ROLE_ADMIN = 'ROLE_ADMIN'
}

export enum Permission {
  ENTITY_CREATE = 'ENTITY_CREATE',
  ENTITY_READ = 'ENTITY_READ',
  OWN_ENTITY_UPDATE = 'OWN_ENTITY_UPDATE',
  ALL_ENTITY_UPDATE = 'ALL_ENTITY_UPDATE',
  OWN_ENTITY_DELETE = 'OWN_ENTITY_DELETE',
  ALL_ENTITY_DELETE = 'ALL_ENTITY_DELETE',
  ALL_ENTITY_BULK_DELETE = 'ALL_ENTITY_BULK_DELETE',
  OWN_ENTITY_BULK_DELETE = 'OWN_ENTITY_BULK_DELETE',
  ALL_USER_READ = 'ALL_USER_READ',
  ALL_USER_UPDATE = 'ALL_USER_UPDATE',
  ALL_USER_DELETE = 'ALL_USER_DELETE',
  ALL_USER_BULK_DELETE = 'ALL_USER_BULK_DELETE',
  IMPORT_ENTITY = 'IMPORT_ENTITY',
  READ_OWN_IMPORT = 'READ_OWN_IMPORT',
  READ_ALL_IMPORT = 'READ_ALL_IMPORT'
}

export const RolePermissions: { [key in Role]: Permission[] } = {
  [Role.ROLE_USER]: [
    Permission.ENTITY_CREATE,
    Permission.ENTITY_READ,
    Permission.OWN_ENTITY_UPDATE,
    Permission.OWN_ENTITY_DELETE,
    Permission.OWN_ENTITY_BULK_DELETE,
    Permission.IMPORT_ENTITY,
    Permission.READ_OWN_IMPORT
  ],
  [Role.ROLE_ADMIN]: [
    Permission.ALL_ENTITY_UPDATE,
    Permission.ALL_ENTITY_DELETE,
    Permission.ALL_ENTITY_BULK_DELETE,
    Permission.ALL_USER_READ,
    Permission.ALL_USER_UPDATE,
    Permission.ALL_USER_DELETE,
    Permission.ALL_USER_BULK_DELETE,
    Permission.READ_ALL_IMPORT
  ]
};
