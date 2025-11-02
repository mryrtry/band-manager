package org.is.exception;

import org.springframework.http.HttpStatus;


public interface ErrorMessage {

    String getFormattedMessage(Object... args);

    HttpStatus getHttpStatus();

}