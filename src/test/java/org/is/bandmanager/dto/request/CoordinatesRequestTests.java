package org.is.bandmanager.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CoordinatesRequestTests {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void shouldCreateValidCoordinatesRequest() {
        // Given
        CoordinatesRequest request = CoordinatesRequest.builder().x(100).y(50.5f).build();

        // When
        Set<ConstraintViolation<CoordinatesRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldCreateValidCoordinatesRequestWithNullY() {
        // Given
        CoordinatesRequest request = CoordinatesRequest.builder().x(100).y(null).build();

        // When
        Set<ConstraintViolation<CoordinatesRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailWhenXIsNull() {
        // Given
        CoordinatesRequest request = CoordinatesRequest.builder().x(null).y(50.5f).build();

        // When
        Set<ConstraintViolation<CoordinatesRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<CoordinatesRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("x");
        assertThat(violation.getMessage()).isEqualTo("Coordinates.X не может быть пустым");
    }

    @Test
    void shouldFailWhenXIsEqualToMinus147() {
        // Given
        CoordinatesRequest request = CoordinatesRequest.builder().x(-147).y(50.5f).build();

        // When
        Set<ConstraintViolation<CoordinatesRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<CoordinatesRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("x");
        assertThat(violation.getMessage()).isEqualTo("Coordinates.X должно быть больше -147");
    }

    @Test
    void shouldFailWhenXIsLessThanMinus147() {
        // Given
        CoordinatesRequest request = CoordinatesRequest.builder().x(-148).y(50.5f).build();

        // When
        Set<ConstraintViolation<CoordinatesRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<CoordinatesRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("x");
        assertThat(violation.getMessage()).isEqualTo("Coordinates.X должно быть больше -147");
    }

    @Test
    void shouldBeValidWhenXIsGreaterThanMinus147() {
        // Given
        CoordinatesRequest request = CoordinatesRequest.builder().x(-146).y(50.5f).build();

        // When
        Set<ConstraintViolation<CoordinatesRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldBeValidWhenXIsPositive() {
        // Given
        CoordinatesRequest request = CoordinatesRequest.builder().x(1000).y(50.5f).build();

        // When
        Set<ConstraintViolation<CoordinatesRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldBeValidWhenXIsZero() {
        // Given
        CoordinatesRequest request = CoordinatesRequest.builder().x(0).y(50.5f).build();

        // When
        Set<ConstraintViolation<CoordinatesRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldBeValidWhenXIsMinus146() {
        // Given
        CoordinatesRequest request = CoordinatesRequest.builder().x(-146).y(50.5f).build();

        // When
        Set<ConstraintViolation<CoordinatesRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldBeValidWithAllFieldsNullExceptX() {
        // Given
        CoordinatesRequest request = CoordinatesRequest.builder().x(100).y(null).build();

        // When
        Set<ConstraintViolation<CoordinatesRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailWithMultipleViolations() {
        // Given
        CoordinatesRequest request = CoordinatesRequest.builder().x(-147).y(null).build();

        // When
        Set<ConstraintViolation<CoordinatesRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<CoordinatesRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("x");
        assertThat(violation.getMessage()).isEqualTo("Coordinates.X должно быть больше -147");
    }
}