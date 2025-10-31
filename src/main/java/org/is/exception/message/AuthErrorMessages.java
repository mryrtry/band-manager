package org.is.exception.message;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthErrorMessages implements ErrorMessage {

    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Некорректный токен типа '%s', проверьте токен");


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
