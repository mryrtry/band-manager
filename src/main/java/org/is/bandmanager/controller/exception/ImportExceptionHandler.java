package org.is.bandmanager.controller.exception;

import lombok.extern.slf4j.Slf4j;
import org.is.bandmanager.controller.ImportController;
import org.is.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice(assignableTypes = ImportController.class)
public class ImportExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Import validation error: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder()
                        .message(e.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleImportError(RuntimeException e) {
        log.error("Import processing error: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .message("Import processing failed: " + e.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

}