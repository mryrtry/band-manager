package org.is.auth.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.is.auth.annotation.Login;
import org.is.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class LoginValidator implements ConstraintValidator<Login, String> {

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        if (!userRepository.existsByUsername(username)) {
            context.buildConstraintViolationWithTemplate("Пользователь с именем '%s' не найден".formatted(username))
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
            return false;
        }
        return true;
    }

}