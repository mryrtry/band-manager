package org.is.auth.service.user;

import lombok.RequiredArgsConstructor;
import org.is.auth.dto.UserDto;
import org.is.auth.dto.UserMapper;
import org.is.auth.dto.request.LoginRequest;
import org.is.auth.dto.request.RoleRequest;
import org.is.auth.dto.request.UserRequest;
import org.is.auth.model.Role;
import org.is.auth.model.User;
import org.is.auth.model.UserDetailsImpl;
import org.is.auth.repository.UserRepository;
import org.is.auth.repository.filter.UserFilter;
import org.is.event.EntityEvent;
import org.is.exception.ServiceException;
import org.is.util.pageable.PageableConfig;
import org.is.util.pageable.PageableCreator;
import org.is.util.pageable.PageableType;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.is.auth.exception.message.AuthErrorMessages.INCORRECT_PASSWORD;
import static org.is.auth.exception.message.AuthErrorMessages.USER_NOT_AUTHENTICATED;
import static org.is.bandmanager.exception.message.BandManagerErrorMessage.ID_MUST_BE_POSITIVE;
import static org.is.bandmanager.exception.message.BandManagerErrorMessage.MUST_BE_NOT_NULL;
import static org.is.bandmanager.exception.message.BandManagerErrorMessage.SOURCE_WITH_ID_NOT_FOUND;
import static org.is.event.EventType.CREATED;
import static org.is.event.EventType.DELETED;
import static org.is.event.EventType.UPDATED;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserMapper mapper;

    private final ApplicationEventPublisher eventPublisher;

    private User findUser(Long id) {
        if (id == null) {
            throw new ServiceException(MUST_BE_NOT_NULL, "User.id");
        }
        if (id <= 0) {
            throw new ServiceException(ID_MUST_BE_POSITIVE, "User.id");
        }
        return userRepository.findById(id).orElseThrow(() ->
                new ServiceException(SOURCE_WITH_ID_NOT_FOUND, "User", id));
    }

    private User findUser(String username) {
        if (username == null || username.isBlank()) {
            throw new ServiceException(MUST_BE_NOT_NULL, "User.username");
        }
        return userRepository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException(SOURCE_WITH_ID_NOT_FOUND.getFormattedMessage("User", username))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return new UserDetailsImpl(findUser(username));
    }

    @Override
    @Transactional
    public UserDto create(UserRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Set.of(Role.ROLE_USER));
        UserDto createdUser = mapper.toDto(userRepository.save(user));
        eventPublisher.publishEvent(new EntityEvent<>(CREATED, createdUser));
        return createdUser;
    }

    @Override
    public Page<UserDto> getAll(UserFilter filter, PageableConfig config) {
        Pageable pageable = PageableCreator.create(config, PageableType.USERS);
        Page<User> users = userRepository.findWithFilter(filter, pageable);
        return users.map(mapper::toDto);
    }

    @Override
    public UserDto get(Long id) {
        return mapper.toDto(findUser(id));
    }

    @Override
    public UserDto get(String username) {
        return mapper.toDto(findUser(username));
    }

    @Override
    public UserDto getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            throw new ServiceException(USER_NOT_AUTHENTICATED);
        }
        String username = authentication.getName();
        User user = findUser(username);
        return mapper.toDto(user);
    }

    @Override
    public User getEntity(Long id) {
        return findUser(id);
    }

    @Override
    @Transactional
    public UserDto update(Long id, UserRequest request) {
        User updatingUser = findUser(id);
        updatingUser.setUsername(request.getUsername());
        updatingUser.setPassword(passwordEncoder.encode(request.getPassword()));
        UserDto updatedUser = mapper.toDto(userRepository.save(updatingUser));
        eventPublisher.publishEvent(new EntityEvent<>(UPDATED, updatedUser));
        return updatedUser;
    }

    @Override
    @Transactional
    public UserDto updateRoles(Long id, RoleRequest request) {
        User updatingUser = findUser(id);
        updatingUser.setRoles(request.getRoles());
        UserDto updatedUser = mapper.toDto(userRepository.save(updatingUser));
        eventPublisher.publishEvent(new EntityEvent<>(UPDATED, updatedUser));
        return updatedUser;
    }

    @Override
    @Transactional
    public UserDto delete(Long id) {
        User deletingUser = findUser(id);
        userRepository.delete(deletingUser);
        UserDto deletedUser = mapper.toDto(deletingUser);
        eventPublisher.publishEvent(new EntityEvent<>(DELETED, deletingUser));
        return deletedUser;
    }

    @Override
    public boolean validateLogin(LoginRequest loginRequest) {
        User user = findUser(loginRequest.getUsername());
        if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) return true;
        throw new ServiceException(INCORRECT_PASSWORD);
    }

}
