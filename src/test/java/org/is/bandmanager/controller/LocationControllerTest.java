package org.is.bandmanager.controller;

import org.is.bandmanager.config.IntegrationTest;
import org.is.bandmanager.dto.request.LocationRequest;
import org.is.bandmanager.model.Location;
import org.is.bandmanager.repository.LocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class LocationControllerTest extends AbstractIntegrationTest {

    private WebTestClient webTestClient;

    @Autowired
    private LocationRepository locationRepository;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        this.webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();

        locationRepository.deleteAll();
    }

    @Test
    void shouldCreateLocationSuccessfully() {
        // Given
        LocationRequest request = LocationRequest.builder()
                .x(10)
                .y(20L)
                .z(30L)
                .build();

        // When & Then
        webTestClient.post()
                .uri("/api/locations")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.x").isEqualTo(10)
                .jsonPath("$.y").isEqualTo(20)
                .jsonPath("$.z").isEqualTo(30);

        // Verify database
        List<Location> locations = locationRepository.findAll();
        assertThat(locations).hasSize(1);
        assertThat(locations.get(0).getX()).isEqualTo(10);
        assertThat(locations.get(0).getY()).isEqualTo(20L);
        assertThat(locations.get(0).getZ()).isEqualTo(30L);
    }

    @Test
    void shouldReturnBadRequestWhenCreatingLocationWithNullY() {
        // Given
        LocationRequest request = LocationRequest.builder()
                .x(10)
                .y(null) // Invalid - null
                .z(30L)
                .build();

        // When & Then
        webTestClient.post()
                .uri("/api/locations")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.y").isEqualTo("Location.Y не может быть пустым");
    }

    @Test
    void shouldReturnBadRequestWhenCreatingLocationWithNullZ() {
        // Given
        LocationRequest request = LocationRequest.builder()
                .x(10)
                .y(20L)
                .z(null) // Invalid - null
                .build();

        // When & Then
        webTestClient.post()
                .uri("/api/locations")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.z").isEqualTo("Location.Z не может быть пустым");
    }

    @Test
    void shouldGetAllLocations() {
        // Given
        Location location1 = Location.builder()
                .x(1)
                .y(10L)
                .z(100L)
                .build();

        Location location2 = Location.builder()
                .x(2)
                .y(20L)
                .z(200L)
                .build();

        locationRepository.saveAll(List.of(location1, location2));

        // When & Then
        webTestClient.get()
                .uri("/api/locations")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].x").isEqualTo(1)
                .jsonPath("$[0].y").isEqualTo(10)
                .jsonPath("$[0].z").isEqualTo(100)
                .jsonPath("$[1].x").isEqualTo(2)
                .jsonPath("$[1].y").isEqualTo(20)
                .jsonPath("$[1].z").isEqualTo(200);
    }

    @Test
    void shouldGetLocationById() {
        // Given
        Location location = Location.builder()
                .x(5)
                .y(50L)
                .z(500L)
                .build();
        Location savedLocation = locationRepository.save(location);

        // When & Then
        webTestClient.get()
                .uri("/api/locations/{id}", savedLocation.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(savedLocation.getId())
                .jsonPath("$.x").isEqualTo(5)
                .jsonPath("$.y").isEqualTo(50)
                .jsonPath("$.z").isEqualTo(500);
    }

    @Test
    void shouldReturnNotFoundWhenGettingNonExistentLocation() {
        // When & Then
        webTestClient.get()
                .uri("/api/locations/{id}", 999L)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").exists();
    }

    @Test
    void shouldUpdateLocationSuccessfully() {
        // Given
        Location location = Location.builder()
                .x(1)
                .y(10L)
                .z(100L)
                .build();
        Location savedLocation = locationRepository.save(location);

        LocationRequest updateRequest = LocationRequest.builder()
                .x(99)
                .y(999L)
                .z(9999L)
                .build();

        // When & Then
        webTestClient.put()
                .uri("/api/locations/{id}", savedLocation.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(savedLocation.getId())
                .jsonPath("$.x").isEqualTo(99)
                .jsonPath("$.y").isEqualTo(999)
                .jsonPath("$.z").isEqualTo(9999);

        // Verify database update
        Location updatedLocation = locationRepository.findById(savedLocation.getId()).orElseThrow();
        assertThat(updatedLocation.getX()).isEqualTo(99);
        assertThat(updatedLocation.getY()).isEqualTo(999L);
        assertThat(updatedLocation.getZ()).isEqualTo(9999L);
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentLocation() {
        // Given
        LocationRequest updateRequest = LocationRequest.builder()
                .x(99)
                .y(999L)
                .z(9999L)
                .build();

        // When & Then
        webTestClient.put()
                .uri("/api/locations/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").exists();
    }

    @Test
    void shouldDeleteLocationSuccessfully() {
        // Given
        Location location = Location.builder()
                .x(1)
                .y(10L)
                .z(100L)
                .build();
        Location savedLocation = locationRepository.save(location);

        // When & Then
        webTestClient.delete()
                .uri("/api/locations/{id}", savedLocation.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(savedLocation.getId())
                .jsonPath("$.x").isEqualTo(1)
                .jsonPath("$.y").isEqualTo(10)
                .jsonPath("$.z").isEqualTo(100);

        // Verify database deletion
        assertThat(locationRepository.existsById(savedLocation.getId())).isFalse();
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentLocation() {
        // When & Then
        webTestClient.delete()
                .uri("/api/locations/{id}", 999L)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").exists();
    }

    @Test
    void shouldCreateLocationWithNullX() {
        // Given - X can be null according to your model
        LocationRequest request = LocationRequest.builder()
                .x(null) // This is allowed
                .y(20L)
                .z(30L)
                .build();

        // When & Then
        webTestClient.post()
                .uri("/api/locations")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.x").isEmpty()
                .jsonPath("$.y").isEqualTo(20)
                .jsonPath("$.z").isEqualTo(30);
    }

}