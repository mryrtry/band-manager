package org.is.bandmanager.controller;

import org.is.bandmanager.exception.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.lang.reflect.Field;
import java.util.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static List<String> getFieldOrder(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .map(Field::getName)
                .toList();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        List<String> order = getFieldOrder(Objects.requireNonNull(ex.getBindingResult().getTarget()).getClass());
        ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .sorted(Comparator.comparingInt(e -> order.indexOf(e.getField())))
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<Map<String, String>> handleOtherExceptions(ServiceException ex) {
        Map<String, String> error = new LinkedHashMap<>();
        error.put("error", ex.getMessage());
        return new ResponseEntity<>(error, ex.getHttpStatus());
    }

}
