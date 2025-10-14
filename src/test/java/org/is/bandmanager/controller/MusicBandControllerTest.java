package org.is.bandmanager.controller;

import org.is.bandmanager.config.IntegrationTest;
import org.is.bandmanager.dto.request.MusicBandRequest;
import org.is.bandmanager.model.*;
import org.is.bandmanager.repository.*;
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

    @Autowired
    private LocationRepository locationRepository;

    @LocalServerPort
    private int port;

    private WebTestClient webTestClient;

    private Coordinates savedCoordinates;
    private Album savedAlbum;
    private Person savedPerson;

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
        locationRepository.deleteAll();

        savedCoordinates = coordinatesRepository.save(Coordinates.builder()
                .x(10)
                .y(5.5f)
                .build());

        savedAlbum = albumRepository.save(Album.builder()
                .name("Best Album")
                .tracks(8L)
                .sales(100)
                .build());

        Location location = locationRepository.save(Location.builder()
                .x(5)
                .y(10L)
                .z(15L)
                .build());

        savedPerson = personRepository.save(Person.builder()
                .name("John Doe")
                .eyeColor(BLUE)
                .hairColor(BLACK)
                .location(location)
                .weight(70f)
                .nationality(USA)
                .build());
    }

    @AfterAll
    void cleanUp() {
        musicBandRepository.deleteAll();
        coordinatesRepository.deleteAll();
        albumRepository.deleteAll();
        personRepository.deleteAll();
        locationRepository.deleteAll();
    }

    private MusicBandRequest createMusicBandRequest() {
        return MusicBandRequest.builder()
                .name("Radiohead")
                .coordinatesId(savedCoordinates.getId())
                .genre(ROCK)
                .numberOfParticipants(5L)
                .singlesCount(10L)
                .description("Legendary rock band")
                .bestAlbumId(savedAlbum.getId())
                .albumsCount(9L)
                .establishmentDate(new Date())
                .frontManId(savedPerson.getId())
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
                .jsonPath("$.coordinatesId").isEqualTo(savedCoordinates.getId())
                .jsonPath("$.bestAlbumId").isEqualTo(savedAlbum.getId())
                .jsonPath("$.frontManId").isEqualTo(savedPerson.getId());

        List<MusicBand> bands = musicBandRepository.findAll();
        assertThat(bands).hasSize(1);
        assertThat(bands.get(0).getName()).isEqualTo("Radiohead");
        assertThat(bands.get(0).getGenre()).isEqualTo(MusicGenre.ROCK);
        assertThat(bands.get(0).getCoordinates().getId()).isEqualTo(savedCoordinates.getId());
        assertThat(bands.get(0).getBestAlbum().getId()).isEqualTo(savedAlbum.getId());
        assertThat(bands.get(0).getFrontMan().getId()).isEqualTo(savedPerson.getId());
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
    void shouldReturnBadRequestWhenCoordinatesIdIsNull() {
        MusicBandRequest request = createMusicBandRequest();
        request.setCoordinatesId(null);

        webTestClient.post()
                .uri("/music-bands")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message").isEqualTo("Ошибка валидации данных")
                .jsonPath("$.details[0].field").isEqualTo("coordinatesId")
                .jsonPath("$.details[0].message").isEqualTo("MusicBand.CoordinatesId не может быть пустым")
                .jsonPath("$.details[0].errorType").isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void shouldReturnNotFoundWhenCoordinatesIdDoesNotExist() {

        MusicBandRequest request = createMusicBandRequest();

        request.setCoordinatesId(999L);

        webTestClient.post()
                .uri("/music-bands")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404);
    }

    @Test
    void shouldGetAllMusicBands() {

        Coordinates c2 = coordinatesRepository.save(Coordinates.builder().x(3254).y(1.3f).build());

        Album a2 = albumRepository.save(Album.builder().name("Really Best Album").tracks(124L).sales(300).build());

        Location location2 = locationRepository.save(Location.builder().x(2).y(3L).z(3L).build());

        Person p2 = personRepository.save(Person.builder()
                .name("Man Yesterday")
                .eyeColor(BLACK)
                .hairColor(BLUE)
                .location(location2)
                .weight(213f)
                .nationality(FRANCE)
                .build());

        MusicBand b1 = musicBandRepository.save(MusicBand.builder()
                .name("Radiohead")
                .coordinates(savedCoordinates)
                .genre(ROCK)
                .numberOfParticipants(5L)
                .singlesCount(10L)
                .description("Legendary rock band")
                .bestAlbum(savedAlbum)
                .albumsCount(9L)
                .establishmentDate(new Date())
                .frontMan(savedPerson)
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
                .jsonPath("$.content.length()").isEqualTo(2)
                .jsonPath("$.content[0].id").isEqualTo(b1.getId())
                .jsonPath("$.content[1].id").isEqualTo(b2.getId());
    }

    @Test
    void shouldGetMusicBandById() {
        MusicBand saved = musicBandRepository.save(MusicBand.builder()
                .name("Test Band")
                .coordinates(savedCoordinates)
                .genre(ROCK)
                .numberOfParticipants(5L)
                .singlesCount(10L)
                .description("Some description")
                .bestAlbum(savedAlbum)
                .albumsCount(3L)
                .establishmentDate(new Date())
                .frontMan(savedPerson)
                .creationDate(new Date())
                .build());

        webTestClient.get()
                .uri("/music-bands/{id}", saved.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(saved.getId())
                .jsonPath("$.name").isEqualTo("Test Band")
                .jsonPath("$.coordinatesId").isEqualTo(savedCoordinates.getId())
                .jsonPath("$.bestAlbumId").isEqualTo(savedAlbum.getId())
                .jsonPath("$.frontManId").isEqualTo(savedPerson.getId());
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
        MusicBand saved = musicBandRepository.save(MusicBand.builder()
                .name("Old Band")
                .coordinates(savedCoordinates)
                .genre(ROCK)
                .numberOfParticipants(5L)
                .singlesCount(10L)
                .description("Old description")
                .bestAlbum(savedAlbum)
                .albumsCount(9L)
                .establishmentDate(new Date())
                .frontMan(savedPerson)
                .creationDate(new Date())
                .build());

        Coordinates newCoordinates = coordinatesRepository.save(Coordinates.builder().x(100).y(50.5f).build());

        Album newAlbum = albumRepository.save(Album.builder().name("New Album").tracks(15L).sales(200).build());

        Location newLocation = locationRepository.save(Location.builder().x(10).y(20L).z(30L).build());

        Person newPerson = personRepository.save(Person.builder()
                .name("New Frontman")
                .eyeColor(GREEN)
                .hairColor(BROWN)
                .location(newLocation)
                .weight(80f)
                .nationality(UK)
                .build());

        MusicBandRequest updateRequest = MusicBandRequest.builder()
                .name("Updated Band")
                .coordinatesId(newCoordinates.getId())
                .genre(POST_ROCK)
                .numberOfParticipants(8L)
                .singlesCount(15L)
                .description("Updated description")
                .bestAlbumId(newAlbum.getId())
                .albumsCount(12L)
                .establishmentDate(new Date())
                .frontManId(newPerson.getId())
                .build();

        webTestClient.put()
                .uri("/music-bands/{id}", saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Updated Band")
                .jsonPath("$.coordinatesId").isEqualTo(newCoordinates.getId())
                .jsonPath("$.bestAlbumId").isEqualTo(newAlbum.getId())
                .jsonPath("$.frontManId").isEqualTo(newPerson.getId());

        MusicBand updated = musicBandRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Updated Band");
        assertThat(updated.getCoordinates().getId()).isEqualTo(newCoordinates.getId());
        assertThat(updated.getBestAlbum().getId()).isEqualTo(newAlbum.getId());
        assertThat(updated.getFrontMan().getId()).isEqualTo(newPerson.getId());
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
        MusicBand saved = musicBandRepository.save(MusicBand.builder()
                .name("ToDelete")
                .coordinates(savedCoordinates)
                .genre(ROCK)
                .numberOfParticipants(4L)
                .singlesCount(7L)
                .description("temp")
                .bestAlbum(savedAlbum)
                .albumsCount(3L)
                .establishmentDate(new Date())
                .frontMan(savedPerson)
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
        Coordinates c1 = coordinatesRepository.save(Coordinates.builder().x(100).y(50.5f).build());
        Coordinates c2 = coordinatesRepository.save(Coordinates.builder().x(50).y(25.5f).build());
        Coordinates c3 = coordinatesRepository.save(Coordinates.builder().x(100).y(30.5f).build());

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
                .jsonPath("$.coordinatesId").isEqualTo(c1.getId());
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
    void shouldGetUniqueAlbumsCount() {
        createAndSaveBand("Band 1", 5L);
        createAndSaveBand("Band 2", 3L);
        createAndSaveBand("Band 3", 5L);
        createAndSaveBand("Band 4", 7L);

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
        MusicBand band = createAndSaveBandWithParticipants("Test Band", 5L);

        webTestClient.put()
                .uri("/music-bands/{id}/remove-participant", band.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(band.getId())
                .jsonPath("$.name").isEqualTo("Test Band")
                .jsonPath("$.numberOfParticipants").isEqualTo(4);

        MusicBand updated = musicBandRepository.findById(band.getId()).orElseThrow();
        assertThat(updated.getNumberOfParticipants()).isEqualTo(4L);
    }

    @Test
    void shouldReturnBadRequestWhenRemovingLastParticipant() {
        MusicBand band = createAndSaveBandWithParticipants("Solo Band", 1L);

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

    // Вспомогательные методы
    private MusicBand createAndSaveBand(String name, Coordinates coordinates) {
        return musicBandRepository.save(MusicBand.builder()
                .name(name)
                .coordinates(coordinates)
                .genre(ROCK)
                .numberOfParticipants(5L)
                .singlesCount(10L)
                .description("Description for " + name)
                .bestAlbum(savedAlbum)
                .albumsCount(5L)
                .establishmentDate(new Date())
                .frontMan(savedPerson)
                .creationDate(new Date())
                .build());
    }

    private MusicBand createAndSaveBandWithParticipants(String name, Long numberOfParticipants) {
        return musicBandRepository.save(MusicBand.builder()
                .name(name)
                .coordinates(savedCoordinates)
                .genre(ROCK)
                .numberOfParticipants(numberOfParticipants)
                .singlesCount(10L)
                .description("Description for " + name)
                .bestAlbum(savedAlbum)
                .albumsCount(5L)
                .establishmentDate(new Date())
                .frontMan(savedPerson)
                .creationDate(new Date())
                .build());
    }

    private MusicBand createAndSaveBandWithDate(String name, Date establishmentDate) {
        return musicBandRepository.save(MusicBand.builder()
                .name(name)
                .coordinates(savedCoordinates)
                .genre(ROCK)
                .numberOfParticipants(5L)
                .singlesCount(10L)
                .description("Description for " + name)
                .bestAlbum(savedAlbum)
                .albumsCount(5L)
                .establishmentDate(establishmentDate)
                .frontMan(savedPerson)
                .creationDate(new Date())
                .build());
    }

    private void createAndSaveBand(String name, Long albumsCount) {
        musicBandRepository.save(MusicBand.builder()
                .name(name)
                .coordinates(savedCoordinates)
                .genre(ROCK)
                .numberOfParticipants(5L)
                .singlesCount(10L)
                .description("Description for " + name)
                .bestAlbum(savedAlbum)
                .albumsCount(albumsCount)
                .establishmentDate(new Date())
                .frontMan(savedPerson)
                .creationDate(new Date())
                .build());
    }

}