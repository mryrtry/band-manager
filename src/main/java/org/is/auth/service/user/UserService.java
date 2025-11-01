package org.is.auth.service.user;

import jakarta.validation.Valid;
import org.is.auth.dto.UserDto;
import org.is.auth.dto.request.UserRequest;
import org.is.auth.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {

    UserDto create(@Valid UserRequest request);

    List<UserDto> getAll();

    UserDto get(Long id);

    User getEntity(Long id);

    UserDto update(Long id, @Valid UserRequest request);

    UserDto delete(Long id);

    UserDto getByUsername(String username);

    User getEntityByUsername(String username);

    UserDto activate(Long id);

    UserDto deactivate(Long id);

    UserDto updateRoles(Long id, List<String> roles);

    boolean existsByUsername(String username);

    UserDto changePassword(Long id, String newPassword);

    boolean isUsernameAvailable(String username);

}