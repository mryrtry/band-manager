package org.is.bandmanager.exception.message;

import org.springframework.http.HttpStatus;


public interface ErrorMessage {

	String getFormattedMessage(Object... args);

	HttpStatus getHttpStatus();

}