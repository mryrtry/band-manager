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
		PersonRequest request = PersonRequest.builder().name("John Doe").eyeColor(Color.BLUE).hairColor(Color.BLACK).locationId(1L)  // ID существующей локации
				.weight(75.5f).nationality(Country.USA).build();

		// When
		Set<ConstraintViolation<PersonRequest>> violations = validator.validate(request);

		// Then
		assertThat(violations).isEmpty();
	}

	@Test
	void shouldFailWhenNameIsNull() {
		// Given
		PersonRequest request = PersonRequest.builder().name(null).eyeColor(Color.BLUE).hairColor(Color.BLACK).locationId(1L).weight(75.5f).nationality(Country.USA).build();

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
		PersonRequest request = PersonRequest.builder().name("   ").eyeColor(Color.BLUE).hairColor(Color.BLACK).locationId(1L).weight(75.5f).nationality(Country.USA).build();

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
		PersonRequest request = PersonRequest.builder().name("").eyeColor(Color.BLUE).hairColor(Color.BLACK).locationId(1L).weight(75.5f).nationality(Country.USA).build();

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
		PersonRequest request = PersonRequest.builder().name("John Doe").eyeColor(null).hairColor(Color.BLACK).locationId(1L).weight(75.5f).nationality(Country.USA).build();

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
		PersonRequest request = PersonRequest.builder().name("John Doe").eyeColor(Color.BLUE).hairColor(null).locationId(1L).weight(75.5f).nationality(Country.USA).build();

		// When
		Set<ConstraintViolation<PersonRequest>> violations = validator.validate(request);

		// Then
		assertThat(violations).hasSize(1);
		ConstraintViolation<PersonRequest> violation = violations.iterator().next();
		assertThat(violation.getPropertyPath().toString()).isEqualTo("hairColor");
		assertThat(violation.getMessage()).isEqualTo("Person.HairColor не может быть пустым");
	}

	@Test
	void shouldFailWhenLocationIdIsNull() {
		// Given
		PersonRequest request = PersonRequest.builder().name("John Doe").eyeColor(Color.BLUE).hairColor(Color.BLACK).locationId(null).weight(75.5f).nationality(Country.USA).build();

		// When
		Set<ConstraintViolation<PersonRequest>> violations = validator.validate(request);

		// Then
		assertThat(violations).hasSize(1);
		ConstraintViolation<PersonRequest> violation = violations.iterator().next();
		assertThat(violation.getPropertyPath().toString()).isEqualTo("locationId");
		assertThat(violation.getMessage()).isEqualTo("Person.LocationId не может быть пустым");
	}

	@Test
	void shouldFailWhenWeightIsNull() {
		// Given
		PersonRequest request = PersonRequest.builder().name("John Doe").eyeColor(Color.BLUE).hairColor(Color.BLACK).locationId(1L).weight(null).nationality(Country.USA).build();

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
		PersonRequest request = PersonRequest.builder().name("John Doe").eyeColor(Color.BLUE).hairColor(Color.BLACK).locationId(1L).weight(0.0f).nationality(Country.USA).build();

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
		PersonRequest request = PersonRequest.builder().name("John Doe").eyeColor(Color.BLUE).hairColor(Color.BLACK).locationId(1L).weight(-5.5f).nationality(Country.USA).build();

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
		PersonRequest request = PersonRequest.builder().name("John Doe").eyeColor(Color.BLUE).hairColor(Color.BLACK).locationId(1L).weight(75.5f).nationality(null).build();

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
		PersonRequest request = PersonRequest.builder().name("").eyeColor(null).hairColor(null).locationId(null).weight(0.0f).nationality(null).build();

		// When
		Set<ConstraintViolation<PersonRequest>> violations = validator.validate(request);

		// Then
		assertThat(violations).hasSize(6);
		assertThat(violations).extracting(ConstraintViolation::getPropertyPath).extracting(Object::toString).containsExactlyInAnyOrder("name", "eyeColor", "hairColor", "locationId", "weight", "nationality");
	}

	@Test
	void shouldBeValidWithMinimalPositiveWeight() {
		// Given
		PersonRequest request = PersonRequest.builder().name("John Doe").eyeColor(Color.BLUE).hairColor(Color.BLACK).locationId(1L).weight(Float.MIN_VALUE) // Самое маленькое положительное значение
				.nationality(Country.USA).build();

		// When
		Set<ConstraintViolation<PersonRequest>> violations = validator.validate(request);

		// Then
		assertThat(violations).isEmpty();
	}

	@Test
	void shouldBeValidWithDifferentEnumValues() {
		// Given
		PersonRequest request = PersonRequest.builder().name("Jane Doe").eyeColor(Color.GREEN).hairColor(Color.BROWN).locationId(2L).weight(60.0f).nationality(Country.UK).build();

		// When
		Set<ConstraintViolation<PersonRequest>> violations = validator.validate(request);

		// Then
		assertThat(violations).isEmpty();
	}

	@Test
	void shouldBeValidWithZeroLocationId() {
		// Given
		PersonRequest request = PersonRequest.builder().name("John Doe").eyeColor(Color.BLUE).hairColor(Color.BLACK).locationId(0L) // 0 - валидный Long, но невалидный ID в БД
				.weight(75.5f).nationality(Country.USA).build();

		// When
		Set<ConstraintViolation<PersonRequest>> violations = validator.validate(request);

		// Then - locationId не может быть null, но 0L - валидное значение Long
		assertThat(violations).isEmpty();
	}

	@Test
	void shouldBeValidWithNegativeLocationId() {
		// Given
		PersonRequest request = PersonRequest.builder().name("John Doe").eyeColor(Color.BLUE).hairColor(Color.BLACK).locationId(-1L) // Отрицательный ID - валидный Long
				.weight(75.5f).nationality(Country.USA).build();

		// When
		Set<ConstraintViolation<PersonRequest>> violations = validator.validate(request);

		// Then - валидация проходит, но такой ID не будет найден в БД
		assertThat(violations).isEmpty();
	}

}