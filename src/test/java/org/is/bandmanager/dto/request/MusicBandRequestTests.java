package org.is.bandmanager.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.is.bandmanager.model.MusicGenre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MusicBandRequestTests {

	private Validator validator;

	@BeforeEach
	void setUp() {
		try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
			validator = factory.getValidator();
		}
	}

	@Test
	void shouldCreateValidMusicBandRequest() {
		// Given
		MusicBandRequest request = MusicBandRequest.builder().name("Test Band").coordinatesId(1L).genre(MusicGenre.ROCK).numberOfParticipants(5L).singlesCount(10L).description("A great band").bestAlbumId(2L).albumsCount(3L).establishmentDate(new Date()).frontManId(3L).build();

		// When
		Set<ConstraintViolation<MusicBandRequest>> violations = validator.validate(request);

		// Then
		assertThat(violations).isEmpty();
	}

	@Test
	void shouldFailWhenNameIsNull() {
		// Given
		MusicBandRequest request = createValidMusicBandRequest().name(null).build();

		// When
		Set<ConstraintViolation<MusicBandRequest>> violations = validator.validate(request);

		// Then
		assertThat(violations).hasSize(1);
		assertThat(violations.iterator().next().getMessage()).isEqualTo("MusicBand.Name не может быть пустым");
	}

	@Test
	void shouldFailWhenNameIsBlank() {
		// Given
		MusicBandRequest request = createValidMusicBandRequest().name("   ").build();

		// When
		Set<ConstraintViolation<MusicBandRequest>> violations = validator.validate(request);

		// Then
		assertThat(violations).hasSize(1);
		assertThat(violations.iterator().next().getMessage()).isEqualTo("MusicBand.Name не может быть пустым");
	}

	@Test
	void shouldFailWhenCoordinatesIdIsNull() {
		// Given
		MusicBandRequest request = createValidMusicBandRequest().coordinatesId(null).build();

		// When
		Set<ConstraintViolation<MusicBandRequest>> violations = validator.validate(request);

		// Then
		assertThat(violations).hasSize(1);
		assertThat(violations.iterator().next().getMessage()).isEqualTo("MusicBand.CoordinatesId не может быть пустым");
	}

	@Test
	void shouldFailWhenGenreIsNull() {
		// Given
		MusicBandRequest request = createValidMusicBandRequest().genre(null).build();

		// When
		Set<ConstraintViolation<MusicBandRequest>> violations = validator.validate(request);

		// Then
		assertThat(violations).hasSize(1);
		assertThat(violations.iterator().next().getMessage()).isEqualTo("MusicBand.MusicGenre не может быть пустым");
	}

	@Test
	void shouldFailWhenNumberOfParticipantsIsNull() {
		// Given
		MusicBandRequest request = createValidMusicBandRequest().numberOfParticipants(null).build();

		// When
		Set<ConstraintViolation<MusicBandRequest>> violations = validator.validate(request);

		// Then
		assertThat(violations).hasSize(1);
		assertThat(violations.iterator().next().getMessage()).isEqualTo("MusicBand.NumberOfParticipants не может быть пустым");
	}

	@Test
	void shouldFailWhenNumberOfParticipantsIsZero() {
		// Given
		MusicBandRequest request = createValidMusicBandRequest().numberOfParticipants(0L).build();

		// When
		Set<ConstraintViolation<MusicBandRequest>> violations = validator.validate(request);

		// Then
		assertThat(violations).hasSize(1);
		assertThat(violations.iterator().next().getMessage()).isEqualTo("MusicBand.NumberOfParticipants должно быть > 0");
	}

	@Test
	void shouldFailWhenSinglesCountIsNull() {
		// Given
		MusicBandRequest request = createValidMusicBandRequest().singlesCount(null).build();

		// When
		Set<ConstraintViolation<MusicBandRequest>> violations = validator.validate(request);

		// Then
		assertThat(violations).hasSize(1);
		assertThat(violations.iterator().next().getMessage()).isEqualTo("MusicBand.SinglesCount не может быть пустым");
	}

	@Test
	void shouldFailWhenDescriptionIsNull() {
		// Given
		MusicBandRequest request = createValidMusicBandRequest().description(null).build();

		// When
		Set<ConstraintViolation<MusicBandRequest>> violations = validator.validate(request);

		// Then
		assertThat(violations).hasSize(1);
		assertThat(violations.iterator().next().getMessage()).isEqualTo("MusicBand.Description не может быть пустым");
	}

	@Test
	void shouldFailWhenDescriptionIsBlank() {
		// Given
		MusicBandRequest request = createValidMusicBandRequest().description("   ").build();

		// When
		Set<ConstraintViolation<MusicBandRequest>> violations = validator.validate(request);

		// Then
		assertThat(violations).hasSize(1);
		assertThat(violations.iterator().next().getMessage()).isEqualTo("MusicBand.Description не может быть пустым");
	}

	@Test
	void shouldFailWhenBestAlbumIdIsNull() {
		// Given
		MusicBandRequest request = createValidMusicBandRequest().bestAlbumId(null).build();

		// When
		Set<ConstraintViolation<MusicBandRequest>> violations = validator.validate(request);

		// Then
		assertThat(violations).hasSize(1);
		assertThat(violations.iterator().next().getMessage()).isEqualTo("MusicBand.BestAlbumId не может быть пустым");
	}

	@Test
	void shouldFailWhenAlbumsCountIsNull() {
		// Given
		MusicBandRequest request = createValidMusicBandRequest().albumsCount(null).build();

		// When
		Set<ConstraintViolation<MusicBandRequest>> violations = validator.validate(request);

		// Then
		assertThat(violations).hasSize(1);
		assertThat(violations.iterator().next().getMessage()).isEqualTo("MusicBand.AlbumsCount не может быть пустым");
	}

	@Test
	void shouldFailWhenAlbumsCountIsZero() {
		// Given
		MusicBandRequest request = createValidMusicBandRequest().albumsCount(0L).build();

		// When
		Set<ConstraintViolation<MusicBandRequest>> violations = validator.validate(request);

		// Then
		assertThat(violations).hasSize(1);
		assertThat(violations.iterator().next().getMessage()).isEqualTo("MusicBand.AlbumsCount должно быть > 0");
	}

	@Test
	void shouldFailWhenEstablishmentDateIsNull() {
		// Given
		MusicBandRequest request = createValidMusicBandRequest().establishmentDate(null).build();

		// When
		Set<ConstraintViolation<MusicBandRequest>> violations = validator.validate(request);

		// Then
		assertThat(violations).hasSize(1);
		assertThat(violations.iterator().next().getMessage()).isEqualTo("MusicBand.EstablishmentDate не может быть пустым");
	}

	@Test
	void shouldFailWhenFrontManIdIsNull() {
		// Given
		MusicBandRequest request = createValidMusicBandRequest().frontManId(null).build();

		// When
		Set<ConstraintViolation<MusicBandRequest>> violations = validator.validate(request);

		// Then
		assertThat(violations).hasSize(1);
		assertThat(violations.iterator().next().getMessage()).isEqualTo("MusicBand.FrontManId не может быть пустым");
	}

	@Test
	void shouldFailWithMultipleViolations() {
		// Given
		MusicBandRequest request = MusicBandRequest.builder().name("").coordinatesId(null).genre(null).numberOfParticipants(0L).singlesCount(0L).description("").bestAlbumId(null).albumsCount(0L).establishmentDate(null).frontManId(null).build();

		// When
		Set<ConstraintViolation<MusicBandRequest>> violations = validator.validate(request);

		// Then
		assertThat(violations).hasSize(10);
	}

	private MusicBandRequest.MusicBandRequestBuilder createValidMusicBandRequest() {
		return MusicBandRequest.builder().name("Test Band").coordinatesId(1L).genre(MusicGenre.ROCK).numberOfParticipants(5L).singlesCount(10L).description("A great band").bestAlbumId(2L).albumsCount(3L).establishmentDate(new Date()).frontManId(3L);
	}

}