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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.is.bandmanager.model.MusicGenre.POST_PUNK;
import static org.is.bandmanager.model.MusicGenre.POST_ROCK;
import static org.is.bandmanager.model.MusicGenre.PROGRESSIVE_ROCK;
import static org.is.bandmanager.model.MusicGenre.PUNK_ROCK;
import static org.is.bandmanager.model.MusicGenre.ROCK;
import static org.is.bandmanager.model.MusicGenre.SOUL;

@IntegrationTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BestBandAwardControllerTest extends AbstractIntegrationTest {

	@Autowired
	private BestBandAwardRepository bestBandAwardRepository;

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

	private MusicBand savedMusicBand;

	@BeforeAll
	void setClient() {
		this.webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
	}

	@BeforeEach
	void setUp() {
		bestBandAwardRepository.deleteAll();
		musicBandRepository.deleteAll();
		personRepository.deleteAll();
		albumRepository.deleteAll();
		coordinatesRepository.deleteAll();
		locationRepository.deleteAll();

		// Создаем все зависимости для MusicBand
		Location location = locationRepository.save(Location.builder().x(10).y(100L).z(1000L).build());

		Coordinates coordinates = coordinatesRepository.save(Coordinates.builder().x(100).y(50.5f).build());

		Album album = albumRepository.save(Album.builder().name("Test Album").tracks(10L).sales(1000).build());

		Person frontMan = personRepository.save(Person.builder().name("Test Frontman").eyeColor(Color.BLUE).hairColor(Color.BLACK).location(location).weight(70f).nationality(Country.USA).build());

		// Создаем тестовую группу для наград
		savedMusicBand = musicBandRepository.save(MusicBand.builder().name("Test Band").coordinates(coordinates).genre(ROCK).numberOfParticipants(5L).singlesCount(10L).description("Test description").bestAlbum(album).albumsCount(5L).establishmentDate(new java.util.Date()).frontMan(frontMan).creationDate(new java.util.Date()).build());
	}

	@AfterAll
	void tearDown() {
		bestBandAwardRepository.deleteAll();
		musicBandRepository.deleteAll();
		personRepository.deleteAll();
		albumRepository.deleteAll();
		coordinatesRepository.deleteAll();
		locationRepository.deleteAll();
	}

	@Test
	void shouldCreateBestBandAwardSuccessfully() {
		// Given
		BestBandAwardRequest request = BestBandAwardRequest.builder().musicBandId(savedMusicBand.getId()).genre(PROGRESSIVE_ROCK).build();

		// When & Then
		webTestClient.post().uri("/best-band-awards").contentType(MediaType.APPLICATION_JSON).bodyValue(request).exchange().expectStatus().isCreated();

		// Verify database
		List<BestBandAward> awards = bestBandAwardRepository.findAll();
		assertThat(awards).hasSize(1);
		assertThat(awards.get(0).getBand().getId()).isEqualTo(savedMusicBand.getId());
		assertThat(awards.get(0).getGenre()).isEqualTo(PROGRESSIVE_ROCK);
	}

	@Test
	void shouldReturnBadRequestWhenCreatingAwardWithNullMusicBandId() {
		// Given
		BestBandAwardRequest request = BestBandAwardRequest.builder().musicBandId(null) // Invalid
				.genre(ROCK).build();

		// When & Then
		webTestClient.post().uri("/best-band-awards").contentType(MediaType.APPLICATION_JSON).bodyValue(request).exchange().expectStatus().isBadRequest().expectBody().jsonPath("$.status").isEqualTo(400).jsonPath("$.message").isEqualTo("Ошибка валидации данных").jsonPath("$.details[0].field").isEqualTo("musicBandId").jsonPath("$.details[0].message").isEqualTo("BestBandAward.MusicBandId не может быть пустым").jsonPath("$.details[0].errorType").isEqualTo("VALIDATION_ERROR");
	}

	@Test
	void shouldReturnBadRequestWhenCreatingAwardWithNullGenre() {
		// Given
		BestBandAwardRequest request = BestBandAwardRequest.builder().musicBandId(savedMusicBand.getId()).genre(null) // Invalid
				.build();

		// When & Then
		webTestClient.post().uri("/best-band-awards").contentType(MediaType.APPLICATION_JSON).bodyValue(request).exchange().expectStatus().isBadRequest().expectBody().jsonPath("$.status").isEqualTo(400).jsonPath("$.message").isEqualTo("Ошибка валидации данных").jsonPath("$.details[0].field").isEqualTo("genre").jsonPath("$.details[0].message").isEqualTo("BestBandAward.MusicGenre не может быть пустым").jsonPath("$.details[0].errorType").isEqualTo("VALIDATION_ERROR");
	}

	@Test
	void shouldReturnBadRequestWhenCreatingAwardWithMultipleErrors() {
		// Given
		BestBandAwardRequest request = BestBandAwardRequest.builder().musicBandId(null) // Invalid
				.genre(null) // Invalid
				.build();

		// When & Then
		webTestClient.post().uri("/best-band-awards").contentType(MediaType.APPLICATION_JSON).bodyValue(request).exchange().expectStatus().isBadRequest().expectBody().jsonPath("$.status").isEqualTo(400).jsonPath("$.message").isEqualTo("Ошибка валидации данных").jsonPath("$.details.length()").isEqualTo(2);
	}

	@Test
	void shouldGetAllBestBandAwards() {
		// Given
		BestBandAward award1 = BestBandAward.builder().band(savedMusicBand).genre(ROCK).build();
		BestBandAward award2 = BestBandAward.builder().band(savedMusicBand).genre(PROGRESSIVE_ROCK).build();
		bestBandAwardRepository.saveAll(List.of(award1, award2));

		// When & Then
		webTestClient.get().uri("/best-band-awards").exchange().expectStatus().isOk().expectBody().jsonPath("$.content.length()").isEqualTo(2);
	}

	@Test
	void shouldGetBestBandAwardById() {
		// Given
		BestBandAward award = bestBandAwardRepository.save(BestBandAward.builder().band(savedMusicBand).genre(PUNK_ROCK).build());

		// When & Then
		webTestClient.get().uri("/best-band-awards/{id}", award.getId()).exchange().expectStatus().isOk();
	}

	@Test
	void shouldReturnNotFoundWhenGettingNonExistentAward() {
		webTestClient.get().uri("/best-band-awards/{id}", 999L).exchange().expectStatus().isNotFound().expectBody().jsonPath("$.status").isEqualTo(404).jsonPath("$.message").isEqualTo("Ошибка выполнения операции").jsonPath("$.details[0].field").isEqualTo("service").jsonPath("$.details[0].errorType").isEqualTo("SERVICE_ERROR");
	}

	@Test
	void shouldUpdateBestBandAwardSuccessfully() {
		// Given
		BestBandAward award = bestBandAwardRepository.save(BestBandAward.builder().band(savedMusicBand).genre(ROCK).build());

		// Создаем все зависимости для новой группы
		Location newLocation = locationRepository.save(Location.builder().x(20).y(200L).z(2000L).build());

		Coordinates newCoordinates = coordinatesRepository.save(Coordinates.builder().x(200).y(60.5f).build());

		Album newAlbum = albumRepository.save(Album.builder().name("New Album").tracks(12L).sales(2000).build());

		Person newFrontMan = personRepository.save(Person.builder().name("New Frontman").eyeColor(Color.GREEN).hairColor(Color.BROWN).location(newLocation).weight(75f).nationality(Country.UK).build());

		// Создаем другую группу для обновления
		MusicBand newMusicBand = musicBandRepository.save(MusicBand.builder().name("New Band").coordinates(newCoordinates).genre(PROGRESSIVE_ROCK).numberOfParticipants(3L).singlesCount(5L).description("New description").bestAlbum(newAlbum).albumsCount(3L).establishmentDate(new java.util.Date()).frontMan(newFrontMan).creationDate(new java.util.Date()).build());

		BestBandAwardRequest update = BestBandAwardRequest.builder().musicBandId(newMusicBand.getId()).genre(SOUL).build();

		// When & Then
		webTestClient.put().uri("/best-band-awards/{id}", award.getId()).contentType(MediaType.APPLICATION_JSON).bodyValue(update).exchange().expectStatus().isOk();

		// Verify in DB
		BestBandAward updated = bestBandAwardRepository.findById(award.getId()).orElseThrow();
		assertThat(updated.getBand().getId()).isEqualTo(newMusicBand.getId());
		assertThat(updated.getGenre()).isEqualTo(SOUL);
	}

	@Test
	void shouldReturnNotFoundWhenUpdatingNonExistentAward() {
		// Given
		BestBandAwardRequest update = BestBandAwardRequest.builder().musicBandId(savedMusicBand.getId()).genre(POST_ROCK).build();

		// When & Then
		webTestClient.put().uri("/best-band-awards/{id}", 999L).contentType(MediaType.APPLICATION_JSON).bodyValue(update).exchange().expectStatus().isNotFound().expectBody().jsonPath("$.status").isEqualTo(404).jsonPath("$.message").isEqualTo("Ошибка выполнения операции").jsonPath("$.details[0].field").isEqualTo("service").jsonPath("$.details[0].errorType").isEqualTo("SERVICE_ERROR");
	}

	@Test
	void shouldDeleteBestBandAwardSuccessfully() {
		// Given
		BestBandAward award = bestBandAwardRepository.save(BestBandAward.builder().band(savedMusicBand).genre(POST_PUNK).build());

		// When & Then
		webTestClient.delete().uri("/best-band-awards/{id}", award.getId()).exchange().expectStatus().isOk();

		// Verify DB deletion
		assertThat(bestBandAwardRepository.existsById(award.getId())).isFalse();
	}

	@Test
	void shouldReturnNotFoundWhenDeletingNonExistentAward() {
		webTestClient.delete().uri("/best-band-awards/{id}", 999L).exchange().expectStatus().isNotFound().expectBody().jsonPath("$.status").isEqualTo(404).jsonPath("$.message").isEqualTo("Ошибка выполнения операции").jsonPath("$.details[0].field").isEqualTo("service").jsonPath("$.details[0].errorType").isEqualTo("SERVICE_ERROR");
	}

	@Test
	void shouldReturnBadRequestWhenUpdatingWithInvalidData() {
		// Given
		BestBandAward award = bestBandAwardRepository.save(BestBandAward.builder().band(savedMusicBand).genre(ROCK).build());

		BestBandAwardRequest invalidUpdate = BestBandAwardRequest.builder().musicBandId(null) // Invalid
				.genre(null) // Invalid
				.build();

		// When & Then
		webTestClient.put().uri("/best-band-awards/{id}", award.getId()).contentType(MediaType.APPLICATION_JSON).bodyValue(invalidUpdate).exchange().expectStatus().isBadRequest().expectBody().jsonPath("$.status").isEqualTo(400).jsonPath("$.message").isEqualTo("Ошибка валидации данных").jsonPath("$.details.length()").isEqualTo(2);
	}

	@Test
	void shouldCreateAwardsWithDifferentGenres() {
		// Test creation with all available genres
		for (MusicGenre genre : MusicGenre.values()) {
			// Given
			BestBandAwardRequest request = BestBandAwardRequest.builder().musicBandId(savedMusicBand.getId()).genre(genre).build();

			// When & Then
			webTestClient.post().uri("/best-band-awards").contentType(MediaType.APPLICATION_JSON).bodyValue(request).exchange().expectStatus().isCreated().expectBody().jsonPath("$.genre").isEqualTo(genre.toString());
		}

		// Cleanup
		bestBandAwardRepository.deleteAll();
	}
}