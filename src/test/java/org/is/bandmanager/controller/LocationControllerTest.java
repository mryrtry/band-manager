package org.is.bandmanager.controller;

import org.is.bandmanager.config.IntegrationTest;
import org.is.bandmanager.dto.request.LocationRequest;
import org.is.bandmanager.model.Location;
import org.is.bandmanager.model.Person;
import org.is.bandmanager.repository.LocationRepository;
import org.is.bandmanager.repository.PersonRepository;
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
import static org.is.bandmanager.util.IntegrationTestUtil.performDelete;
import static org.is.bandmanager.util.IntegrationTestUtil.performGet;
import static org.is.bandmanager.util.IntegrationTestUtil.performPost;
import static org.is.bandmanager.util.IntegrationTestUtil.performPutWithBody;

@IntegrationTest
class LocationControllerTest extends AbstractIntegrationTest {

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private PersonRepository personRepository;

    private static Stream<Arguments> provideInvalidLocationRequests() {
        return Stream.of(
                Arguments.of("Null Y coordinate",
                        createLocationRequest(10, null, 20L), "y"),
                Arguments.of("Null Z coordinate",
                        createLocationRequest(10, 15L, null), "z")
        );
    }

    private static LocationRequest createLocationRequest(Integer x, Long y, Long z) {
        return LocationRequest.builder()
                .x(x)
                .y(y)
                .z(z)
                .build();
    }

    @BeforeEach
    @AfterEach
    void cleanDatabase() {
        personRepository.deleteAll();
        locationRepository.deleteAll();
    }

    @Test
    void shouldCreateLocationSuccessfully() {
        // Given
        LocationRequest request = createValidLocationRequest();

        // When & Then
        performPost(webTestClient, "/locations", request)
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.x").isEqualTo(10)
                .jsonPath("$.y").isEqualTo(15)
                .jsonPath("$.z").isEqualTo(20);

        // Verify database
        assertThat(locationRepository.findAll()).hasSize(1);
    }

    @Test
    void shouldCreateLocationWithNullX() {
        // Given
        LocationRequest request = createLocationRequest(null, 15L, 20L);

        // When & Then
        performPost(webTestClient, "/locations", request)
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.x").isEmpty()
                .jsonPath("$.y").isEqualTo(15)
                .jsonPath("$.z").isEqualTo(20);
    }

