package org.is.exception.message;

import org.springframework.http.HttpStatus;


public interface ErrorMessage {

    String getFormattedMessage(Object... args);

    HttpStatus getHttpStatus();

}