package org.is.bandmanager.controller;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.is.bandmanager.exception.ErrorResponse;
import org.is.bandmanager.exception.ServiceException;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
		List<String> fieldOrder = getFieldOrderFromClass(ex.getParameter());

		List<ErrorResponse.ErrorDetail> errorDetails = ex.getBindingResult().getFieldErrors().stream().sorted(Comparator.comparing(error -> {
			int index = fieldOrder.indexOf(error.getField());
			return index != -1 ? index : Integer.MAX_VALUE;
		})).map(error -> ErrorResponse.ErrorDetail.builder().field(error.getField()).message(error.getDefaultMessage()).rejectedValue(error.getRejectedValue()).errorType("VALIDATION_ERROR").build()).collect(Collectors.toList());

		ErrorResponse errorResponse = ErrorResponse.builder().status(HttpStatus.BAD_REQUEST.value()).message("Ошибка валидации данных").details(errorDetails).timestamp(LocalDateTime.now()).build();

		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}


	@ExceptionHandler(ServiceException.class)
	public ResponseEntity<ErrorResponse> handleServiceException(ServiceException ex) {
		List<ErrorResponse.ErrorDetail> errorDetails = Collections.singletonList(ErrorResponse.ErrorDetail.builder().field("service").message(ex.getMessage()).rejectedValue(null).errorType("SERVICE_ERROR").build());

		ErrorResponse errorResponse = ErrorResponse.builder().status(ex.getHttpStatus().value()).message("Ошибка выполнения операции").details(errorDetails).timestamp(LocalDateTime.now()).build();

		return new ResponseEntity<>(errorResponse, ex.getHttpStatus());
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
		List<ErrorResponse.ErrorDetail> errorDetails = Collections.singletonList(ErrorResponse.ErrorDetail.builder().field(ex.getName()).message(String.format("Некорректный тип параметра. Ожидается: %s", ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown")).rejectedValue(ex.getValue()).errorType("TYPE_MISMATCH").build());

		ErrorResponse errorResponse = ErrorResponse.builder().status(HttpStatus.BAD_REQUEST.value()).message("Ошибка в параметрах запроса").details(errorDetails).timestamp(LocalDateTime.now()).build();

		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ErrorResponse> handleMissingParams(MissingServletRequestParameterException ex) {
		List<ErrorResponse.ErrorDetail> errorDetails = Collections.singletonList(ErrorResponse.ErrorDetail.builder().field(ex.getParameterName()).message("Обязательный параметр отсутствует").rejectedValue(null).errorType("MISSING_PARAMETER").build());

		ErrorResponse errorResponse = ErrorResponse.builder().status(HttpStatus.BAD_REQUEST.value()).message("Отсутствуют обязательные параметры").details(errorDetails).timestamp(LocalDateTime.now()).build();

		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
		List<ErrorResponse.ErrorDetail> errorDetails = Collections.singletonList(ErrorResponse.ErrorDetail.builder().field("system").message("Внутренняя ошибка сервера").rejectedValue(ex.getMessage()).errorType("INTERNAL_ERROR").build());

		ErrorResponse errorResponse = ErrorResponse.builder().status(HttpStatus.INTERNAL_SERVER_ERROR.value()).message("Произошла непредвиденная ошибка").details(errorDetails).timestamp(LocalDateTime.now()).build();

		return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
		String message = "Некорректный формат JSON";
		String details = "Отсутствует или неверный формат тела запроса";

		Throwable cause = ex.getCause();

		if (cause instanceof JsonParseException) {
			message = "Синтаксическая ошибка в JSON";
			details = "Неверный формат JSON";

		} else if (cause instanceof InvalidFormatException invalidFormat) {
			message = "Неверный формат данных";
			details = String.format("Поле '%s': неверный формат. Ожидается: %s", invalidFormat.getPath().get(0).getFieldName(), invalidFormat.getTargetType().getSimpleName());

		} else if (cause instanceof JsonMappingException) {
			message = "Ошибка маппинга JSON";
			details = "Не удалось преобразовать JSON в объект";

		} else if (ex.getMessage().contains("Required request body is missing")) {
			message = "Отсутствует тело запроса";
			details = "Запрос должен содержать тело в формате JSON";
		}

		List<ErrorResponse.ErrorDetail> errorDetails = Collections.singletonList(ErrorResponse.ErrorDetail.builder().field("requestBody").message(details).rejectedValue(null).errorType("INVALID_JSON").build());

		ErrorResponse errorResponse = ErrorResponse.builder().status(HttpStatus.BAD_REQUEST.value()).message(message).details(errorDetails).timestamp(LocalDateTime.now()).build();

		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

	private List<String> getFieldOrderFromClass(MethodParameter parameter) {
		if (parameter == null) {
			return Collections.emptyList();
		} else {
			parameter.getParameterType();
		}

		Class<?> targetClass = parameter.getParameterType();
		return Arrays.stream(targetClass.getDeclaredFields()).map(Field::getName).collect(Collectors.toList());
	}

}