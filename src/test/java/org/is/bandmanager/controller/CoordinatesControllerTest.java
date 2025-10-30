package org.is.bandmanager.controller;

import org.is.bandmanager.config.IntegrationTest;
import org.is.bandmanager.dto.request.CoordinatesRequest;
import org.is.bandmanager.model.Coordinates;
import org.is.bandmanager.repository.CoordinatesRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CoordinatesControllerTest extends AbstractIntegrationTest {

    @Autowired
    private CoordinatesRepository coordinatesRepository;

    @BeforeEach
    void setUp() {
        coordinatesRepository.deleteAll();
    }

    @AfterAll
    void tearDown() {
        coordinatesRepository.deleteAll();
    }

    @Test
    void shouldCreateCoordinatesSuccessfully() {
        CoordinatesRequest request = CoordinatesRequest.builder().x(10).y(20.5f).build();

        webTestClient.post().uri("/coordinates").contentType(MediaType.APPLICATION_JSON).bodyValue(request).exchange().expectStatus().isCreated().expectBody().jsonPath("$.id").exists().jsonPath("$.x").isEqualTo(10).jsonPath("$.y").isEqualTo(20.5);

        List<Coordinates> all = coordinatesRepository.findAll();
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getX()).isEqualTo(10);
        assertThat(all.get(0).getY()).isEqualTo(20.5f);
    }

    @Test
    void shouldReturnBadRequestWhenCreatingCoordinatesWithNullX() {
        CoordinatesRequest request = CoordinatesRequest.builder().x(null).y(5f).build();

        webTestClient.post().uri("/coordinates").contentType(MediaType.APPLICATION_JSON).bodyValue(request).exchange().expectStatus().isBadRequest().expectBody().jsonPath("$.status").isEqualTo(400).jsonPath("$.message").isEqualTo("Ошибка валидации данных").jsonPath("$.details[0].field").isEqualTo("x").jsonPath("$.details[0].message").isEqualTo("Coordinates.X не может быть пустым").jsonPath("$.details[0].errorType").isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void shouldReturnBadRequestWhenCreatingCoordinatesWithXTooSmall() {
        CoordinatesRequest request = CoordinatesRequest.builder().x(-200).y(5f).build();

        webTestClient.post().uri("/coordinates").contentType(MediaType.APPLICATION_JSON).bodyValue(request).exchange().expectStatus().isBadRequest().expectBody().jsonPath("$.status").isEqualTo(400).jsonPath("$.message").isEqualTo("Ошибка валидации данных").jsonPath("$.details[0].field").isEqualTo("x").jsonPath("$.details[0].message").isEqualTo("Coordinates.X должно быть больше -147").jsonPath("$.details[0].errorType").isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void shouldReturnBadRequestWhenCreatingCoordinatesWithMultipleErrors() {
        CoordinatesRequest request = CoordinatesRequest.builder().x(null).y(null).build();

        webTestClient.post().uri("/coordinates").contentType(MediaType.APPLICATION_JSON).bodyValue(request).exchange().expectStatus().isBadRequest().expectBody().jsonPath("$.status").isEqualTo(400).jsonPath("$.message").isEqualTo("Ошибка валидации данных").jsonPath("$.details.length()").isEqualTo(1).jsonPath("$.details[0].field").isEqualTo("x").jsonPath("$.details[0].message").isEqualTo("Coordinates.X не может быть пустым");
    }

    @Test
    void shouldGetAllCoordinates() {
        Coordinates c1 = Coordinates.builder().x(1).y(1.1f).build();
        Coordinates c2 = Coordinates.builder().x(2).y(2.2f).build();
        coordinatesRepository.saveAll(List.of(c1, c2));

        webTestClient.get().uri("/coordinates").exchange().expectStatus().isOk().expectBody().jsonPath("$.length()").isEqualTo(2).jsonPath("$[0].x").isEqualTo(1).jsonPath("$[0].y").isEqualTo(1.1).jsonPath("$[1].x").isEqualTo(2).jsonPath("$[1].y").isEqualTo(2.2);
    }

    @Test
    void shouldGetCoordinatesById() {
        Coordinates coordinates = Coordinates.builder().x(5).y(5.5f).build();
        Coordinates saved = coordinatesRepository.save(coordinates);

        webTestClient.get().uri("/coordinates/{id}", saved.getId()).exchange().expectStatus().isOk().expectBody().jsonPath("$.id").isEqualTo(saved.getId()).jsonPath("$.x").isEqualTo(5).jsonPath("$.y").isEqualTo(5.5);
    }

    @Test
    void shouldReturnNotFoundWhenGettingNonExistentCoordinates() {
        webTestClient.get().uri("/coordinates/{id}", 999L).exchange().expectStatus().isNotFound().expectBody().jsonPath("$.status").isEqualTo(404).jsonPath("$.message").isEqualTo("Ошибка выполнения операции").jsonPath("$.details[0].field").isEqualTo("service").jsonPath("$.details[0].errorType").isEqualTo("SERVICE_ERROR");
    }

    @Test
    void shouldUpdateCoordinatesSuccessfully() {
        Coordinates c = Coordinates.builder().x(1).y(1.1f).build();
        Coordinates saved = coordinatesRepository.save(c);

        CoordinatesRequest updateRequest = CoordinatesRequest.builder().x(99).y(99.9f).build();

        webTestClient.put().uri("/coordinates/{id}", saved.getId()).contentType(MediaType.APPLICATION_JSON).bodyValue(updateRequest).exchange().expectStatus().isOk().expectBody().jsonPath("$.id").isEqualTo(saved.getId()).jsonPath("$.x").isEqualTo(99).jsonPath("$.y").isEqualTo(99.9);

        Coordinates updated = coordinatesRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getX()).isEqualTo(99);
        assertThat(updated.getY()).isEqualTo(99.9f);
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentCoordinates() {
        CoordinatesRequest updateRequest = CoordinatesRequest.builder().x(10).y(10f).build();

        webTestClient.put().uri("/coordinates/{id}", 999L).contentType(MediaType.APPLICATION_JSON).bodyValue(updateRequest).exchange().expectStatus().isNotFound().expectBody().jsonPath("$.status").isEqualTo(404).jsonPath("$.message").isEqualTo("Ошибка выполнения операции").jsonPath("$.details[0].field").isEqualTo("service").jsonPath("$.details[0].errorType").isEqualTo("SERVICE_ERROR");
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingWithInvalidData() {
        Coordinates c = Coordinates.builder().x(1).y(1.1f).build();
        Coordinates saved = coordinatesRepository.save(c);

        CoordinatesRequest invalidUpdate = CoordinatesRequest.builder().x(null).y(-200f).build();

        webTestClient.put().uri("/coordinates/{id}", saved.getId()).contentType(MediaType.APPLICATION_JSON).bodyValue(invalidUpdate).exchange().expectStatus().isBadRequest().expectBody().jsonPath("$.status").isEqualTo(400).jsonPath("$.message").isEqualTo("Ошибка валидации данных").jsonPath("$.details.length()").isEqualTo(1);
    }

    @Test
    void shouldDeleteCoordinatesSuccessfully() {
        Coordinates c = Coordinates.builder().x(1).y(1.1f).build();
        Coordinates saved = coordinatesRepository.save(c);

        webTestClient.delete().uri("/coordinates/{id}", saved.getId()).exchange().expectStatus().isOk().expectBody().jsonPath("$.id").isEqualTo(saved.getId()).jsonPath("$.x").isEqualTo(1).jsonPath("$.y").isEqualTo(1.1);

        assertThat(coordinatesRepository.existsById(saved.getId())).isFalse();
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentCoordinates() {
        webTestClient.delete().uri("/coordinates/{id}", 999L).exchange().expectStatus().isNotFound().expectBody().jsonPath("$.status").isEqualTo(404).jsonPath("$.message").isEqualTo("Ошибка выполнения операции").jsonPath("$.details[0].field").isEqualTo("service").jsonPath("$.details[0].errorType").isEqualTo("SERVICE_ERROR");
    }
}