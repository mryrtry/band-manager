package org.is.auth.dto;

public record ErrorResponse(int status, String error, String message, long timestamp) {
}
