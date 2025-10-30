package org.is.bandmanager.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AlbumRequestTests {

	private Validator validator;

	@BeforeEach
	void setUp() {
		try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
			validator = factory.getValidator();
		}
	}

	@Test
	void shouldCreateValidAlbumRequest() {
		// Given
		AlbumRequest request = AlbumRequest.builder().name("Test Album").tracks(10L).sales(50000).build();

		// When
		Set<ConstraintViolation<AlbumRequest>> violations = validator.validate(request);

		// Then
		assertThat(violations).isEmpty();
	}

	@Test
	void shouldFailWhenNameIsNull() {
		// Given
		AlbumRequest request = AlbumRequest.builder().name(null).tracks(10L).sales(50000).build();

		// When
		Set<ConstraintViolation<AlbumRequest>> violations = validator.validate(request);

		// Then
		assertThat(violations).hasSize(1);
		ConstraintViolation<AlbumRequest> violation = violations.iterator().next();
		assertThat(violation.getPropertyPath().toString()).isEqualTo("name");
		assertThat(violation.getMessage()).isEqualTo("Album.Name не может быть пустым");
	}

	@Test
	void shouldFailWhenNameIsBlank() {
		// Given
		AlbumRequest request = AlbumRequest.builder().name("   ").tracks(10L).sales(50000).build();

		// When
		Set<ConstraintViolation<AlbumRequest>> violations = validator.validate(request);

		// Then
		assertThat(violations).hasSize(1);
		ConstraintViolation<AlbumRequest> violation = violations.iterator().next();
		assertThat(violation.getPropertyPath().toString()).isEqualTo("name");
		assertThat(violation.getMessage()).isEqualTo("Album.Name не может быть пустым");
	}

	@Test
	void shouldFailWhenNameIsEmpty() {
		// Given
		AlbumRequest request = AlbumRequest.builder().name("").tracks(10L).sales(50000).build();

		// When
		Set<ConstraintViolation<AlbumRequest>> violations = validator.validate(request);

		// Then
		assertThat(violations).hasSize(1);
		ConstraintViolation<AlbumRequest> violation = violations.iterator().next();
		assertThat(violation.getPropertyPath().toString()).isEqualTo("name");
		assertThat(violation.getMessage()).isEqualTo("Album.Name не может быть пустым");
	}

	@Test
	void shouldFailWhenTracksIsNull() {
		// Given
		AlbumRequest request = AlbumRequest.builder().name("Test Album").tracks(null).sales(50000).build();

		// When
		Set<ConstraintViolation<AlbumRequest>> violations = validator.validate(request);

		// Then
		assertThat(violations).hasSize(1);
		ConstraintViolation<AlbumRequest> violation = violations.iterator().next();
		assertThat(violation.getPropertyPath().toString()).isEqualTo("tracks");
		assertThat(violation.getMessage()).isEqualTo("Album.Tracks не может быть пустым");
	}

	@Test
	void shouldFailWhenTracksIsZero() {
		// Given
		AlbumRequest request = AlbumRequest.builder().name("Test Album").tracks(0L).sales(50000).build();

		// When
		Set<ConstraintViolation<AlbumRequest>> violations = validator.validate(request);

		// Then
		assertThat(violations).hasSize(1);
		ConstraintViolation<AlbumRequest> violation = violations.iterator().next();
		assertThat(violation.getPropertyPath().toString()).isEqualTo("tracks");
		assertThat(violation.getMessage()).isEqualTo("Album.Tracks должно быть > 0");
	}

	@Test
	void shouldFailWhenTracksIsNegative() {
		// Given
		AlbumRequest request = AlbumRequest.builder().name("Test Album").tracks(-5L).sales(50000).build();

		// When
		Set<ConstraintViolation<AlbumRequest>> violations = validator.validate(request);

		// Then
		assertThat(violations).hasSize(1);
		ConstraintViolation<AlbumRequest> violation = violations.iterator().next();
		assertThat(violation.getPropertyPath().toString()).isEqualTo("tracks");
		assertThat(violation.getMessage()).isEqualTo("Album.Tracks должно быть > 0");
	}

	@Test
	void shouldFailWhenSalesIsZero() {
		// Given
		AlbumRequest request = AlbumRequest.builder().name("Test Album").tracks(10L).sales(0).build();

		// When
		Set<ConstraintViolation<AlbumRequest>> violations = validator.validate(request);

		// Then
		assertThat(violations).hasSize(1);
		ConstraintViolation<AlbumRequest> violation = violations.iterator().next();
		assertThat(violation.getPropertyPath().toString()).isEqualTo("sales");
		assertThat(violation.getMessage()).isEqualTo("Album.Sales должно быть > 0");
	}

	@Test
	void shouldFailWhenSalesIsNegative() {
		// Given
		AlbumRequest request = AlbumRequest.builder().name("Test Album").tracks(10L).sales(-100).build();

		// When
		Set<ConstraintViolation<AlbumRequest>> violations = validator.validate(request);

		// Then
		assertThat(violations).hasSize(1);
		ConstraintViolation<AlbumRequest> violation = violations.iterator().next();
		assertThat(violation.getPropertyPath().toString()).isEqualTo("sales");
		assertThat(violation.getMessage()).isEqualTo("Album.Sales должно быть > 0");
	}

	@Test
	void shouldBeValidWhenSalesIsNull() {
		// Given
		AlbumRequest request = AlbumRequest.builder().name("Test Album").tracks(10L).sales(null).build();

		// When
		Set<ConstraintViolation<AlbumRequest>> violations = validator.validate(request);

		// Then
		assertThat(violations).isEmpty();
	}

	@Test
	void shouldFailWithMultipleViolations() {
		// Given
		AlbumRequest request = AlbumRequest.builder().name("").tracks(0L).sales(0).build();

		// When
		Set<ConstraintViolation<AlbumRequest>> violations = validator.validate(request);

		// Then
		assertThat(violations).hasSize(3);
		assertThat(violations).extracting(ConstraintViolation::getPropertyPath).extracting(Object::toString).containsExactlyInAnyOrder("name", "tracks", "sales");
	}

}