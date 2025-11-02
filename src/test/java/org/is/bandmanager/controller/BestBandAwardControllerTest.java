package org.is.bandmanager.controller;

import org.is.bandmanager.config.IntegrationTest;
import org.is.bandmanager.dto.request.BestBandAwardRequest;
import org.is.bandmanager.model.Album;
import org.is.bandmanager.model.BestBandAward;
import org.is.bandmanager.model.Color;
import org.is.bandmanager.model.Coordinates;
import org.is.bandmanager.model.Country;
import org.is.bandmanager.model.Location;
import org.is.bandmanager.model.MusicBand;
import org.is.bandmanager.model.MusicGenre;
import org.is.bandmanager.model.Person;
import org.is.bandmanager.repository.AlbumRepository;
import org.is.bandmanager.repository.BestBandAwardRepository;
import org.is.bandmanager.repository.CoordinatesRepository;
import org.is.bandmanager.repository.LocationRepository;
import org.is.bandmanager.repository.MusicBandRepository;
import org.is.bandmanager.repository.PersonRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class BestBandAwardControllerTest extends AbstractIntegrationTest {

    @Autowired
    private BestBandAwardRepository bestBandAwardRepository;

    @Autowired
    private MusicBandRepository musicBandRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private CoordinatesRepository coordinatesRepository;

    private MusicBand testBand;

    private static Stream<Arguments> provideInvalidBestBandAwardRequests() {
        return Stream.of(
                Arguments.of("Null music band id",
                        createBestBandAwardRequest(null, MusicGenre.ROCK), "musicBandId"),
                Arguments.of("Null genre",
                        createBestBandAwardRequest(1, null), "genre")
        );
    }

    private static BestBandAwardRequest createBestBandAwardRequest(Integer musicBandId, MusicGenre genre) {
        return BestBandAwardRequest.builder()
                .musicBandId(musicBandId)
                .genre(genre)
                .build();
    }

    @BeforeEach
    void setUp() {
        getClient();
        // Сначала сохраняем все зависимости
        Album savedAlbum = albumRepository.save(
                Album.builder()
                        .name("Test Album")
                        .tracks(10L)
                        .sales(100)
                        .build()
        );

        Location savedLocation = locationRepository.save(
                Location.builder()
                        .x(1)
                        .y(2L)
                        .z(3L)
                        .build()
        );

        Person savedFrontMan = personRepository.save(
                Person.builder()
                        .name("Front Man")
                        .eyeColor(Color.BLACK)
                        .hairColor(Color.BLUE)
                        .location(savedLocation)
                        .weight(75.5F)
                        .nationality(Country.USA)
                        .build()
        );

        Coordinates savedCoordinates = coordinatesRepository.save(
                Coordinates.builder()
                        .x(1)
                        .y(2F)
                        .build()
        );

        // Теперь сохраняем MusicBand с сохраненными зависимостями
        testBand = musicBandRepository.save(
                MusicBand.builder()
                        .name("Test Band")
                        .description("Test Description")
                        .genre(MusicGenre.ROCK)
                        .numberOfParticipants(5L)
                        .bestAlbum(savedAlbum)
                        .singlesCount(10L)
                        .albumsCount(3L)
                        .frontMan(savedFrontMan)
                        .coordinates(savedCoordinates)
                        .establishmentDate(new Date())
                        .build()
        );
    }

    @AfterEach
    void cleanDatabase() {
        bestBandAwardRepository.deleteAll();
        musicBandRepository.deleteAll();
        personRepository.deleteAll();
        locationRepository.deleteAll();
        coordinatesRepository.deleteAll();
        albumRepository.deleteAll();
    }

    @Test
    void shouldCreateBestBandAwardSuccessfully() {
        // Given
        BestBandAwardRequest request = createValidBestBandAwardRequest();

        // When & Then
        getClient().post("/best-band-awards", request)
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.bandId").isEqualTo(testBand.getId())
                .jsonPath("$.bandName").isEqualTo("Test Band")
                .jsonPath("$.genre").isEqualTo("ROCK");

        // Verify database
        assertThat(bestBandAwardRepository.findAll()).hasSize(1);
    }

    @Test
    void shouldGetAllBestBandAwardsWithPagination() {
        // Given
        BestBandAward award1 = createBestBandAward(testBand, MusicGenre.ROCK, LocalDateTime.now().minusDays(2));
        BestBandAward award2 = createBestBandAward(testBand, MusicGenre.POST_ROCK, LocalDateTime.now().minusDays(1));
        bestBandAwardRepository.saveAll(List.of(award1, award2));

        // When & Then - без фильтра, с пагинацией
        getClient().get("/best-band-awards?page=0&size=1&sort=createdDate,DESC")
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(1)
                .jsonPath("$.page.totalElements").isEqualTo(2)
                .jsonPath("$.page.totalPages").isEqualTo(2);
    }

    @Test
    void shouldGetAllBestBandAwardsWithGenreFilter() {
        // Given
        BestBandAward award1 = createBestBandAward(testBand, MusicGenre.ROCK, LocalDateTime.now());
        BestBandAward award2 = createBestBandAward(testBand, MusicGenre.POST_ROCK, LocalDateTime.now());
        bestBandAwardRepository.saveAll(List.of(award1, award2));

        // When & Then - фильтр по жанру
        getClient().get("/best-band-awards?genre=ROCK")
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(1)
                .jsonPath("$.content[0].genre").isEqualTo("ROCK");
    }

    @Test
    void shouldGetAllBestBandAwardsWithBandNameFilter() {
        // Given
        BestBandAward award = createBestBandAward(testBand, MusicGenre.ROCK, LocalDateTime.now());
        bestBandAwardRepository.save(award);

        // When & Then - фильтр по имени группы
        getClient().get("/best-band-awards?bandName=Test Band")
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(1)
                .jsonPath("$.content[0].bandName").isEqualTo("Test Band");
    }

    @Test
    void shouldGetBestBandAwardById() {
        // Given
        BestBandAward award = bestBandAwardRepository.save(createBestBandAward(testBand, MusicGenre.ROCK, LocalDateTime.now()));

        // When & Then
        getClient().get("/best-band-awards/{id}", award.getId())
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(award.getId())
                .jsonPath("$.bandId").isEqualTo(testBand.getId())
                .jsonPath("$.bandName").isEqualTo("Test Band")
                .jsonPath("$.genre").isEqualTo("ROCK");
    }

    @Test
    void shouldUpdateBestBandAwardSuccessfully() {
        // Given
        BestBandAward award = bestBandAwardRepository.save(createBestBandAward(testBand, MusicGenre.ROCK, LocalDateTime.now()));
        BestBandAwardRequest updateRequest = createBestBandAwardRequest(testBand.getId(), MusicGenre.POST_ROCK);

        // When & Then
        getClient().putWithBody("/best-band-awards/{id}", updateRequest, award.getId())
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(award.getId())
                .jsonPath("$.genre").isEqualTo("POST_ROCK");

        // Verify in DB
        BestBandAward updated = bestBandAwardRepository.findById(award.getId()).orElseThrow();
        assertThat(updated.getGenre()).isEqualTo(MusicGenre.POST_ROCK);
    }

    @Test
    void shouldDeleteBestBandAwardSuccessfully() {
        // Given
        BestBandAward award = bestBandAwardRepository.save(createBestBandAward(testBand, MusicGenre.ROCK, LocalDateTime.now()));

        // When & Then
        getClient().delete("/best-band-awards/{id}", award.getId())
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(award.getId())
                .jsonPath("$.genre").isEqualTo("ROCK");

        // Verify DB deletion
        assertThat(bestBandAwardRepository.existsById(award.getId())).isFalse();
    }

    @Test
    void shouldReturnNotFoundWhenGettingNonExistentBestBandAward() {
        getClient().get("/best-band-awards/{id}", 999L)
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").isEqualTo("Ошибка выполнения операции")
                .jsonPath("$.details[0].errorType").isEqualTo("SERVICE_ERROR");
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentBestBandAward() {
        BestBandAwardRequest updateRequest = createValidBestBandAwardRequest();

        getClient().putWithBody("/best-band-awards/{id}", updateRequest, 999L)
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentBestBandAward() {
        getClient().delete("/best-band-awards/{id}", 999L)
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404);
    }

    @Test
    void shouldReturnBadRequestWhenMusicBandNotFound() {
        BestBandAwardRequest request = createBestBandAwardRequest(999, MusicGenre.ROCK);

        getClient().post("/best-band-awards", request)
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidBestBandAwardRequests")
    void shouldReturnBadRequestWhenCreatingBestBandAwardWithInvalidData(
            String ignored, BestBandAwardRequest invalidRequest, String expectedErrorField) {

        getClient().post("/best-band-awards", invalidRequest)
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message").isEqualTo("Ошибка валидации данных")
                .jsonPath("$.details[0].field").isEqualTo(expectedErrorField);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidBestBandAwardRequests")
    void shouldReturnBadRequestWhenUpdatingBestBandAwardWithInvalidData(
            String ignored, BestBandAwardRequest invalidRequest, String expectedErrorField) {

        BestBandAward award = bestBandAwardRepository.save(createBestBandAward(testBand, MusicGenre.ROCK, LocalDateTime.now()));

        getClient().putWithBody("/best-band-awards/{id}", invalidRequest, award.getId())
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.details[0].field").isEqualTo(expectedErrorField);
    }

    @Test
    void shouldReturnEmptyPageWhenNoBestBandAwards() {
        getClient().get("/best-band-awards")
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(0)
                .jsonPath("$.page.totalElements").isEqualTo(0)
                .jsonPath("$.page.totalPages").isEqualTo(0);
    }

    private BestBandAwardRequest createValidBestBandAwardRequest() {
        return createBestBandAwardRequest(testBand.getId(), MusicGenre.ROCK);
    }

    private BestBandAward createBestBandAward(MusicBand band, MusicGenre genre, LocalDateTime ignored) {
        return BestBandAward.builder()
                .band(band)
                .genre(genre)
                .build();
    }

}