package org.is.exception.message;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthErrorMessages implements ErrorMessage {

    MUST_BE_NOT_NULL(HttpStatus.BAD_REQUEST, "Ресурс '%s' не может быть пустым"),
    ID_MUST_BE_POSITIVE(HttpStatus.BAD_REQUEST, "Идентификатор ресурса '%s' должен быть положительным"),
    SOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Ресурс '%s' с ID: '%s' не был найден"),
    CANNOT_ACCESS_SOURCE(HttpStatus.UNAUTHORIZED, "Ресурс '%s' с ID: '%s' не может быть удалён или изменён вами"),
    CANNOT_REMOVE_LAST_PARTICIPANT(HttpStatus.BAD_REQUEST, "Невозможно удалить участника - в группе должен остаться хотя бы 1 участник"),
    ENTITY_IN_USE(HttpStatus.BAD_REQUEST, "Невозможно удалить ресурс '%s' с ID: '%s', так как он связан с другим ресурсом '%s'");


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
