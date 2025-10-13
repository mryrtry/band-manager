package org.is.bandmanager.controller;

import org.is.bandmanager.config.IntegrationTest;
import org.is.bandmanager.dto.request.AlbumRequest;
import org.is.bandmanager.model.Album;
import org.is.bandmanager.repository.AlbumRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AlbumControllerTest extends AbstractIntegrationTest {

    @Autowired
    private AlbumRepository albumRepository;

    @LocalServerPort
    private int port;

    private WebTestClient webTestClient;

    @BeforeAll
    void setClient() {
        this.webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @BeforeEach
    void setUp() {
        albumRepository.deleteAll();
    }

    @AfterAll
    void tearDown() {
        albumRepository.deleteAll();
    }

    @Test
    void shouldCreateAlbumSuccessfully() {
        // Given
        AlbumRequest request = AlbumRequest.builder()
                .name("Test Album")
                .tracks(10L)
                .sales(100)
                .build();

        // When & Then
        webTestClient.post()
                .uri("/albums")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.name").isEqualTo("Test Album")
                .jsonPath("$.tracks").isEqualTo(10)
                .jsonPath("$.sales").isEqualTo(100);

        // Verify database
        List<Album> albums = albumRepository.findAll();
        assertThat(albums).hasSize(1);
        assertThat(albums.get(0).getName()).isEqualTo("Test Album");
        assertThat(albums.get(0).getTracks()).isEqualTo(10L);
        assertThat(albums.get(0).getSales()).isEqualTo(100);
    }

    @Test
    void shouldReturnBadRequestWhenCreatingAlbumWithBlankName() {
        // Given
        AlbumRequest request = AlbumRequest.builder()
                .name("") // Invalid
                .tracks(5L)
                .sales(100)
                .build();

        // When & Then
        webTestClient.post()
                .uri("/albums")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Album.Name не может быть пустым");
    }

    @Test
    void shouldReturnBadRequestWhenCreatingAlbumWithNullTracks() {
        // Given
        AlbumRequest request = AlbumRequest.builder()
                .name("Album 1")
                .tracks(null) // Invalid
                .sales(10)
                .build();

        // When & Then
        webTestClient.post()
                .uri("/albums")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.tracks").isEqualTo("Album.Tracks не может быть пустым");
    }

    @Test
    void shouldReturnBadRequestWhenCreatingAlbumWithNegativeSales() {
        // Given
        AlbumRequest request = AlbumRequest.builder()
                .name("Album 1")
                .tracks(5L)
                .sales(-10)
                .build();

        // When & Then
        webTestClient.post()
                .uri("/albums")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.sales").isEqualTo("Album.Sales должно быть > 0");
    }

    @Test
    void shouldGetAllAlbums() {
        // Given
        Album a1 = Album.builder()
                .name("A1")
                .tracks(5L)
                .sales(10)
                .build();
        Album a2 = Album.builder()
                .name("A2")
                .tracks(7L)
                .sales(20)
                .build();
        albumRepository.saveAll(List.of(a1, a2));

        // When & Then
        webTestClient.get()
                .uri("/albums")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].name").isEqualTo("A1")
                .jsonPath("$[1].name").isEqualTo("A2");
    }

    @Test
    void shouldGetAlbumById() {
        // Given
        Album album = albumRepository.save(
                Album.builder().name("Greatest Hits").tracks(15L).sales(500).build()
        );

        // When & Then
        webTestClient.get()
                .uri("/albums/{id}", album.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(album.getId())
                .jsonPath("$.name").isEqualTo("Greatest Hits")
                .jsonPath("$.tracks").isEqualTo(15)
                .jsonPath("$.sales").isEqualTo(500);
    }

    @Test
    void shouldReturnNotFoundWhenGettingNonExistentAlbum() {
        webTestClient.get()
                .uri("/albums/{id}", 999L)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").exists();
    }

    @Test
    void shouldUpdateAlbumSuccessfully() {
        // Given
        Album album = albumRepository.save(
                Album.builder().name("Old Name").tracks(3L).sales(10).build()
        );

        AlbumRequest update = AlbumRequest.builder()
                .name("Updated Name")
                .tracks(9L)
                .sales(99)
                .build();

        // When & Then
        webTestClient.put()
                .uri("/albums/{id}", album.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(update)
                .exchange()
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
    void shouldReturnNotFoundWhenUpdatingNonExistentAlbum() {
        // Given
        AlbumRequest update = AlbumRequest.builder()
                .name("Does not exist")
                .tracks(10L)
                .sales(100)
                .build();

        // When & Then
        webTestClient.put()
                .uri("/albums/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(update)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").exists();
    }

    @Test
    void shouldDeleteAlbumSuccessfully() {
        // Given
        Album album = albumRepository.save(
                Album.builder().name("To Delete").tracks(5L).sales(50).build()
        );

        // When & Then
        webTestClient.delete()
                .uri("/albums/{id}", album.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(album.getId())
                .jsonPath("$.name").isEqualTo("To Delete");

        // Verify DB deletion
        assertThat(albumRepository.existsById(album.getId())).isFalse();
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentAlbum() {
        webTestClient.delete()
                .uri("/albums/{id}", 999L)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").exists();
    }

}
