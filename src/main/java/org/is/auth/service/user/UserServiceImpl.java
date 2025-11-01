package org.is.auth.service.user;

import lombok.RequiredArgsConstructor;
import org.is.auth.dto.UserDto;
import org.is.auth.dto.request.UserRequest;
import org.is.auth.model.User;
import org.is.auth.model.UserDetailsImpl;
import org.is.auth.repository.UserRepository;
import org.is.exception.ServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.is.exception.message.BandManagerErrorMessage.ID_MUST_BE_POSITIVE;
import static org.is.exception.message.BandManagerErrorMessage.MUST_BE_NOT_NULL;
import static org.is.exception.message.BandManagerErrorMessage.SOURCE_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private User findUser(Long id) {
        if (id == null) {
            throw new ServiceException(MUST_BE_NOT_NULL, "User.id");
        }
        if (id <= 0) {
            throw new ServiceException(ID_MUST_BE_POSITIVE, "User.id");
        }
        return userRepository.findById(id).orElseThrow(() ->
                new ServiceException(SOURCE_NOT_FOUND, "User", id));
    }

    private User findUser(String username) {
        if (username.isBlank()) {
            throw new ServiceException(MUST_BE_NOT_NULL, "User.username");
        }
        return userRepository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException(SOURCE_NOT_FOUND.getFormattedMessage("User", username))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return new UserDetailsImpl(findUser(username));
    }

    @Override
    public UserDto create(UserRequest request) {
        return null;
    }

    @Override
    public List<UserDto> getAll() {
        return List.of();
    }

    @Override
    public UserDto get(Long id) {
        return null;
    }

    @Override
    public User getEntity(Long id) {
        return null;
    }

    @Override
    public UserDto update(Long id, UserRequest request) {
        return null;
    }

    @Override
    public UserDto delete(Long id) {
        return null;
    }

    @Override
    public UserDto getByUsername(String username) {
        return null;
    }

    @Override
    public User getEntityByUsername(String username) {
        return null;
    }

    @Override
    public UserDto activate(Long id) {
        return null;
    }

    @Override
    public UserDto deactivate(Long id) {
        return null;
    }

    @Override
    public UserDto updateRoles(Long id, List<String> roles) {
        return null;
    }

    @Override
    public boolean existsByUsername(String username) {
        return false;
    }

    @Override
    public UserDto changePassword(Long id, String newPassword) {
        return null;
    }

    @Override
    public boolean isUsernameAvailable(String username) {
        return false;
    }

}
