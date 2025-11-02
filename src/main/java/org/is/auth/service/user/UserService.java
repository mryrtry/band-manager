package org.is.auth.service.user;

import jakarta.validation.Valid;
import org.is.auth.dto.UserDto;
import org.is.auth.dto.request.LoginRequest;
import org.is.auth.dto.request.RoleRequest;
import org.is.auth.dto.request.UserRequest;
import org.is.auth.model.Permission;
import org.is.auth.model.User;
import org.is.auth.repository.filter.UserFilter;
import org.is.util.pageable.PageableConfig;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    UserDto create(@Valid UserRequest request);

    Page<UserDto> getAll(UserFilter filter, PageableConfig config);

    UserDto get(Long id);

    UserDto get(String username);

    UserDto getAuthenticatedUser();

    User getEntity(Long id);

    UserDto update(Long id, @Valid UserRequest request);

    UserDto updateRoles(Long id, @Valid RoleRequest request);

    UserDto delete(Long id);

    boolean validateLogin(@Valid LoginRequest loginRequest);

    boolean authenticatedUserHasPermission(Permission permission);

    boolean authenticatedUserHasPermission(Permission... permissions);

}