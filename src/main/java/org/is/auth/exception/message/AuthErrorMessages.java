package org.is.auth.exception.message;

import lombok.Getter;
import org.is.exception.ErrorMessage;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthErrorMessages implements ErrorMessage {

    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Некорректный токен типа '%s', проверьте токен"),
    INCORRECT_PASSWORD(HttpStatus.UNAUTHORIZED, "Неверный пароль, попробуйте ещё раз"),
    USER_NOT_AUTHENTICATED(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован"),
    TOKEN_BLACKLISTED(HttpStatus.UNAUTHORIZED, "Токен в черном списке");


    private final HttpStatus httpStatus;

    private final String messageTemplate;

    AuthErrorMessages(HttpStatus httpStatus, String messageTemplate) {
        this.httpStatus = httpStatus;
        this.messageTemplate = messageTemplate;
    }

    @Override
    public String getFormattedMessage(Object... args) {
        return messageTemplate.formatted(args);
    }

}
