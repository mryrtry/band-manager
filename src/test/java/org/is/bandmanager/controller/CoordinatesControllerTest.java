package org.is.bandmanager.controller;

import org.is.bandmanager.config.IntegrationTest;
import org.is.bandmanager.dto.request.CoordinatesRequest;
import org.is.bandmanager.model.Coordinates;
import org.is.bandmanager.repository.CoordinatesRepository;
import org.is.bandmanager.repository.MusicBandRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class CoordinatesControllerTest extends AbstractIntegrationTest {

    @Autowired
    private CoordinatesRepository coordinatesRepository;

    @Autowired
    private MusicBandRepository musicBandRepository;

    private static Stream<Arguments> provideInvalidCoordinatesRequests() {
        return Stream.of(
                Arguments.of("Null X coordinate",
                        createCoordinatesRequest(null, 15.5f), "x"),
                Arguments.of("X equal to -147",
                        createCoordinatesRequest(-147, 15.5f), "x"),
                Arguments.of("X less than -147",
                        createCoordinatesRequest(-148, 15.5f), "x")
        );
    }

    private static CoordinatesRequest createCoordinatesRequest(Integer x, Float y) {
        return CoordinatesRequest.builder()
                .x(x)
                .y(y)
                .build();
    }

    @BeforeEach
    @AfterEach
    void cleanDatabase() {
        musicBandRepository.deleteAll();
        coordinatesRepository.deleteAll();
    }

    @Test
    void shouldCreateCoordinatesSuccessfully() {
        // Given
        CoordinatesRequest request = createValidCoordinatesRequest();

        // When & Then
        getClient().post("/coordinates", request)
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.x").isEqualTo(10)
                .jsonPath("$.y").isEqualTo(15.5);

        // Verify database
        assertThat(coordinatesRepository.findAll()).hasSize(1);
    }

    @Test
    void shouldCreateCoordinatesWithNullY() {
        // Given
        CoordinatesRequest request = createCoordinatesRequest(10, null);

        // When & Then
        getClient().post("/coordinates", request)
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.x").isEqualTo(10)
                .jsonPath("$.y").isEmpty();
    }

    @Test
    void shouldGetAllCoordinates() {
        // Given
        Coordinates coordinates1 = createCoordinates(10, 15.5f);
        Coordinates coordinates2 = createCoordinates(20, 25.5f);
        coordinatesRepository.saveAll(List.of(coordinates1, coordinates2));

        // When & Then
        getClient().get("/coordinates")
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].x").isEqualTo(10)
                .jsonPath("$[1].x").isEqualTo(20);
    }

    @Test
    void shouldGetCoordinatesById() {
        // Given
        Coordinates coordinates = coordinatesRepository.save(createCoordinates(10, 15.5f));

        // When & Then
        getClient().get("/coordinates/{id}", coordinates.getId())
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(coordinates.getId())
                .jsonPath("$.x").isEqualTo(10)
                .jsonPath("$.y").isEqualTo(15.5);
    }

    @Test
    void shouldUpdateCoordinatesSuccessfully() {
        // Given
        Coordinates coordinates = coordinatesRepository.save(createCoordinates(10, 15.5f));
        CoordinatesRequest updateRequest = createCoordinatesRequest(50, 55.5f);

        // When & Then
        getClient().putWithBody("/coordinates/{id}", updateRequest, coordinates.getId())
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(coordinates.getId())
                .jsonPath("$.x").isEqualTo(50)
                .jsonPath("$.y").isEqualTo(55.5);

        // Verify in DB
        Coordinates updated = coordinatesRepository.findById(coordinates.getId()).orElseThrow();
        assertThat(updated.getX()).isEqualTo(50);
        assertThat(updated.getY()).isEqualTo(55.5f);
    }

    @Test
    void shouldUpdateCoordinatesWithNullY() {
        // Given
        Coordinates coordinates = coordinatesRepository.save(createCoordinates(10, 15.5f));
        CoordinatesRequest updateRequest = createCoordinatesRequest(50, null);

        // When & Then
        getClient().putWithBody("/coordinates/{id}", updateRequest, coordinates.getId())
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(coordinates.getId())
                .jsonPath("$.x").isEqualTo(50)
                .jsonPath("$.y").isEmpty();

        // Verify in DB
        Coordinates updated = coordinatesRepository.findById(coordinates.getId()).orElseThrow();
        assertThat(updated.getX()).isEqualTo(50);
        assertThat(updated.getY()).isNull();
    }

    @Test
    void shouldDeleteCoordinatesSuccessfully() {
        // Given
        Coordinates coordinates = coordinatesRepository.save(createCoordinates(10, 15.5f));

        // When & Then
        getClient().delete("/coordinates/{id}", coordinates.getId())
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(coordinates.getId())
                .jsonPath("$.x").isEqualTo(10)
                .jsonPath("$.y").isEqualTo(15.5);

        // Verify DB deletion
        assertThat(coordinatesRepository.existsById(coordinates.getId())).isFalse();
    }

    @Test
    void shouldReturnNotFoundWhenGettingNonExistentCoordinates() {
        getClient().get("/coordinates/{id}", 999L)
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").isEqualTo("Ошибка выполнения операции")
                .jsonPath("$.details[0].errorType").isEqualTo("SERVICE_ERROR");
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentCoordinates() {
        CoordinatesRequest updateRequest = createValidCoordinatesRequest();

        getClient().putWithBody("/coordinates/{id}", updateRequest, 999L)
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentCoordinates() {
        getClient().delete("/coordinates/{id}", 999L)
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidCoordinatesRequests")
    void shouldReturnBadRequestWhenCreatingCoordinatesWithInvalidData(
            String ignored, CoordinatesRequest invalidRequest, String expectedErrorField) {

        getClient().post("/coordinates", invalidRequest)
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message").isEqualTo("Ошибка валидации данных")
                .jsonPath("$.details[0].field").isEqualTo(expectedErrorField);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidCoordinatesRequests")
    void shouldReturnBadRequestWhenUpdatingCoordinatesWithInvalidData(
            String ignored, CoordinatesRequest invalidRequest, String expectedErrorField) {

        Coordinates coordinates = coordinatesRepository.save(createCoordinates(10, 15.5f));

        getClient().putWithBody("/coordinates/{id}", invalidRequest, coordinates.getId())
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.details[0].field").isEqualTo(expectedErrorField);
    }

    @Test
    void shouldReturnEmptyListWhenNoCoordinates() {
        getClient().get("/coordinates")
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void shouldCreateCoordinatesWithMinimumValidX() {
        CoordinatesRequest request = createCoordinatesRequest(-146, 15.5f);

        getClient().post("/coordinates", request)
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.x").isEqualTo(-146);
    }

    private CoordinatesRequest createValidCoordinatesRequest() {
        return createCoordinatesRequest(10, 15.5f);
    }

    private Coordinates createCoordinates(Integer x, Float y) {
        return Coordinates.builder()
                .x(x)
                .y(y)
                .build();
    }

}