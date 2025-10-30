package org.is.bandmanager.controller;

import org.is.bandmanager.config.IntegrationTest;
import org.is.bandmanager.dto.request.MusicBandRequest;
import org.is.bandmanager.model.Album;
import org.is.bandmanager.model.Coordinates;
import org.is.bandmanager.model.Location;
import org.is.bandmanager.model.MusicBand;
import org.is.bandmanager.model.MusicGenre;
import org.is.bandmanager.model.Person;
import org.is.bandmanager.repository.AlbumRepository;
import org.is.bandmanager.repository.CoordinatesRepository;
import org.is.bandmanager.repository.LocationRepository;
import org.is.bandmanager.repository.MusicBandRepository;
import org.is.bandmanager.repository.PersonRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.is.bandmanager.model.Color.BLACK;
import static org.is.bandmanager.model.Color.BLUE;
import static org.is.bandmanager.model.Color.BROWN;
import static org.is.bandmanager.model.Color.GREEN;
import static org.is.bandmanager.model.Country.FRANCE;
import static org.is.bandmanager.model.Country.UK;
import static org.is.bandmanager.model.Country.USA;
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

    private Coordinates savedCoordinates;
    private Album savedAlbum;
    private Person savedPerson;

    @BeforeEach
    void setUp() {
        musicBandRepository.deleteAll();
        coordinatesRepository.deleteAll();
        albumRepository.deleteAll();
        personRepository.deleteAll();
        locationRepository.deleteAll();

        savedCoordinates = coordinatesRepository.save(Coordinates.builder().x(10).y(5.5f).build());

        savedAlbum = albumRepository.save(Album.builder().name("Best Album").tracks(8L).sales(100).build());

        Location location = locationRepository.save(Location.builder().x(5).y(10L).z(15L).build());

        savedPerson = personRepository.save(Person.builder().name("John Doe").eyeColor(BLUE).hairColor(BLACK).location(location).weight(70f).nationality(USA).build());
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
        return MusicBandRequest.builder().name("Radiohead").coordinatesId(savedCoordinates.getId()).genre(ROCK).numberOfParticipants(5L).singlesCount(10L).description("Legendary rock band").bestAlbumId(savedAlbum.getId()).albumsCount(9L).establishmentDate(new Date()).frontManId(savedPerson.getId()).build();
    }

    @Test
    void shouldCreateMusicBandSuccessfully() {
        MusicBandRequest request = createMusicBandRequest();

        webTestClient.post().uri("/music-bands").contentType(MediaType.APPLICATION_JSON).bodyValue(request).exchange().expectStatus().isCreated().expectBody().jsonPath("$.id").exists().jsonPath("$.name").isEqualTo("Radiohead").jsonPath("$.genre").isEqualTo("ROCK").jsonPath("$.numberOfParticipants").isEqualTo(5).jsonPath("$.coordinates.id").isEqualTo(savedCoordinates.getId()).jsonPath("$.bestAlbum.id").isEqualTo(savedAlbum.getId()).jsonPath("$.frontMan.id").isEqualTo(savedPerson.getId());

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

        webTestClient.post().uri("/music-bands").contentType(MediaType.APPLICATION_JSON).bodyValue(request).exchange().expectStatus().isBadRequest().expectBody().jsonPath("$.status").isEqualTo(400).jsonPath("$.message").isEqualTo("Ошибка валидации данных").jsonPath("$.details[0].field").isEqualTo("name").jsonPath("$.details[0].message").isEqualTo("MusicBand.Name не может быть пустым").jsonPath("$.details[0].errorType").isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void shouldReturnBadRequestWhenCoordinatesIdIsNull() {
        MusicBandRequest request = createMusicBandRequest();
        request.setCoordinatesId(null);

        webTestClient.post().uri("/music-bands").contentType(MediaType.APPLICATION_JSON).bodyValue(request).exchange().expectStatus().isBadRequest().expectBody().jsonPath("$.status").isEqualTo(400).jsonPath("$.message").isEqualTo("Ошибка валидации данных").jsonPath("$.details[0].field").isEqualTo("coordinatesId").jsonPath("$.details[0].message").isEqualTo("MusicBand.CoordinatesId не может быть пустым").jsonPath("$.details[0].errorType").isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void shouldReturnNotFoundWhenCoordinatesIdDoesNotExist() {

        MusicBandRequest request = createMusicBandRequest();

        request.setCoordinatesId(999L);

        webTestClient.post().uri("/music-bands").contentType(MediaType.APPLICATION_JSON).bodyValue(request).exchange().expectStatus().isNotFound().expectBody().jsonPath("$.status").isEqualTo(404);
    }

    @Test
    void shouldGetAllMusicBands() {

        Coordinates c2 = coordinatesRepository.save(Coordinates.builder().x(3254).y(1.3f).build());

        Album a2 = albumRepository.save(Album.builder().name("Really Best Album").tracks(124L).sales(300).build());

        Location location2 = locationRepository.save(Location.builder().x(2).y(3L).z(3L).build());

        Person p2 = personRepository.save(Person.builder().name("Man Yesterday").eyeColor(BLACK).hairColor(BLUE).location(location2).weight(213f).nationality(FRANCE).build());

        MusicBand b1 = musicBandRepository.save(MusicBand.builder().name("Radiohead").coordinates(savedCoordinates).genre(ROCK).numberOfParticipants(5L).singlesCount(10L).description("Legendary rock band").bestAlbum(savedAlbum).albumsCount(9L).establishmentDate(new Date()).frontMan(savedPerson).creationDate(new Date()).build());

        MusicBand b2 = musicBandRepository.save(MusicBand.builder().name("Muse").coordinates(c2).genre(POST_ROCK).numberOfParticipants(3L).singlesCount(7L).description("British alt-rock band").bestAlbum(a2).albumsCount(5L).establishmentDate(new Date()).frontMan(p2).creationDate(new Date()).build());

        webTestClient.get().uri("/music-bands").exchange().expectStatus().isOk().expectBody().jsonPath("$.content.length()").isEqualTo(2).jsonPath("$.content[0].id").isEqualTo(b1.getId()).jsonPath("$.content[1].id").isEqualTo(b2.getId());
    }

    @Test
    void shouldGetMusicBandById() {
        MusicBand saved = musicBandRepository.save(MusicBand.builder().name("Test Band").coordinates(savedCoordinates).genre(ROCK).numberOfParticipants(5L).singlesCount(10L).description("Some description").bestAlbum(savedAlbum).albumsCount(3L).establishmentDate(new Date()).frontMan(savedPerson).creationDate(new Date()).build());

        webTestClient.get().uri("/music-bands/{id}", saved.getId()).exchange().expectStatus().isOk().expectBody().jsonPath("$.id").isEqualTo(saved.getId()).jsonPath("$.name").isEqualTo("Test Band").jsonPath("$.coordinates.id").isEqualTo(savedCoordinates.getId()).jsonPath("$.bestAlbum.id").isEqualTo(savedAlbum.getId()).jsonPath("$.frontMan.id").isEqualTo(savedPerson.getId());
    }

    @Test
    void shouldReturnNotFoundWhenGettingNonExistentBand() {
        webTestClient.get().uri("/music-bands/{id}", 9999).exchange().expectStatus().isNotFound().expectBody().jsonPath("$.status").isEqualTo(404).jsonPath("$.message").isEqualTo("Ошибка выполнения операции").jsonPath("$.details[0].field").isEqualTo("service").jsonPath("$.details[0].errorType").isEqualTo("SERVICE_ERROR");
    }

    @Test
    void shouldUpdateMusicBandSuccessfully() {
        MusicBand saved = musicBandRepository.save(MusicBand.builder().name("Old Band").coordinates(savedCoordinates).genre(ROCK).numberOfParticipants(5L).singlesCount(10L).description("Old description").bestAlbum(savedAlbum).albumsCount(9L).establishmentDate(new Date()).frontMan(savedPerson).creationDate(new Date()).build());

        Coordinates newCoordinates = coordinatesRepository.save(Coordinates.builder().x(100).y(50.5f).build());

        Album newAlbum = albumRepository.save(Album.builder().name("New Album").tracks(15L).sales(200).build());

        Location newLocation = locationRepository.save(Location.builder().x(10).y(20L).z(30L).build());

        Person newPerson = personRepository.save(Person.builder().name("New Frontman").eyeColor(GREEN).hairColor(BROWN).location(newLocation).weight(80f).nationality(UK).build());

        MusicBandRequest updateRequest = MusicBandRequest.builder().name("Updated Band").coordinatesId(newCoordinates.getId()).genre(POST_ROCK).numberOfParticipants(8L).singlesCount(15L).description("Updated description").bestAlbumId(newAlbum.getId()).albumsCount(12L).establishmentDate(new Date()).frontManId(newPerson.getId()).build();

        webTestClient.put().uri("/music-bands/{id}", saved.getId()).contentType(MediaType.APPLICATION_JSON).bodyValue(updateRequest).exchange().expectStatus().isOk().expectBody().jsonPath("$.name").isEqualTo("Updated Band").jsonPath("$.coordinates.id").isEqualTo(newCoordinates.getId()).jsonPath("$.bestAlbum.id").isEqualTo(newAlbum.getId()).jsonPath("$.frontMan.id").isEqualTo(newPerson.getId());

        MusicBand updated = musicBandRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Updated Band");
        assertThat(updated.getCoordinates().getId()).isEqualTo(newCoordinates.getId());
        assertThat(updated.getBestAlbum().getId()).isEqualTo(newAlbum.getId());
        assertThat(updated.getFrontMan().getId()).isEqualTo(newPerson.getId());
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentBand() {
        MusicBandRequest updateRequest = createMusicBandRequest();

        webTestClient.put().uri("/music-bands/{id}", 9999).contentType(MediaType.APPLICATION_JSON).bodyValue(updateRequest).exchange().expectStatus().isNotFound().expectBody().jsonPath("$.status").isEqualTo(404).jsonPath("$.message").isEqualTo("Ошибка выполнения операции").jsonPath("$.details[0].field").isEqualTo("service").jsonPath("$.details[0].errorType").isEqualTo("SERVICE_ERROR");
    }

    @Test
    void shouldDeleteMusicBandSuccessfully() {
        MusicBand saved = musicBandRepository.save(MusicBand.builder().name("ToDelete").coordinates(savedCoordinates).genre(ROCK).numberOfParticipants(4L).singlesCount(7L).description("temp").bestAlbum(savedAlbum).albumsCount(3L).establishmentDate(new Date()).frontMan(savedPerson).creationDate(new Date()).build());

        webTestClient.delete().uri("/music-bands/{id}", saved.getId()).exchange().expectStatus().isOk().expectBody().jsonPath("$.id").isEqualTo(saved.getId());

        assertThat(musicBandRepository.existsById(saved.getId())).isFalse();
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentBand() {
        webTestClient.delete().uri("/music-bands/{id}", 9999).exchange().expectStatus().isNotFound().expectBody().jsonPath("$.status").isEqualTo(404).jsonPath("$.message").isEqualTo("Ошибка выполнения операции").jsonPath("$.details[0].field").isEqualTo("service").jsonPath("$.details[0].errorType").isEqualTo("SERVICE_ERROR");
    }

    @Test
    void shouldGetBandsWithMaxCoordinates() {
        Coordinates c1 = coordinatesRepository.save(Coordinates.builder().x(100).y(50.5f).build());
        Coordinates c2 = coordinatesRepository.save(Coordinates.builder().x(50).y(25.5f).build());
        Coordinates c3 = coordinatesRepository.save(Coordinates.builder().x(100).y(30.5f).build());

        MusicBand b1 = createAndSaveBand("Band 1", c1);
        createAndSaveBand("Band 2", c2);
        createAndSaveBand("Band 3", c3);

        webTestClient.get().uri("/music-bands/max-coordinates").exchange().expectStatus().isOk().expectBody().jsonPath("$.id").isEqualTo(b1.getId()).jsonPath("$.name").isEqualTo("Band 1").jsonPath("$.coordinates.id").isEqualTo(c1.getId());
    }

    @Test
    void shouldGetBandsEstablishedBeforeDate() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date oldDate = sdf.parse("2010-01-01");
        Date newDate = sdf.parse("2020-01-01");

        MusicBand oldBand = createAndSaveBandWithDate("Old Band", oldDate);
        createAndSaveBandWithDate("New Band", newDate);

        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/music-bands/established-before").queryParam("date", "2015-01-01").build()).exchange().expectStatus().isOk().expectBody().jsonPath("$.length()").isEqualTo(1).jsonPath("$[0].id").isEqualTo(oldBand.getId()).jsonPath("$[0].name").isEqualTo("Old Band");
    }

    @Test
    void shouldReturnBadRequestWhenDateParameterMissing() {
        webTestClient.get().uri("/music-bands/established-before").exchange().expectStatus().isBadRequest().expectBody().jsonPath("$.status").isEqualTo(400).jsonPath("$.message").isEqualTo("Отсутствуют обязательные параметры").jsonPath("$.details[0].field").isEqualTo("date").jsonPath("$.details[0].errorType").isEqualTo("MISSING_PARAMETER");
    }

    @Test
    void shouldReturnBadRequestWhenDateInvalidFormat() {
        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/music-bands/established-before").queryParam("date", "invalid-date").build()).exchange().expectStatus().isBadRequest().expectBody().jsonPath("$.status").isEqualTo(400).jsonPath("$.message").isEqualTo("Ошибка в параметрах запроса").jsonPath("$.details[0].field").isEqualTo("date").jsonPath("$.details[0].errorType").isEqualTo("TYPE_MISMATCH");
    }

    @Test
    void shouldReturnEmptyArrayWhenNoBandsFound() {
        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/music-bands/established-before").queryParam("date", "1900-01-01").build()).exchange().expectStatus().isOk().expectBody().jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void shouldReturnBadRequestWhenCreatingWithMissingRequestBody() {
        webTestClient.post().uri("/music-bands").contentType(MediaType.APPLICATION_JSON).exchange().expectStatus().isBadRequest().expectBody().jsonPath("$.status").isEqualTo(400).jsonPath("$.message").isEqualTo("Отсутствует тело запроса").jsonPath("$.details[0].field").isEqualTo("requestBody").jsonPath("$.details[0].errorType").isEqualTo("INVALID_JSON");
    }

    @Test
    void shouldGetUniqueAlbumsCount() {
        createAndSaveBand("Band 1", 5L);
        createAndSaveBand("Band 2", 3L);
        createAndSaveBand("Band 3", 5L);
        createAndSaveBand("Band 4", 7L);

        webTestClient.get().uri("/music-bands/unique-albums-count").exchange().expectStatus().isOk().expectBody().jsonPath("$.length()").isEqualTo(3);
    }

    @Test
    void shouldRemoveParticipantFromBand() {
        MusicBand band = createAndSaveBandWithParticipants("Test Band", 5L);

        webTestClient.put().uri("/music-bands/{id}/remove-participant", band.getId()).exchange().expectStatus().isOk().expectBody().jsonPath("$.id").isEqualTo(band.getId()).jsonPath("$.name").isEqualTo("Test Band").jsonPath("$.numberOfParticipants").isEqualTo(4);

        MusicBand updated = musicBandRepository.findById(band.getId()).orElseThrow();
        assertThat(updated.getNumberOfParticipants()).isEqualTo(4L);
    }

    @Test
    void shouldReturnBadRequestWhenRemovingLastParticipant() {
        MusicBand band = createAndSaveBandWithParticipants("Solo Band", 1L);

        webTestClient.put().uri("/music-bands/{id}/remove-participant", band.getId()).exchange().expectStatus().isBadRequest().expectBody().jsonPath("$.status").isEqualTo(400).jsonPath("$.message").isEqualTo("Ошибка выполнения операции").jsonPath("$.details[0].field").isEqualTo("service").jsonPath("$.details[0].message").isEqualTo("Невозможно удалить участника - в группе должен остаться хотя бы 1 участник").jsonPath("$.details[0].errorType").isEqualTo("SERVICE_ERROR");

        MusicBand unchanged = musicBandRepository.findById(band.getId()).orElseThrow();
        assertThat(unchanged.getNumberOfParticipants()).isEqualTo(1L);
    }

    @Test
    void shouldReturnNotFoundWhenRemovingFromNonExistentBand() {
        webTestClient.put().uri("/music-bands/{id}/remove-participant", 9999).exchange().expectStatus().isNotFound().expectBody().jsonPath("$.status").isEqualTo(404).jsonPath("$.message").isEqualTo("Ошибка выполнения операции").jsonPath("$.details[0].field").isEqualTo("service").jsonPath("$.details[0].errorType").isEqualTo("SERVICE_ERROR");
    }

    // Вспомогательные методы
    private MusicBand createAndSaveBand(String name, Coordinates coordinates) {
        return musicBandRepository.save(MusicBand.builder().name(name).coordinates(coordinates).genre(ROCK).numberOfParticipants(5L).singlesCount(10L).description("Description for " + name).bestAlbum(savedAlbum).albumsCount(5L).establishmentDate(new Date()).frontMan(savedPerson).creationDate(new Date()).build());
    }

    private MusicBand createAndSaveBandWithParticipants(String name, Long numberOfParticipants) {
        return musicBandRepository.save(MusicBand.builder().name(name).coordinates(savedCoordinates).genre(ROCK).numberOfParticipants(numberOfParticipants).singlesCount(10L).description("Description for " + name).bestAlbum(savedAlbum).albumsCount(5L).establishmentDate(new Date()).frontMan(savedPerson).creationDate(new Date()).build());
    }

    private MusicBand createAndSaveBandWithDate(String name, Date establishmentDate) {
        return musicBandRepository.save(MusicBand.builder().name(name).coordinates(savedCoordinates).genre(ROCK).numberOfParticipants(5L).singlesCount(10L).description("Description for " + name).bestAlbum(savedAlbum).albumsCount(5L).establishmentDate(establishmentDate).frontMan(savedPerson).creationDate(new Date()).build());
    }

    private void createAndSaveBand(String name, Long albumsCount) {
        musicBandRepository.save(MusicBand.builder().name(name).coordinates(savedCoordinates).genre(ROCK).numberOfParticipants(5L).singlesCount(10L).description("Description for " + name).bestAlbum(savedAlbum).albumsCount(albumsCount).establishmentDate(new Date()).frontMan(savedPerson).creationDate(new Date()).build());
    }

    @Test
    void shouldGetMusicBandsWithPaginationAndSorting() {
        // Создаем тестовые данные
        createAndSaveBand("Band A", 5L, 10L, 3L);
        createAndSaveBand("Band C", 3L, 15L, 5L);
        createAndSaveBand("Band B", 7L, 5L, 2L);

        // Тест пагинации с сортировкой по имени (asc)
        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/music-bands").queryParam("page", "0").queryParam("size", "2").queryParam("sort", "name").queryParam("direction", "asc").build()).exchange().expectStatus().isOk().expectBody().jsonPath("$.content.length()").isEqualTo(2).jsonPath("$.content[0].name").isEqualTo("Band A").jsonPath("$.content[1].name").isEqualTo("Band B").jsonPath("$.page.totalPages").isEqualTo(2).jsonPath("$.page.totalElements").isEqualTo(3).jsonPath("$.page.size").isEqualTo(2).jsonPath("$.page.number").isEqualTo(0);
    }

    @Test
    void shouldGetMusicBandsSortedByNameDesc() {
        // Создаем тестовые данные
        createAndSaveBand("Alpha Band", 5L, 10L, 3L);
        createAndSaveBand("Beta Band", 3L, 15L, 5L);
        createAndSaveBand("Gamma Band", 7L, 5L, 2L);

        // Тест сортировки по имени (desc)
        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/music-bands").queryParam("sort", "name").queryParam("direction", "desc").build()).exchange().expectStatus().isOk().expectBody().jsonPath("$.content.length()").isEqualTo(3).jsonPath("$.content[0].name").isEqualTo("Gamma Band").jsonPath("$.content[1].name").isEqualTo("Beta Band").jsonPath("$.content[2].name").isEqualTo("Alpha Band");
    }

    @Test
    void shouldGetMusicBandsSortedByNumberOfParticipantsDesc() {
        // Создаем тестовые данные с разным количеством участников
        createAndSaveBand("Small Band", 2L, 10L, 3L);
        createAndSaveBand("Medium Band", 5L, 15L, 5L);
        createAndSaveBand("Large Band", 8L, 5L, 2L);

        // Тест сортировки по количеству участников (desc)
        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/music-bands").queryParam("sort", "numberOfParticipants").queryParam("direction", "desc").build()).exchange().expectStatus().isOk().expectBody().jsonPath("$.content.length()").isEqualTo(3).jsonPath("$.content[0].numberOfParticipants").isEqualTo(8).jsonPath("$.content[1].numberOfParticipants").isEqualTo(5).jsonPath("$.content[2].numberOfParticipants").isEqualTo(2);
    }

    @Test
    void shouldGetMusicBandsSortedBySinglesCountAsc() {
        // Создаем тестовые данные с разным количеством синглов
        createAndSaveBand("Band One", 5L, 5L, 3L);
        createAndSaveBand("Band Two", 5L, 15L, 5L);
        createAndSaveBand("Band Three", 5L, 10L, 2L);

        // Тест сортировки по количеству синглов (asc)
        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/music-bands").queryParam("sort", "singlesCount").queryParam("direction", "asc").build()).exchange().expectStatus().isOk().expectBody().jsonPath("$.content.length()").isEqualTo(3).jsonPath("$.content[0].singlesCount").isEqualTo(5).jsonPath("$.content[1].singlesCount").isEqualTo(10).jsonPath("$.content[2].singlesCount").isEqualTo(15);
    }

    @Test
    void shouldFilterMusicBandsByName() {
        // Создаем тестовые данные
        createAndSaveBand("Rock Band", 5L, 10L, 3L);
        createAndSaveBand("Jazz Band", 3L, 15L, 5L);
        createAndSaveBand("Rock Group", 7L, 5L, 2L);

        // Тест фильтрации по точному имени
        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/music-bands").queryParam("name", "Rock Band").build()).exchange().expectStatus().isOk().expectBody().jsonPath("$.content.length()").isEqualTo(1).jsonPath("$.content[0].name").isEqualTo("Rock Band").jsonPath("$.content[0].numberOfParticipants").isEqualTo(5);
    }

    @Test
    void shouldFilterMusicBandsByGenre() {
        // Создаем тестовые данные с разными жанрами
        createAndSaveBandWithGenre("Rockers", ROCK);
        createAndSaveBandWithGenre("Soul Singers", MusicGenre.SOUL);
        createAndSaveBandWithGenre("Punk Band", MusicGenre.PUNK_ROCK);

        // Тест фильтрации по жанру
        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/music-bands").queryParam("genre", "ROCK").build()).exchange().expectStatus().isOk().expectBody().jsonPath("$.content.length()").isEqualTo(1).jsonPath("$.content[0].name").isEqualTo("Rockers").jsonPath("$.content[0].genre").isEqualTo("ROCK");
    }

    @Test
    void shouldFilterMusicBandsByParticipantsRange() {
        // Создаем тестовые данные с разным количеством участников
        createAndSaveBand("Small", 2L, 10L, 3L);
        createAndSaveBand("Medium", 5L, 15L, 5L);
        createAndSaveBand("Large", 8L, 5L, 2L);

        // Тест фильтрации по диапазону участников
        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/music-bands").queryParam("minParticipants", "3").queryParam("maxParticipants", "6").build()).exchange().expectStatus().isOk().expectBody().jsonPath("$.content.length()").isEqualTo(1).jsonPath("$.content[0].name").isEqualTo("Medium").jsonPath("$.content[0].numberOfParticipants").isEqualTo(5);
    }

    @Test
    void shouldFilterMusicBandsBySinglesCountRange() {
        // Создаем тестовые данные с разным количеством синглов
        createAndSaveBand("Few Singles", 5L, 3L, 3L);
        createAndSaveBand("Some Singles", 5L, 8L, 5L);
        createAndSaveBand("Many Singles", 5L, 12L, 2L);

        // Тест фильтрации по диапазону синглов
        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/music-bands").queryParam("minSingles", "5").queryParam("maxSingles", "10").build()).exchange().expectStatus().isOk().expectBody().jsonPath("$.content.length()").isEqualTo(1).jsonPath("$.content[0].name").isEqualTo("Some Singles").jsonPath("$.content[0].singlesCount").isEqualTo(8);
    }

    @Test
    void shouldFilterMusicBandsByAlbumsCountRange() {
        // Создаем тестовые данные с разным количеством альбомов
        createAndSaveBand("New Band", 5L, 10L, 1L);
        createAndSaveBand("Established", 5L, 15L, 5L);
        createAndSaveBand("Veteran", 5L, 5L, 10L);

        // Тест фильтрации по диапазону альбомов
        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/music-bands").queryParam("minAlbumsCount", "3").queryParam("maxAlbumsCount", "7").build()).exchange().expectStatus().isOk().expectBody().jsonPath("$.content.length()").isEqualTo(1).jsonPath("$.content[0].name").isEqualTo("Established").jsonPath("$.content[0].albumsCount").isEqualTo(5);
    }

    @Test
    void shouldFilterMusicBandsByFrontManName() {
        // Создаем разных Бронтманов
        Person john = createPerson("John Doe");
        Person mike = createPerson("Mike Smith");
        Person anna = createPerson("Anna Johnson");

        createAndSaveBandWithFrontMan("Band One", john);
        createAndSaveBandWithFrontMan("Band Two", mike);
        createAndSaveBandWithFrontMan("Band Three", anna);

        // Тест фильтрации по имени фронтмена
        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/music-bands").queryParam("frontManName", "John Doe").build()).exchange().expectStatus().isOk().expectBody().jsonPath("$.content.length()").isEqualTo(1).jsonPath("$.content[0].name").isEqualTo("Band One").jsonPath("$.content[0].frontMan.id").isEqualTo(john.getId());
    }

    @Test
    void shouldFilterMusicBandsByBestAlbumName() {
        // Создаем разные альбомы
        Album album1 = createAlbum("Dark Side of the Moon");
        Album album2 = createAlbum("Thriller");
        Album album3 = createAlbum("Back in Black");

        createAndSaveBandWithAlbum("Pink Floyd", album1);
        createAndSaveBandWithAlbum("Michael Jackson", album2);
        createAndSaveBandWithAlbum("AC/DC", album3);

        // Тест фильтрации по названию альбома
        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/music-bands").queryParam("bestAlbumName", "Thriller").build()).exchange().expectStatus().isOk().expectBody().jsonPath("$.content.length()").isEqualTo(1).jsonPath("$.content[0].name").isEqualTo("Michael Jackson").jsonPath("$.content[0].bestAlbum.id").isEqualTo(album2.getId());
    }

    @Test
    void shouldFilterMusicBandsByMultipleCriteria() {
        // Создаем тестовые данные для комплексного фильтра
        createAndSaveBand("Target Band", 5L, 10L, 5L);
        createAndSaveBand("Other Band 1", 3L, 15L, 3L);
        createAndSaveBand("Other Band 2", 7L, 5L, 7L);

        // Комплексный фильтр
        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/music-bands").queryParam("name", "Target Band").queryParam("minParticipants", "4").queryParam("maxParticipants", "6").queryParam("minAlbumsCount", "4").queryParam("maxAlbumsCount", "6").queryParam("sort", "singlesCount").queryParam("direction", "desc").build()).exchange().expectStatus().isOk().expectBody().jsonPath("$.content.length()").isEqualTo(1).jsonPath("$.content[0].name").isEqualTo("Target Band").jsonPath("$.content[0].numberOfParticipants").isEqualTo(5).jsonPath("$.content[0].albumsCount").isEqualTo(5);
    }

    @Test
    void shouldReturnEmptyPageWhenNoBandsMatchFilters() {
        // Создаем тестовые данные
        createAndSaveBand("Rock Band", 5L, 10L, 3L);
        createAndSaveBand("Jazz Band", 3L, 15L, 5L);

        // Фильтр, который не соответствует ни одной группе
        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/music-bands").queryParam("name", "Non Existent Band").build()).exchange().expectStatus().isOk().expectBody().jsonPath("$.content.length()").isEqualTo(0).jsonPath("$.page.totalElements").isEqualTo(0);
    }

    @Test
    void shouldUseDefaultPaginationWhenNoParametersProvided() {
        // Создаем больше 20 групп для проверки дефолтной пагинации
        for (int i = 1; i <= 25; i++) {
            createAndSaveBand("Band " + i, 5L, 10L, 3L);
        }

        webTestClient.get().uri("/music-bands").exchange().expectStatus().isOk().expectBody().jsonPath("$.content.length()").isEqualTo(10) // дефолтный size
                .jsonPath("$.page.totalPages").isEqualTo(3).jsonPath("$.page.totalElements").isEqualTo(25).jsonPath("$.page.size").isEqualTo(10).jsonPath("$.page.number").isEqualTo(0);
    }

    // Вспомогательные методы для тестов

    private void createAndSaveBand(String name, Long numberOfParticipants, Long singlesCount, Long albumsCount) {
        musicBandRepository.save(MusicBand.builder().name(name).coordinates(savedCoordinates).genre(ROCK).numberOfParticipants(numberOfParticipants).singlesCount(singlesCount).description("Description for " + name).bestAlbum(savedAlbum).albumsCount(albumsCount).establishmentDate(new Date()).frontMan(savedPerson).creationDate(new Date()).build());
    }

    private void createAndSaveBandWithGenre(String name, MusicGenre genre) {
        musicBandRepository.save(MusicBand.builder().name(name).coordinates(savedCoordinates).genre(genre).numberOfParticipants(5L).singlesCount(10L).description("Description for " + name).bestAlbum(savedAlbum).albumsCount(5L).establishmentDate(new Date()).frontMan(savedPerson).creationDate(new Date()).build());
    }

    private void createAndSaveBandWithFrontMan(String name, Person frontMan) {
        musicBandRepository.save(MusicBand.builder().name(name).coordinates(savedCoordinates).genre(ROCK).numberOfParticipants(5L).singlesCount(10L).description("Description for " + name).bestAlbum(savedAlbum).albumsCount(5L).establishmentDate(new Date()).frontMan(frontMan).creationDate(new Date()).build());
    }

    private void createAndSaveBandWithAlbum(String name, Album album) {
        musicBandRepository.save(MusicBand.builder().name(name).coordinates(savedCoordinates).genre(ROCK).numberOfParticipants(5L).singlesCount(10L).description("Description for " + name).bestAlbum(album).albumsCount(5L).establishmentDate(new Date()).frontMan(savedPerson).creationDate(new Date()).build());
    }

    private Person createPerson(String name) {
        Location location = locationRepository.save(Location.builder().x(1).y(2L).z(3L).build());

        return personRepository.save(Person.builder().name(name).eyeColor(BLUE).hairColor(BLACK).location(location).weight(70f).nationality(USA).build());
    }

    private Album createAlbum(String name) {
        return albumRepository.save(Album.builder().name(name).tracks(10L).sales(100).build());
    }

}