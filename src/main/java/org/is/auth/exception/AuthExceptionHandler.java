package org.is.auth.exception;

import org.is.auth.controller.AuthController;
import org.is.exception.ErrorResponse;
import org.is.util.fieldOrder.ClassFieldOrderUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice(basePackageClasses = AuthController.class)
public class AuthExceptionHandler {

    @ExceptionHandler(AuthCredNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(AuthCredNotValidException ex) {
        List<ErrorResponse.ErrorDetail> errorDetailList = ex.getDetails().entrySet().stream()
                .map(entry -> ErrorResponse.ErrorDetail.builder()
                        .field(entry.getKey())
                        .message(entry.getValue())
                        .rejectedValue(null)
                        .errorType("VALIDATION_ERROR")
                        .build())
                .toList();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .message(ex.getMessage())
                .details(errorDetailList)
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> fieldOrder = ClassFieldOrderUtil.getFieldOrderFromClass(ex.getParameter());

        List<ErrorResponse.ErrorDetail> errorDetails = ex.getBindingResult().getFieldErrors().stream().sorted(Comparator.comparing(error -> {
            int index = fieldOrder.indexOf(error.getField());
            return index != -1 ? index : Integer.MAX_VALUE;
        })).map(error -> ErrorResponse.ErrorDetail.builder().field(error.getField()).message(error.getDefaultMessage()).rejectedValue(error.getRejectedValue()).errorType("VALIDATION_ERROR").build()).collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.builder().status(HttpStatus.UNAUTHORIZED.value()).message("Ошибка валидации данных").details(errorDetails).timestamp(LocalDateTime.now()).build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

}
