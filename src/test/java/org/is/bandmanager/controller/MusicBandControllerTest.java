package org.is.bandmanager.controller;

import org.is.bandmanager.config.IntegrationTest;
import org.is.bandmanager.dto.request.*;
import org.is.bandmanager.model.*;
import org.is.bandmanager.repository.AlbumRepository;
import org.is.bandmanager.repository.CoordinatesRepository;
import org.is.bandmanager.repository.MusicBandRepository;
import org.is.bandmanager.repository.PersonRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.is.bandmanager.model.Color.*;
import static org.is.bandmanager.model.Country.*;
import static org.is.bandmanager.model.MusicGenre.POST_ROCK;
import static org.is.bandmanager.model.MusicGenre.ROCK;

@IntegrationTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MusicBandControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MusicBandRepository musicBandRepository;

    @Autowired
    private CoordinatesRepository coordinatesRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private PersonRepository personRepository;

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
        musicBandRepository.deleteAll();
        coordinatesRepository.deleteAll();
        albumRepository.deleteAll();
        personRepository.deleteAll();
    }

    @AfterAll
    void cleanUp() {
        musicBandRepository.deleteAll();
        coordinatesRepository.deleteAll();
        albumRepository.deleteAll();
        personRepository.deleteAll();
    }

    private CoordinatesRequest createCoordinatesRequest() {
        return CoordinatesRequest.builder()
                .x(10)
                .y(5.5f)
                .build();
    }

    private AlbumRequest createAlbumRequest() {
        return AlbumRequest.builder()
                .name("Best Album")
                .tracks(8L)
                .sales(100)
                .build();
    }

    private PersonRequest createPersonRequest() {
        return PersonRequest.builder()
                .name("John Doe")
                .eyeColor(BLUE)
                .hairColor(BLACK)
                .weight(70f)
                .nationality(USA)
                .location(LocationRequest.builder()
                        .x(5)
                        .y(10L)
                        .z(15L)
                        .build())
                .build();
    }

    private MusicBandRequest createMusicBandRequest() {
        return MusicBandRequest.builder()
                .name("Radiohead")
                .coordinates(createCoordinatesRequest())
                .genre(ROCK)
                .numberOfParticipants(5L)
                .singlesCount(10L)
                .description("Legendary rock band")
                .bestAlbum(createAlbumRequest())
                .albumsCount(9L)
                .establishmentDate(new Date())
                .frontMan(createPersonRequest())
                .build();
    }

    // ==== Тесты ====

    @Test
    void shouldCreateMusicBandSuccessfully() {
        MusicBandRequest request = createMusicBandRequest();

        webTestClient.post()
                .uri("/music-bands")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.name").isEqualTo("Radiohead")
                .jsonPath("$.genre").isEqualTo("ROCK")
                .jsonPath("$.numberOfParticipants").isEqualTo(5)
                .jsonPath("$.bestAlbum.name").isEqualTo("Best Album");

        List<MusicBand> bands = musicBandRepository.findAll();
        assertThat(bands).hasSize(1);
        assertThat(bands.get(0).getName()).isEqualTo("Radiohead");
        assertThat(bands.get(0).getGenre()).isEqualTo(MusicGenre.ROCK);
    }

    @Test
    void shouldReturnBadRequestWhenNameIsBlank() {
        MusicBandRequest request = createMusicBandRequest();
        request.setName("");

        webTestClient.post()
                .uri("/music-bands")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.name").isEqualTo("MusicBand.Name не может быть пустым");
    }

    @Test
    void shouldGetAllMusicBands() {
        Coordinates c1 = Coordinates.builder().x(10).y(5.5f).build();
        Coordinates c2 = Coordinates.builder().x(3254).y(1.3f).build();
        Album a1 = Album.builder().name("Best Album").tracks(8L).sales(100).build();
        Album a2 = Album.builder().name("Really Best Album").tracks(124L).sales(300).build();
        Person p1 = Person.builder()
                .name("Thom Yorke")
                .eyeColor(BLUE)
                .hairColor(BROWN)
                .weight(65f)
                .nationality(UK)
                .location(Location.builder().x(1).y(2L).z(3L).build())
                .build();
        Person p2 = Person.builder()
                .name("Man Yesterday")
                .eyeColor(BLACK)
                .hairColor(BLUE)
                .weight(213f)
                .nationality(FRANCE)
                .location(Location.builder().x(2).y(3L).z(3L).build())
                .build();

        MusicBand b1 = musicBandRepository.save(MusicBand.builder()
                .name("Radiohead")
                .coordinates(c1)
                .genre(ROCK)
                .numberOfParticipants(5L)
                .singlesCount(10L)
                .description("Legendary rock band")
                .bestAlbum(a1)
                .albumsCount(9L)
                .establishmentDate(new Date())
                .frontMan(p1)
                .creationDate(new Date())
                .build());

        MusicBand b2 = musicBandRepository.save(MusicBand.builder()
                .name("Muse")
                .coordinates(c2)
                .genre(POST_ROCK)
                .numberOfParticipants(3L)
                .singlesCount(7L)
                .description("British alt-rock band")
                .bestAlbum(a2)
                .albumsCount(5L)
                .establishmentDate(new Date())
                .frontMan(p2)
                .creationDate(new Date())
                .build());

        webTestClient.get()
                .uri("/music-bands")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].id").isEqualTo(b1.getId())
                .jsonPath("$[1].id").isEqualTo(b2.getId());
    }

    @Test
    void shouldGetMusicBandById() {
        Coordinates c = Coordinates.builder().x(10).y(5.5f).build();
        Album a = Album.builder().name("Album").tracks(5L).sales(50).build();
        Person p = Person.builder()
                .name("John")
                .eyeColor(GREEN)
                .hairColor(BLACK)
                .weight(70f)
                .nationality(USA)
                .location(Location.builder().x(1).y(2L).z(3L).build())
                .build();

        MusicBand saved = musicBandRepository.save(MusicBand.builder()
                .name("Test Band")
                .coordinates(c)
                .genre(ROCK)
                .numberOfParticipants(5L)
                .singlesCount(10L)
                .description("Some description")
                .bestAlbum(a)
                .albumsCount(3L)
                .establishmentDate(new Date())
                .frontMan(p)
                .creationDate(new Date())
                .build());

        webTestClient.get()
                .uri("/music-bands/{id}", saved.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(saved.getId())
                .jsonPath("$.name").isEqualTo("Test Band");
    }

    @Test
    void shouldUpdateMusicBandSuccessfully() {
        MusicBandRequest createRequest = createMusicBandRequest();
        MusicBand saved = musicBandRepository.save(MusicBand.builder()
                .name(createRequest.getName())
                .coordinates(Coordinates.builder().x(10).y(5.5f).build())
                .genre(createRequest.getGenre())
                .numberOfParticipants(createRequest.getNumberOfParticipants())
                .singlesCount(createRequest.getSinglesCount())
                .description(createRequest.getDescription())
                .bestAlbum(Album.builder().name("Old Album").tracks(5L).sales(50).build())
                .albumsCount(createRequest.getAlbumsCount())
                .establishmentDate(new Date())
                .frontMan(Person.builder()
                        .name("Old Frontman")
                        .eyeColor(BROWN)
                        .hairColor(BLACK)
                        .weight(60f)
                        .nationality(USA)
                        .location(Location.builder().x(1).y(2L).z(3L).build())
                        .build())
                .creationDate(new Date())
                .build());

        MusicBandRequest updateRequest = createMusicBandRequest();
        updateRequest.setName("Updated Band");
        updateRequest.getBestAlbum().setName("Updated Album");

        webTestClient.put()
                .uri("/music-bands/{id}", saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Updated Band")
                .jsonPath("$.bestAlbum.name").isEqualTo("Updated Album");

        MusicBand updated = musicBandRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Updated Band");
        assertThat(updated.getBestAlbum().getName()).isEqualTo("Updated Album");
    }

    @Test
    void shouldDeleteMusicBandSuccessfully() {
        Coordinates c = Coordinates.builder().x(10).y(5.5f).build();
        Album a = Album.builder().name("Delete Album").tracks(5L).sales(50).build();
        Person p = Person.builder()
                .name("John")
                .eyeColor(GREEN)
                .hairColor(BLACK)
                .weight(70f)
                .nationality(USA)
                .location(Location.builder().x(1).y(2L).z(3L).build())
                .build();

        MusicBand saved = musicBandRepository.save(MusicBand.builder()
                .name("ToDelete")
                .coordinates(c)
                .genre(ROCK)
                .numberOfParticipants(4L)
                .singlesCount(7L)
                .description("temp")
                .bestAlbum(a)
                .albumsCount(3L)
                .establishmentDate(new Date())
                .frontMan(p)
                .creationDate(new Date())
                .build());

        webTestClient.delete()
                .uri("/music-bands/{id}", saved.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(saved.getId());

        assertThat(musicBandRepository.existsById(saved.getId())).isFalse();
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentBand() {
        webTestClient.delete()
                .uri("/music-bands/{id}", 9999)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").exists();
    }
}
