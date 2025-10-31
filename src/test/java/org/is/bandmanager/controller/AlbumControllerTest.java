package org.is.bandmanager.controller;

import org.is.bandmanager.config.IntegrationTest;
import org.is.bandmanager.dto.request.AlbumRequest;
import org.is.bandmanager.model.Album;
import org.is.bandmanager.repository.AlbumRepository;
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
class AlbumControllerTest extends AbstractIntegrationTest {

    @Autowired
    private AlbumRepository albumRepository;

    private static Stream<Arguments> provideInvalidAlbumRequests() {
        return Stream.of(
                Arguments.of("Blank name",
                        createAlbumRequest("", 5L, 100), "name"),
                Arguments.of("Null tracks",
                        createAlbumRequest("Album 1", null, 100), "tracks"),
                Arguments.of("Zero tracks",
                        createAlbumRequest("Album 1", 0L, 100), "tracks"),
                Arguments.of("Negative tracks",
                        createAlbumRequest("Album 1", -5L, 100), "tracks"),
                Arguments.of("Zero sales",
                        createAlbumRequest("Album 1", 5L, 0), "sales"),
                Arguments.of("Negative sales",
                        createAlbumRequest("Album 1", 5L, -10), "sales")
        );
    }

    private static AlbumRequest createAlbumRequest(String name, Long tracks, Integer sales) {
        return AlbumRequest.builder()
                .name(name)
                .tracks(tracks)
                .sales(sales)
                .build();
    }

    @BeforeEach
    @AfterEach
    void cleanDatabase() {
        albumRepository.deleteAll();
    }

    @Test
    void shouldCreateAlbumSuccessfully() {
        // Given
        AlbumRequest request = createValidAlbumRequest();

        // When & Then
        performPost(webTestClient, "/albums", request)
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.name").isEqualTo("Test Album")
                .jsonPath("$.tracks").isEqualTo(10)
                .jsonPath("$.sales").isEqualTo(100);

        // Verify database
        assertThat(albumRepository.findAll()).hasSize(1);
    }

    @Test
    void shouldGetAllAlbums() {
        // Given
        Album album1 = createAlbum("Album 1", 5L, 50);
        Album album2 = createAlbum("Album 2", 8L, 80);
        albumRepository.saveAll(List.of(album1, album2));

        // When & Then
        performGet(webTestClient, "/albums")
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].name").isEqualTo("Album 1")
                .jsonPath("$[1].name").isEqualTo("Album 2");
    }

    @Test
    void shouldGetAlbumById() {
        // Given
        Album album = albumRepository.save(createAlbum("Greatest Hits", 15L, 500));

        // When & Then
        performGet(webTestClient, "/albums/{id}", album.getId())
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(album.getId())
                .jsonPath("$.name").isEqualTo("Greatest Hits")
                .jsonPath("$.tracks").isEqualTo(15)
                .jsonPath("$.sales").isEqualTo(500);
    }

    @Test
    void shouldUpdateAlbumSuccessfully() {
        // Given
        Album album = albumRepository.save(createAlbum("Old Name", 3L, 10));
        AlbumRequest updateRequest = createAlbumRequest("Updated Name", 9L, 99);

        // When & Then
        performPutWithBody(webTestClient, "/albums/{id}", updateRequest, album.getId())
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(album.getId())
                .jsonPath("$.name").isEqualTo("Updated Name")
                .jsonPath("$.tracks").isEqualTo(9)
                .jsonPath("$.sales").isEqualTo(99);

        // Verify in DB
        Album updated = albumRepository.findById(album.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(updated.getTracks()).isEqualTo(9L);
        assertThat(updated.getSales()).isEqualTo(99);
    }

    @Test
    void shouldDeleteAlbumSuccessfully() {
        // Given
        Album album = albumRepository.save(createAlbum("To Delete", 5L, 50));

        // When & Then
        performDelete(webTestClient, "/albums/{id}", album.getId())
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(album.getId())
                .jsonPath("$.name").isEqualTo("To Delete");

        // Verify DB deletion
        assertThat(albumRepository.existsById(album.getId())).isFalse();
    }

    @Test
    void shouldReturnNotFoundWhenGettingNonExistentAlbum() {
        performGet(webTestClient, "/albums/{id}", 999L)
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").isEqualTo("Ошибка выполнения операции")
                .jsonPath("$.details[0].errorType").isEqualTo("SERVICE_ERROR");
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentAlbum() {
        AlbumRequest updateRequest = createValidAlbumRequest();

        performPutWithBody(webTestClient, "/albums/{id}", updateRequest, 999L)
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentAlbum() {
        performDelete(webTestClient, "/albums/{id}", 999L)
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidAlbumRequests")
    void shouldReturnBadRequestWhenCreatingAlbumWithInvalidData(
            String ignored, AlbumRequest invalidRequest, String expectedErrorField) {

        performPost(webTestClient, "/albums", invalidRequest)
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message").isEqualTo("Ошибка валидации данных")
                .jsonPath("$.details[0].field").isEqualTo(expectedErrorField);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidAlbumRequests")
    void shouldReturnBadRequestWhenUpdatingAlbumWithInvalidData(
            String ignored, AlbumRequest invalidRequest, String expectedErrorField) {

        Album album = albumRepository.save(createAlbum("Valid Album", 5L, 50));

        performPutWithBody(webTestClient, "/albums/{id}", invalidRequest, album.getId())
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.details[0].field").isEqualTo(expectedErrorField);
    }

    @Test
    void shouldReturnBadRequestWhenCreatingAlbumWithMultipleErrors() {
        AlbumRequest request = createAlbumRequest("", null, -5);

        performPost(webTestClient, "/albums", request)
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.details.length()").isEqualTo(3);
    }

    private AlbumRequest createValidAlbumRequest() {
        return createAlbumRequest("Test Album", 10L, 100);
    }

    private Album createAlbum(String name, Long tracks, Integer sales) {
        return Album.builder()
                .name(name)
                .tracks(tracks)
                .sales(sales)
                .build();
    }

}