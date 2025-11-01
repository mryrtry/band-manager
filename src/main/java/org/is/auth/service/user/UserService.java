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

    UserDto get(String username);

    UserDto getAuthenticatedUser();

    User getEntity(Long id);

    UserDto update(Long id, @Valid UserRequest request);

    UserDto delete(Long id);

}