package org.is.bandmanager.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.is.bandmanager.model.Color;
import org.is.bandmanager.model.Country;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PersonRequestTests {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void shouldCreateValidPersonRequest() {
        // Given
        LocationRequest location = LocationRequest.builder()
                .x(100)
                .y(200L)
                .z(300L)
                .build();

        PersonRequest request = PersonRequest.builder()
                .name("John Doe")
                .eyeColor(Color.BLUE)
                .hairColor(Color.BLACK)
                .location(location)
                .weight(75.5f)
                .nationality(Country.USA)
                .build();

        // When
        Set<ConstraintViolation<PersonRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailWhenNameIsNull() {
        // Given
        LocationRequest location = LocationRequest.builder()
                .x(100)
                .y(200L)
                .z(300L)
                .build();

        PersonRequest request = PersonRequest.builder()
                .name(null)
                .eyeColor(Color.BLUE)
                .hairColor(Color.BLACK)
                .location(location)
                .weight(75.5f)
                .nationality(Country.USA)
                .build();

        // When
        Set<ConstraintViolation<PersonRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<PersonRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("name");
        assertThat(violation.getMessage()).isEqualTo("Person.Name не может быть пустым");
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        // Given
        LocationRequest location = LocationRequest.builder()
                .x(100)
                .y(200L)
                .z(300L)
                .build();

        PersonRequest request = PersonRequest.builder()
                .name("   ")
                .eyeColor(Color.BLUE)
                .hairColor(Color.BLACK)
                .location(location)
                .weight(75.5f)
                .nationality(Country.USA)
                .build();

        // When
        Set<ConstraintViolation<PersonRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<PersonRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("name");
        assertThat(violation.getMessage()).isEqualTo("Person.Name не может быть пустым");
    }

    @Test
    void shouldFailWhenNameIsEmpty() {
        // Given
        LocationRequest location = LocationRequest.builder()
                .x(100)
                .y(200L)
                .z(300L)
                .build();

        PersonRequest request = PersonRequest.builder()
                .name("")
                .eyeColor(Color.BLUE)
                .hairColor(Color.BLACK)
                .location(location)
                .weight(75.5f)
                .nationality(Country.USA)
                .build();

        // When
        Set<ConstraintViolation<PersonRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<PersonRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("name");
        assertThat(violation.getMessage()).isEqualTo("Person.Name не может быть пустым");
    }

    @Test
    void shouldFailWhenEyeColorIsNull() {
        // Given
        LocationRequest location = LocationRequest.builder()
                .x(100)
                .y(200L)
                .z(300L)
                .build();

        PersonRequest request = PersonRequest.builder()
                .name("John Doe")
                .eyeColor(null)
                .hairColor(Color.BLACK)
                .location(location)
                .weight(75.5f)
                .nationality(Country.USA)
                .build();

        // When
        Set<ConstraintViolation<PersonRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<PersonRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("eyeColor");
        assertThat(violation.getMessage()).isEqualTo("Person.EyeColor не может быть пустым");
    }

    @Test
    void shouldFailWhenHairColorIsNull() {
        // Given
        LocationRequest location = LocationRequest.builder()
                .x(100)
                .y(200L)
                .z(300L)
                .build();

        PersonRequest request = PersonRequest.builder()
                .name("John Doe")
                .eyeColor(Color.BLUE)
                .hairColor(null)
                .location(location)
                .weight(75.5f)
                .nationality(Country.USA)
                .build();

        // When
        Set<ConstraintViolation<PersonRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<PersonRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("hairColor");
        assertThat(violation.getMessage()).isEqualTo("Person.HairColor не может быть пустым");
    }

    @Test
    void shouldFailWhenLocationIsNull() {
        // Given
        PersonRequest request = PersonRequest.builder()
                .name("John Doe")
                .eyeColor(Color.BLUE)
                .hairColor(Color.BLACK)
                .location(null)
                .weight(75.5f)
                .nationality(Country.USA)
                .build();

        // When
        Set<ConstraintViolation<PersonRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<PersonRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("location");
        assertThat(violation.getMessage()).isEqualTo("Person.LocationRequest не может быть пустым");
    }

    @Test
    void shouldFailWhenLocationIsInvalid() {
        // Given - Location с null полями Y и Z (что невалидно)
        LocationRequest invalidLocation = LocationRequest.builder()
                .x(100)
                .y(null)
                .z(null)
                .build();

        PersonRequest request = PersonRequest.builder()
                .name("John Doe")
                .eyeColor(Color.BLUE)
                .hairColor(Color.BLACK)
                .location(invalidLocation)
                .weight(75.5f)
                .nationality(Country.USA)
                .build();

        // When
        Set<ConstraintViolation<PersonRequest>> violations = validator.validate(request);

        // Then - должна сработать каскадная валидация LocationRequest
        assertThat(violations).hasSize(2);
        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .extracting(Object::toString)
                .containsExactlyInAnyOrder("location.y", "location.z");
    }

    @Test
    void shouldFailWhenWeightIsNull() {
        // Given
        LocationRequest location = LocationRequest.builder()
                .x(100)
                .y(200L)
                .z(300L)
                .build();

        PersonRequest request = PersonRequest.builder()
                .name("John Doe")
                .eyeColor(Color.BLUE)
                .hairColor(Color.BLACK)
                .location(location)
                .weight(null)
                .nationality(Country.USA)
                .build();

        // When
        Set<ConstraintViolation<PersonRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<PersonRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("weight");
        assertThat(violation.getMessage()).isEqualTo("Person.Weight не может быть пустым");
    }

    @Test
    void shouldFailWhenWeightIsZero() {
        // Given
        LocationRequest location = LocationRequest.builder()
                .x(100)
                .y(200L)
                .z(300L)
                .build();

        PersonRequest request = PersonRequest.builder()
                .name("John Doe")
                .eyeColor(Color.BLUE)
                .hairColor(Color.BLACK)
                .location(location)
                .weight(0.0f)
                .nationality(Country.USA)
                .build();

        // When
        Set<ConstraintViolation<PersonRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<PersonRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("weight");
        assertThat(violation.getMessage()).isEqualTo("Person.Weight должно быть > 0");
    }

    @Test
    void shouldFailWhenWeightIsNegative() {
        // Given
        LocationRequest location = LocationRequest.builder()
                .x(100)
                .y(200L)
                .z(300L)
                .build();

        PersonRequest request = PersonRequest.builder()
                .name("John Doe")
                .eyeColor(Color.BLUE)
                .hairColor(Color.BLACK)
                .location(location)
                .weight(-5.5f)
                .nationality(Country.USA)
                .build();

        // When
        Set<ConstraintViolation<PersonRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<PersonRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("weight");
        assertThat(violation.getMessage()).isEqualTo("Person.Weight должно быть > 0");
    }

    @Test
    void shouldFailWhenNationalityIsNull() {
        // Given
        LocationRequest location = LocationRequest.builder()
                .x(100)
                .y(200L)
                .z(300L)
                .build();

        PersonRequest request = PersonRequest.builder()
                .name("John Doe")
                .eyeColor(Color.BLUE)
                .hairColor(Color.BLACK)
                .location(location)
                .weight(75.5f)
                .nationality(null)
                .build();

        // When
        Set<ConstraintViolation<PersonRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<PersonRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("nationality");
        assertThat(violation.getMessage()).isEqualTo("Person.Country не может быть пустым");
    }

    @Test
    void shouldFailWithMultipleViolations() {
        // Given
        LocationRequest invalidLocation = LocationRequest.builder()
                .x(100)
                .y(null)
                .z(null)
                .build();

        PersonRequest request = PersonRequest.builder()
                .name("")
                .eyeColor(null)
                .hairColor(null)
                .location(invalidLocation)
                .weight(0.0f)
                .nationality(null)
                .build();

        // When
        Set<ConstraintViolation<PersonRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(7);
        assertThat(violations)
                .extracting(ConstraintViolation::getPropertyPath)
                .extracting(Object::toString)
                .containsExactlyInAnyOrder(
                        "name",
                        "eyeColor",
                        "hairColor",
                        "location.y",
                        "location.z",
                        "weight",
                        "nationality"
                );
    }

    @Test
    void shouldBeValidWithMinimalPositiveWeight() {
        // Given
        LocationRequest location = LocationRequest.builder()
                .x(100)
                .y(200L)
                .z(300L)
                .build();

        PersonRequest request = PersonRequest.builder()
                .name("John Doe")
                .eyeColor(Color.BLUE)
                .hairColor(Color.BLACK)
                .location(location)
                .weight(Float.MIN_VALUE) // Самое маленькое положительное значение
                .nationality(Country.USA)
                .build();

        // When
        Set<ConstraintViolation<PersonRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldBeValidWithDifferentEnumValues() {
        // Given
        LocationRequest location = LocationRequest.builder()
                .x(100)
                .y(200L)
                .z(300L)
                .build();

        PersonRequest request = PersonRequest.builder()
                .name("Jane Doe")
                .eyeColor(Color.GREEN)
                .hairColor(Color.BROWN)
                .location(location)
                .weight(60.0f)
                .nationality(Country.UK)
                .build();

        // When
        Set<ConstraintViolation<PersonRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }
}