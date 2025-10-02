package org.is.bandmanager.exception.message;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ServiceErrorMessage implements ErrorMessage {

    MUST_BE_NOT_NULL(HttpStatus.BAD_REQUEST, "Ресурс '%s' не может быть пустым"),
    SOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Ресурс '%s' с ID: '%d' не был найден");

    private final HttpStatus httpStatus;

    private final String messageTemplate;

    ServiceErrorMessage(HttpStatus httpStatus, String messageTemplate) {
        this.httpStatus = httpStatus;
        this.messageTemplate = messageTemplate;
    }

    @Override
    public String getFormattedMessage(Object... args) {
        return messageTemplate.formatted(args);
    }

}
