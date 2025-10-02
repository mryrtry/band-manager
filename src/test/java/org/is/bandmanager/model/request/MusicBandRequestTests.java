package org.is.bandmanager.model.request;

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
        CoordinatesRequest coordinates = CoordinatesRequest.builder()
                .x(100)
                .y(50.5f)
                .build();

        AlbumRequest bestAlbum = AlbumRequest.builder()
                .name("Best Album")
                .tracks(12L)
                .sales(1000000)
                .build();

        LocationRequest location = LocationRequest.builder()
                .y(200L)
                .z(300L)
                .build();

        PersonRequest frontMan = PersonRequest.builder()
                .name("John Doe")
                .eyeColor(org.is.bandmanager.model.Color.BLUE)
                .hairColor(org.is.bandmanager.model.Color.BLACK)
                .location(location)
                .weight(75.5f)
                .nationality(org.is.bandmanager.model.Country.USA)
                .build();

        MusicBandRequest request = MusicBandRequest.builder()
                .name("Test Band")
                .coordinates(coordinates)
                .genre(MusicGenre.ROCK)
                .numberOfParticipants(5L)
                .singlesCount(10L)
                .description("A great band")
                .bestAlbum(bestAlbum)
                .albumsCount(3L)
                .establishmentDate(new Date())
                .frontMan(frontMan)
                .build();

        // When
        Set<ConstraintViolation<MusicBandRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailWhenNameIsNull() {
        // Given
        MusicBandRequest request = createValidMusicBandRequest()
                .name(null)
                .build();

        // When
        Set<ConstraintViolation<MusicBandRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("MusicBand.Name не может быть пустым");
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        // Given
        MusicBandRequest request = createValidMusicBandRequest()
                .name("   ")
                .build();

        // When
        Set<ConstraintViolation<MusicBandRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("MusicBand.Name не может быть пустым");
    }

    @Test
    void shouldFailWhenCoordinatesIsNull() {
        // Given
        MusicBandRequest request = createValidMusicBandRequest()
                .coordinates(null)
                .build();

        // When
        Set<ConstraintViolation<MusicBandRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("MusicBand.Coordinates не может быть пустым");
    }

    @Test
    void shouldFailWhenGenreIsNull() {
        // Given
        MusicBandRequest request = createValidMusicBandRequest()
                .genre(null)
                .build();

        // When
        Set<ConstraintViolation<MusicBandRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("MusicBand.MusicGenre не может быть пустым");
    }

    @Test
    void shouldFailWhenNumberOfParticipantsIsNull() {
        // Given
        MusicBandRequest request = createValidMusicBandRequest()
                .numberOfParticipants(null)
                .build();

        // When
        Set<ConstraintViolation<MusicBandRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("MusicBand.NumberOfParticipants не может быть пустым");
    }

    @Test
    void shouldFailWhenNumberOfParticipantsIsZero() {
        // Given
        MusicBandRequest request = createValidMusicBandRequest()
                .numberOfParticipants(0L)
                .build();

        // When
        Set<ConstraintViolation<MusicBandRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("MusicBand.NumberOfParticipants должно быть > 0");
    }

    @Test
    void shouldFailWhenSinglesCountIsNull() {
        // Given
        MusicBandRequest request = createValidMusicBandRequest()
                .singlesCount(null)
                .build();

        // When
        Set<ConstraintViolation<MusicBandRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("MusicBand.SinglesCount не может быть пустым");
    }

    @Test
    void shouldFailWhenSinglesCountIsZero() {
        // Given
        MusicBandRequest request = createValidMusicBandRequest()
                .singlesCount(0L)
                .build();

        // When
        Set<ConstraintViolation<MusicBandRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("MusicBand.NumberOfParticipants должно быть > 0");
    }

    @Test
    void shouldFailWhenDescriptionIsNull() {
        // Given
        MusicBandRequest request = createValidMusicBandRequest()
                .description(null)
                .build();

        // When
        Set<ConstraintViolation<MusicBandRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("MusicBand.Description не может быть пустым");
    }

    @Test
    void shouldFailWhenBestAlbumIsNull() {
        // Given
        MusicBandRequest request = createValidMusicBandRequest()
                .bestAlbum(null)
                .build();

        // When
        Set<ConstraintViolation<MusicBandRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("MusicBand.BestAlbum не может быть пустым");
    }

    @Test
    void shouldFailWhenAlbumsCountIsNull() {
        // Given
        MusicBandRequest request = createValidMusicBandRequest()
                .albumsCount(null)
                .build();

        // When
        Set<ConstraintViolation<MusicBandRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("MusicBand.AlbumsCount не может быть пустым");
    }

    @Test
    void shouldFailWhenAlbumsCountIsZero() {
        // Given
        MusicBandRequest request = createValidMusicBandRequest()
                .albumsCount(0L)
                .build();

        // When
        Set<ConstraintViolation<MusicBandRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("MusicBand.AlbumsCount должно быть > 0");
    }

    @Test
    void shouldFailWhenEstablishmentDateIsNull() {
        // Given
        MusicBandRequest request = createValidMusicBandRequest()
                .establishmentDate(null)
                .build();

        // When
        Set<ConstraintViolation<MusicBandRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("MusicBand.EstablishmentDate не может быть пустым");
    }

    @Test
    void shouldFailWhenFrontManIsNull() {
        // Given
        MusicBandRequest request = createValidMusicBandRequest()
                .frontMan(null)
                .build();

        // When
        Set<ConstraintViolation<MusicBandRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("MusicBand.FrontMan не может быть пустым");
    }

    @Test
    void shouldFailWithMultipleViolations() {
        // Given
        MusicBandRequest request = MusicBandRequest.builder()
                .name("")
                .coordinates(null)
                .genre(null)
                .numberOfParticipants(0L)
                .singlesCount(0L)
                .description("")
                .bestAlbum(null)
                .albumsCount(0L)
                .establishmentDate(null)
                .frontMan(null)
                .build();

        // When
        Set<ConstraintViolation<MusicBandRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(10);
    }

    // Вспомогательный метод для создания валидного запроса
    private MusicBandRequest.MusicBandRequestBuilder createValidMusicBandRequest() {
        CoordinatesRequest coordinates = CoordinatesRequest.builder()
                .x(100)
                .y(50.5f)
                .build();

        AlbumRequest bestAlbum = AlbumRequest.builder()
                .name("Best Album")
                .tracks(12L)
                .sales(1000000)
                .build();

        LocationRequest location = LocationRequest.builder()
                .y(200L)
                .z(300L)
                .build();

        PersonRequest frontMan = PersonRequest.builder()
                .name("John Doe")
                .eyeColor(org.is.bandmanager.model.Color.BLUE)
                .hairColor(org.is.bandmanager.model.Color.BLACK)
                .location(location)
                .weight(75.5f)
                .nationality(org.is.bandmanager.model.Country.USA)
                .build();

        return MusicBandRequest.builder()
                .name("Test Band")
                .coordinates(coordinates)
                .genre(MusicGenre.ROCK)
                .numberOfParticipants(5L)
                .singlesCount(10L)
                .description("A great band")
                .bestAlbum(bestAlbum)
                .albumsCount(3L)
                .establishmentDate(new Date())
                .frontMan(frontMan);
    }
}