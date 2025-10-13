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

import java.text.SimpleDateFormat;
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
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message").isEqualTo("Ошибка валидации данных")
                .jsonPath("$.details[0].field").isEqualTo("name")
                .jsonPath("$.details[0].message").isEqualTo("MusicBand.Name не может быть пустым")
                .jsonPath("$.details[0].errorType").isEqualTo("VALIDATION_ERROR");
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
    void shouldReturnNotFoundWhenGettingNonExistentBand() {
        webTestClient.get()
                .uri("/music-bands/{id}", 9999)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").isEqualTo("Ошибка выполнения операции")
                .jsonPath("$.details[0].field").isEqualTo("service")
                .jsonPath("$.details[0].errorType").isEqualTo("SERVICE_ERROR");
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
    void shouldReturnNotFoundWhenUpdatingNonExistentBand() {
        MusicBandRequest updateRequest = createMusicBandRequest();

        webTestClient.put()
                .uri("/music-bands/{id}", 9999)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").isEqualTo("Ошибка выполнения операции")
                .jsonPath("$.details[0].field").isEqualTo("service")
                .jsonPath("$.details[0].errorType").isEqualTo("SERVICE_ERROR");
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
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").isEqualTo("Ошибка выполнения операции")
                .jsonPath("$.details[0].field").isEqualTo("service")
                .jsonPath("$.details[0].errorType").isEqualTo("SERVICE_ERROR");
    }

    @Test
    void shouldGetBandsWithMaxCoordinates() {
        Coordinates c1 = Coordinates.builder().x(100).y(50.5f).build();
        Coordinates c2 = Coordinates.builder().x(50).y(25.5f).build();
        Coordinates c3 = Coordinates.builder().x(100).y(30.5f).build();

        MusicBand b1 = createAndSaveBand("Band 1", c1);
        createAndSaveBand("Band 2", c2);
        createAndSaveBand("Band 3", c3);

        webTestClient.get()
                .uri("/music-bands/max-coordinates")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(b1.getId())
                .jsonPath("$.name").isEqualTo("Band 1")
                .jsonPath("$.coordinates.x").isEqualTo(100)
                .jsonPath("$.coordinates.y").isEqualTo(50.5);
    }

    @Test
    void shouldGetBandsEstablishedBeforeDate() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date oldDate = sdf.parse("2010-01-01");
        Date newDate = sdf.parse("2020-01-01");

        MusicBand oldBand = createAndSaveBandWithDate("Old Band", oldDate);
        createAndSaveBandWithDate("New Band", newDate);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/music-bands/established-before")
                        .queryParam("date", "2015-01-01")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].id").isEqualTo(oldBand.getId())
                .jsonPath("$[0].name").isEqualTo("Old Band");
    }

    @Test
    void shouldReturnBadRequestWhenDateParameterMissing() {
        webTestClient.get()
                .uri("/music-bands/established-before")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message").isEqualTo("Отсутствуют обязательные параметры")
                .jsonPath("$.details[0].field").isEqualTo("date")
                .jsonPath("$.details[0].errorType").isEqualTo("MISSING_PARAMETER");
    }

    @Test
    void shouldReturnBadRequestWhenDateInvalidFormat() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/music-bands/established-before")
                        .queryParam("date", "invalid-date")
                        .build())
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message").isEqualTo("Ошибка в параметрах запроса")
                .jsonPath("$.details[0].field").isEqualTo("date")
                .jsonPath("$.details[0].errorType").isEqualTo("TYPE_MISMATCH");
    }

    @Test
    void shouldReturnEmptyArrayWhenNoBandsFound() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/music-bands/established-before")
                        .queryParam("date", "1900-01-01")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void shouldReturnBadRequestWhenCreatingWithMissingRequestBody() {
        webTestClient.post()
                .uri("/music-bands")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message").isEqualTo("Отсутствует тело запроса")
                .jsonPath("$.details[0].field").isEqualTo("requestBody")
                .jsonPath("$.details[0].errorType").isEqualTo("INVALID_JSON");
    }

    @Test
    void shouldReturnEmptyArrayWhenNoBands() {
        webTestClient.get()
                .uri("/music-bands/unique-albums-count")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }

    private MusicBand createAndSaveBand(String name, Coordinates coordinates) {
        Album album = Album.builder().name(name + " Album").tracks(10L).sales(100).build();
        Person person = Person.builder()
                .name(name + " Frontman")
                .eyeColor(BLUE)
                .hairColor(BLACK)
                .weight(70f)
                .nationality(USA)
                .location(Location.builder().x(1).y(2L).z(3L).build())
                .build();

        return musicBandRepository.save(MusicBand.builder()
                .name(name)
                .coordinates(coordinates)
                .genre(ROCK)
                .numberOfParticipants(5L)
                .singlesCount(10L)
                .description("Description for " + name)
                .bestAlbum(album)
                .albumsCount(5L)
                .establishmentDate(new Date())
                .frontMan(person)
                .creationDate(new Date())
                .build());
    }

    @Test
    void shouldGetUniqueAlbumsCount() {
        createAndSaveBand("Band 1", 5L);
        createAndSaveBand("Band 2", 3L);
        createAndSaveBand("Band 3", 5L);
        createAndSaveBand("Band 4", 7L);

        // When & Then
        webTestClient.get()
                .uri("/music-bands/unique-albums-count")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$[0]").isEqualTo(3)
                .jsonPath("$[1]").isEqualTo(5)
                .jsonPath("$[2]").isEqualTo(7);
    }

    @Test
    void shouldRemoveParticipantFromBand() {
        // Given - группа с 5 участниками
        MusicBand band = createAndSaveBandWithParticipants("Test Band", 5L);

        // When & Then
        webTestClient.put()
                .uri("/music-bands/{id}/remove-participant", band.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(band.getId())
                .jsonPath("$.name").isEqualTo("Test Band")
                .jsonPath("$.numberOfParticipants").isEqualTo(4);

        // Verify in database
        MusicBand updated = musicBandRepository.findById(band.getId()).orElseThrow();
        assertThat(updated.getNumberOfParticipants()).isEqualTo(4L);
    }

    @Test
    void shouldRemoveMultipleParticipants() {
        // Given - группа с 3 участниками
        MusicBand band = createAndSaveBandWithParticipants("Three Member Band", 3L);

        // When - удаляем двух участников
        webTestClient.put()
                .uri("/music-bands/{id}/remove-participant", band.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.numberOfParticipants").isEqualTo(2);

        webTestClient.put()
                .uri("/music-bands/{id}/remove-participant", band.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.numberOfParticipants").isEqualTo(1);

        // Verify final state
        MusicBand updated = musicBandRepository.findById(band.getId()).orElseThrow();
        assertThat(updated.getNumberOfParticipants()).isEqualTo(1L);
    }

    @Test
    void shouldReturnBadRequestWhenRemovingLastParticipant() {
        // Given - группа с 1 участником
        MusicBand band = createAndSaveBandWithParticipants("Solo Band", 1L);

        // When & Then - попытка удалить последнего участника
        webTestClient.put()
                .uri("/music-bands/{id}/remove-participant", band.getId())
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message").isEqualTo("Ошибка выполнения операции")
                .jsonPath("$.details[0].field").isEqualTo("service")
                .jsonPath("$.details[0].message").isEqualTo("Невозможно удалить участника - в группе должен остаться хотя бы 1 участник")
                .jsonPath("$.details[0].errorType").isEqualTo("SERVICE_ERROR");

        MusicBand unchanged = musicBandRepository.findById(band.getId()).orElseThrow();
        assertThat(unchanged.getNumberOfParticipants()).isEqualTo(1L);
    }

    @Test
    void shouldReturnNotFoundWhenRemovingFromNonExistentBand() {
        webTestClient.put()
                .uri("/music-bands/{id}/remove-participant", 9999)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").isEqualTo("Ошибка выполнения операции")
                .jsonPath("$.details[0].field").isEqualTo("service")
                .jsonPath("$.details[0].errorType").isEqualTo("SERVICE_ERROR");
    }

    private MusicBand createAndSaveBandWithParticipants(String name, Long numberOfParticipants) {
        Coordinates coordinates = Coordinates.builder().x(10).y(5.5f).build();
        Album album = Album.builder().name(name + " Album").tracks(10L).sales(100).build();
        Person person = Person.builder()
                .name(name + " Frontman")
                .eyeColor(BLUE)
                .hairColor(BLACK)
                .weight(70f)
                .nationality(USA)
                .location(Location.builder().x(1).y(2L).z(3L).build())
                .build();

        return musicBandRepository.save(MusicBand.builder()
                .name(name)
                .coordinates(coordinates)
                .genre(ROCK)
                .numberOfParticipants(numberOfParticipants)
                .singlesCount(10L)
                .description("Description for " + name)
                .bestAlbum(album)
                .albumsCount(5L)
                .establishmentDate(new Date())
                .frontMan(person)
                .creationDate(new Date())
                .build());
    }

    private MusicBand createAndSaveBandWithDate(String name, Date establishmentDate) {
        Coordinates coordinates = Coordinates.builder().x(10).y(5.5f).build();
        Album album = Album.builder().name(name + " Album").tracks(10L).sales(100).build();
        Person person = Person.builder()
                .name(name + " Frontman")
                .eyeColor(BLUE)
                .hairColor(BLACK)
                .weight(70f)
                .nationality(USA)
                .location(Location.builder().x(1).y(2L).z(3L).build())
                .build();

        return musicBandRepository.save(MusicBand.builder()
                .name(name)
                .coordinates(coordinates)
                .genre(ROCK)
                .numberOfParticipants(5L)
                .singlesCount(10L)
                .description("Description for " + name)
                .bestAlbum(album)
                .albumsCount(5L)
                .establishmentDate(establishmentDate)
                .frontMan(person)
                .creationDate(new Date())
                .build());
    }

    private void createAndSaveBand(String name, Long albumsCount) {
        Coordinates coordinates = Coordinates.builder().x(10).y(5.5f).build();
        Album album = Album.builder().name(name + " Album").tracks(10L).sales(100).build();
        Person person = Person.builder()
                .name(name + " Frontman")
                .eyeColor(BLUE)
                .hairColor(BLACK)
                .weight(70f)
                .nationality(USA)
                .location(Location.builder().x(1).y(2L).z(3L).build())
                .build();

        musicBandRepository.save(MusicBand.builder()
                .name(name)
                .coordinates(coordinates)
                .genre(ROCK)
                .numberOfParticipants(5L)
                .singlesCount(10L)
                .description("Description for " + name)
                .bestAlbum(album)
                .albumsCount(albumsCount)
                .establishmentDate(new Date())
                .frontMan(person)
                .creationDate(new Date())
                .build());
    }

}