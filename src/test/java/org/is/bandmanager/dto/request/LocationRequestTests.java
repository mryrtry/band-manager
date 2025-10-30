package org.is.bandmanager.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class LocationRequestTests {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void shouldCreateValidLocationRequest() {
        // Given
        LocationRequest request = LocationRequest.builder().x(100).y(200L).z(300L).build();

        // When
        Set<ConstraintViolation<LocationRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldCreateValidLocationRequestWithNullX() {
        // Given
        LocationRequest request = LocationRequest.builder().x(null).y(200L).z(300L).build();

        // When
        Set<ConstraintViolation<LocationRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailWhenYIsNull() {
        // Given
        LocationRequest request = LocationRequest.builder().x(100).y(null).z(300L).build();

        // When
        Set<ConstraintViolation<LocationRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<LocationRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("y");
        assertThat(violation.getMessage()).isEqualTo("Location.Y не может быть пустым");
    }

    @Test
    void shouldFailWhenZIsNull() {
        // Given
        LocationRequest request = LocationRequest.builder().x(100).y(200L).z(null).build();

        // When
        Set<ConstraintViolation<LocationRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<LocationRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("z");
        assertThat(violation.getMessage()).isEqualTo("Location.Z не может быть пустым");
    }

    @Test
    void shouldFailWhenBothYAndZAreNull() {
        // Given
        LocationRequest request = LocationRequest.builder().x(100).y(null).z(null).build();

        // When
        Set<ConstraintViolation<LocationRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(2);
        assertThat(violations).extracting(ConstraintViolation::getPropertyPath).extracting(Object::toString).containsExactlyInAnyOrder("y", "z");
    }

    @Test
    void shouldFailWhenAllFieldsAreNull() {
        // Given
        LocationRequest request = LocationRequest.builder().x(null).y(null).z(null).build();

        // When
        Set<ConstraintViolation<LocationRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(2);
        assertThat(violations).extracting(ConstraintViolation::getPropertyPath).extracting(Object::toString).containsExactlyInAnyOrder("y", "z");
    }

    @Test
    void shouldBeValidWithOnlyRequiredFields() {
        // Given
        LocationRequest request = LocationRequest.builder().y(200L).z(300L).build();

        // When
        Set<ConstraintViolation<LocationRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldBeValidWithZeroValues() {
        // Given
        LocationRequest request = LocationRequest.builder().x(0).y(0L).z(0L).build();

        // When
        Set<ConstraintViolation<LocationRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldBeValidWithNegativeValues() {
        // Given
        LocationRequest request = LocationRequest.builder().x(-100).y(-200L).z(-300L).build();

        // When
        Set<ConstraintViolation<LocationRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldBeValidWithLargeValues() {
        // Given
        LocationRequest request = LocationRequest.builder().x(Integer.MAX_VALUE).y(Long.MAX_VALUE).z(Long.MAX_VALUE).build();

        // When
        Set<ConstraintViolation<LocationRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailWithMultipleViolationsForYAndZ() {
        // Given
        LocationRequest request = LocationRequest.builder().x(100).y(null).z(null).build();

        // When
        Set<ConstraintViolation<LocationRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(2);

        assertThat(violations).extracting(ConstraintViolation::getPropertyPath).extracting(Object::toString).containsExactlyInAnyOrder("y", "z");

        assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactlyInAnyOrder("Location.Y не может быть пустым", "Location.Z не может быть пустым");
    }

}