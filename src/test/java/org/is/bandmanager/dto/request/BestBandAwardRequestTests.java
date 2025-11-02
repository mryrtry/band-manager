package org.is.bandmanager.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.is.bandmanager.model.MusicGenre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class BestBandAwardRequestTests {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void shouldCreateValidBestBandAwardRequest() {
        // Given
        BestBandAwardRequest request = BestBandAwardRequest.builder().musicBandId(1L).genre(MusicGenre.ROCK).build();

        // When
        Set<ConstraintViolation<BestBandAwardRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailWhenMusicBandIdIsNull() {
        // Given
        BestBandAwardRequest request = BestBandAwardRequest.builder().musicBandId(null).genre(MusicGenre.ROCK).build();

        // When
        Set<ConstraintViolation<BestBandAwardRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<BestBandAwardRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("musicBandId");
        assertThat(violation.getMessage()).isEqualTo("BestBandAward.MusicBandId не может быть пустым");
    }

    @Test
    void shouldFailWhenGenreIsNull() {
        // Given
        BestBandAwardRequest request = BestBandAwardRequest.builder().musicBandId(1L).genre(null).build();

        // When
        Set<ConstraintViolation<BestBandAwardRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<BestBandAwardRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("genre");
        assertThat(violation.getMessage()).isEqualTo("BestBandAward.MusicGenre не может быть пустым");
    }

    @Test
    void shouldFailWhenBothFieldsAreNull() {
        // Given
        BestBandAwardRequest request = BestBandAwardRequest.builder().musicBandId(null).genre(null).build();

        // When
        Set<ConstraintViolation<BestBandAwardRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(2);

        // Проверяем, что обе ошибки присутствуют
        Set<String> propertyPaths = Set.of("musicBandId", "genre");
        Set<String> messages = Set.of("BestBandAward.MusicBandId не может быть пустым", "BestBandAward.MusicGenre не может быть пустым");

        for (ConstraintViolation<BestBandAwardRequest> violation : violations) {
            assertThat(propertyPaths).contains(violation.getPropertyPath().toString());
            assertThat(messages).contains(violation.getMessage());
        }
    }

    @Test
    void shouldBeValidWithAllGenres() {
        // Test all available MusicGenre values
        for (MusicGenre genre : MusicGenre.values()) {
            // Given
            BestBandAwardRequest request = BestBandAwardRequest.builder().musicBandId(1L).genre(genre).build();

            // When
            Set<ConstraintViolation<BestBandAwardRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).as("Validation should pass for genre: " + genre).isEmpty();
        }
    }

    @Test
    void shouldBeValidWithDifferentBandIds() {
        Long[] validBandIds = {1L, 100L, 999L, Long.MAX_VALUE};

        for (Long bandId : validBandIds) {
            // Given
            BestBandAwardRequest request = BestBandAwardRequest.builder().musicBandId(bandId).genre(MusicGenre.ROCK).build();

            // When
            Set<ConstraintViolation<BestBandAwardRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).as("Validation should pass for bandId: " + bandId).isEmpty();
        }
    }

    @Test
    void shouldBeValidWithZeroBandId() {
        // Given
        BestBandAwardRequest request = BestBandAwardRequest.builder().musicBandId(0L).genre(MusicGenre.ROCK).build();

        // When
        Set<ConstraintViolation<BestBandAwardRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldBeValidWithNegativeBandId() {
        // Given
        BestBandAwardRequest request = BestBandAwardRequest.builder().musicBandId(-1L).genre(MusicGenre.ROCK).build();

        // When
        Set<ConstraintViolation<BestBandAwardRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldCreateValidRequestWithMinimumData() {
        // Given
        BestBandAwardRequest request = BestBandAwardRequest.builder().musicBandId(1L).genre(MusicGenre.PROGRESSIVE_ROCK).build();

        // When
        Set<ConstraintViolation<BestBandAwardRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldHandleEdgeCaseWithMaxIntegerBandId() {
        // Given
        BestBandAwardRequest request = BestBandAwardRequest.builder().musicBandId(Long.MAX_VALUE).genre(MusicGenre.POST_PUNK).build();

        // When
        Set<ConstraintViolation<BestBandAwardRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldHandleEdgeCaseWithMinIntegerBandId() {
        // Given
        BestBandAwardRequest request = BestBandAwardRequest.builder().musicBandId(Long.MIN_VALUE).genre(MusicGenre.POST_PUNK).build();

        // When
        Set<ConstraintViolation<BestBandAwardRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

}