    @Test
    void shouldGetAllLocations() {
        // Given
        Location location1 = createLocation(10, 15L, 20L);
        Location location2 = createLocation(20, 25L, 30L);
        locationRepository.saveAll(List.of(location1, location2));

        // When & Then
        performGet(webTestClient, "/locations")
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].x").isEqualTo(10)
                .jsonPath("$[1].x").isEqualTo(20);
    }

    @Test
    void shouldGetLocationById() {
        // Given
        Location location = locationRepository.save(createLocation(10, 15L, 20L));

        // When & Then
        performGet(webTestClient, "/locations/{id}", location.getId())
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(location.getId())
                .jsonPath("$.x").isEqualTo(10)
                .jsonPath("$.y").isEqualTo(15)
                .jsonPath("$.z").isEqualTo(20);
    }

    @Test
    void shouldUpdateLocationSuccessfully() {
        // Given
        Location location = locationRepository.save(createLocation(10, 15L, 20L));
        LocationRequest updateRequest = createLocationRequest(50, 55L, 60L);

        // When & Then
        performPutWithBody(webTestClient, "/locations/{id}", updateRequest, location.getId())
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(location.getId())
                .jsonPath("$.x").isEqualTo(50)
                .jsonPath("$.y").isEqualTo(55)
                .jsonPath("$.z").isEqualTo(60);

        // Verify in DB
        Location updated = locationRepository.findById(location.getId()).orElseThrow();
        assertThat(updated.getX()).isEqualTo(50);
        assertThat(updated.getY()).isEqualTo(55L);
        assertThat(updated.getZ()).isEqualTo(60L);
    }

    @Test
    void shouldUpdateLocationWithNullX() {
        // Given
        Location location = locationRepository.save(createLocation(10, 15L, 20L));
        LocationRequest updateRequest = createLocationRequest(null, 55L, 60L);

        // When & Then
        performPutWithBody(webTestClient, "/locations/{id}", updateRequest, location.getId())
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(location.getId())
                .jsonPath("$.x").isEmpty()
                .jsonPath("$.y").isEqualTo(55)
                .jsonPath("$.z").isEqualTo(60);

        // Verify in DB
        Location updated = locationRepository.findById(location.getId()).orElseThrow();
        assertThat(updated.getX()).isNull();
        assertThat(updated.getY()).isEqualTo(55L);
        assertThat(updated.getZ()).isEqualTo(60L);
    }

    @Test
    void shouldDeleteLocationSuccessfully() {
        // Given
        Location location = locationRepository.save(createLocation(10, 15L, 20L));

        // When & Then
        performDelete(webTestClient, "/locations/{id}", location.getId())
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(location.getId())
                .jsonPath("$.x").isEqualTo(10)
                .jsonPath("$.y").isEqualTo(15)
                .jsonPath("$.z").isEqualTo(20);

        // Verify DB deletion
        assertThat(locationRepository.existsById(location.getId())).isFalse();
    }

    @Test
    void shouldReturnNotFoundWhenGettingNonExistentLocation() {
        performGet(webTestClient, "/locations/{id}", 999L)
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").isEqualTo("Ошибка выполнения операции")
                .jsonPath("$.details[0].errorType").isEqualTo("SERVICE_ERROR");
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentLocation() {
        LocationRequest updateRequest = createValidLocationRequest();

        performPutWithBody(webTestClient, "/locations/{id}", updateRequest, 999L)
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentLocation() {
        performDelete(webTestClient, "/locations/{id}", 999L)
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404);
    }

    @Test
    void shouldReturnBadRequestWhenDeletingLocationInUse() {
        // Given
        Location location = locationRepository.save(createLocation(10, 15L, 20L));
        Person person = createPersonWithLocation(location);
        personRepository.save(person);

        // When & Then
        performDelete(webTestClient, "/locations/{id}", location.getId())
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message").isEqualTo("Ошибка выполнения операции")
                .jsonPath("$.details[0].errorType").isEqualTo("SERVICE_ERROR");
    }

    @ParameterizedTest
    @MethodSource("provideInvalidLocationRequests")
    void shouldReturnBadRequestWhenCreatingLocationWithInvalidData(
            String ignored, LocationRequest invalidRequest, String expectedErrorField) {

        performPost(webTestClient, "/locations", invalidRequest)
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message").isEqualTo("Ошибка валидации данных")
                .jsonPath("$.details[0].field").isEqualTo(expectedErrorField);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidLocationRequests")
    void shouldReturnBadRequestWhenUpdatingLocationWithInvalidData(
            String ignored, LocationRequest invalidRequest, String expectedErrorField) {

        Location location = locationRepository.save(createLocation(10, 15L, 20L));

        performPutWithBody(webTestClient, "/locations/{id}", invalidRequest, location.getId())
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.details[0].field").isEqualTo(expectedErrorField);
    }

    @Test
    void shouldReturnEmptyListWhenNoLocations() {
        performGet(webTestClient, "/locations")
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }

    private LocationRequest createValidLocationRequest() {
        return createLocationRequest(10, 15L, 20L);
    }

    private Location createLocation(Integer x, Long y, Long z) {
        return Location.builder()
                .x(x)
                .y(y)
                .z(z)
                .build();
    }

    private Person createPersonWithLocation(Location location) {
        return Person.builder()
                .name("Test Person")
                .eyeColor(org.is.bandmanager.model.Color.BLUE)
                .hairColor(org.is.bandmanager.model.Color.BROWN)
                .location(location)
                .weight(75.5f)
                .nationality(org.is.bandmanager.model.Country.USA)
                .build();
    }

}