package org.is.bandmanager.controller;

import org.is.bandmanager.config.IntegrationTest;
import org.is.bandmanager.dto.request.PersonRequest;
import org.is.bandmanager.model.Color;
import org.is.bandmanager.model.Country;
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

@IntegrationTest
class PersonControllerTest extends AbstractIntegrationTest {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private LocationRepository locationRepository;

    private static Stream<Arguments> provideInvalidPersonRequests() {
        return Stream.of(
                Arguments.of("Blank name",
                        createPersonRequest("", Color.BLUE, Color.BROWN, 75.5f, Country.USA, 1L), "name"),
                Arguments.of("Null eye color",
                        createPersonRequest("John Doe", null, Color.BROWN, 75.5f, Country.USA, 1L), "eyeColor"),
                Arguments.of("Null hair color",
                        createPersonRequest("John Doe", Color.BLUE, null, 75.5f, Country.USA, 1L), "hairColor"),
                Arguments.of("Null location id",
                        createPersonRequest("John Doe", Color.BLUE, Color.BROWN, 75.5f, Country.USA, null), "locationId"),
                Arguments.of("Null weight",
                        createPersonRequest("John Doe", Color.BLUE, Color.BROWN, null, Country.USA, 1L), "weight"),
                Arguments.of("Zero weight",
                        createPersonRequest("John Doe", Color.BLUE, Color.BROWN, 0f, Country.USA, 1L), "weight"),
                Arguments.of("Negative weight",
                        createPersonRequest("John Doe", Color.BLUE, Color.BROWN, -5f, Country.USA, 1L), "weight"),
                Arguments.of("Null nationality",
                        createPersonRequest("John Doe", Color.BLUE, Color.BROWN, 75.5f, null, 1L), "nationality")
        );
    }

    private static PersonRequest createPersonRequest(String name, Color eyeColor, Color hairColor,
                                                     Float weight, Country nationality, Long locationId) {
        return PersonRequest.builder()
                .name(name)
                .eyeColor(eyeColor)
                .hairColor(hairColor)
                .weight(weight)
                .nationality(nationality)
                .locationId(locationId)
                .build();
    }

    @BeforeEach
    @AfterEach
    void cleanDatabase() {
        personRepository.deleteAll();
        locationRepository.deleteAll();
    }

    @Test
    void shouldCreatePersonSuccessfully() {
        // Given
        Location location = locationRepository.save(createLocation());
        PersonRequest request = createValidPersonRequest(location.getId());

        // When & Then
        getClient().post("/persons", request)
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.name").isEqualTo("John Doe")
                .jsonPath("$.eyeColor").isEqualTo("BLUE")
                .jsonPath("$.hairColor").isEqualTo("BROWN")
                .jsonPath("$.weight").isEqualTo(75.5)
                .jsonPath("$.nationality").isEqualTo("USA");

        // Verify database
        assertThat(personRepository.findAll()).hasSize(1);
    }

    @Test
    void shouldGetAllPersons() {
        // Given
        Location location = locationRepository.save(createLocation());
        Person person1 = createPerson("Person 1", Color.BLUE, Color.BROWN, 70.0f, Country.USA, location);
        Person person2 = createPerson("Person 2", Color.GREEN, Color.BLACK, 65.5f, Country.UK, location);
        personRepository.saveAll(List.of(person1, person2));

        // When & Then
        getClient().get("/persons")
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].name").isEqualTo("Person 1")
                .jsonPath("$[1].name").isEqualTo("Person 2");
    }

    @Test
    void shouldGetPersonById() {
        // Given
        Location location = locationRepository.save(createLocation());
        Person person = personRepository.save(createPerson("John Doe", Color.BLUE, Color.BROWN, 75.5f, Country.USA, location));

        // When & Then
        getClient().get("/persons/{id}", person.getId())
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(person.getId())
                .jsonPath("$.name").isEqualTo("John Doe")
                .jsonPath("$.eyeColor").isEqualTo("BLUE")
                .jsonPath("$.hairColor").isEqualTo("BROWN")
                .jsonPath("$.weight").isEqualTo(75.5)
                .jsonPath("$.nationality").isEqualTo("USA");
    }

    @Test
    void shouldUpdatePersonSuccessfully() {
        // Given
        Location location = locationRepository.save(createLocation());
        Person person = personRepository.save(createPerson("Old Name", Color.BLUE, Color.BROWN, 70.0f, Country.USA, location));
        PersonRequest updateRequest = createPersonRequest("Updated Name", Color.GREEN, Color.BLACK, 85.0f, Country.UK, location.getId());

        // When & Then
        getClient().putWithBody("/persons/{id}", updateRequest, person.getId())
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(person.getId())
                .jsonPath("$.name").isEqualTo("Updated Name")
                .jsonPath("$.eyeColor").isEqualTo("GREEN")
                .jsonPath("$.hairColor").isEqualTo("BLACK")
                .jsonPath("$.weight").isEqualTo(85.0)
                .jsonPath("$.nationality").isEqualTo("UK");

        // Verify in DB
        Person updated = personRepository.findById(person.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(updated.getEyeColor()).isEqualTo(Color.GREEN);
        assertThat(updated.getHairColor()).isEqualTo(Color.BLACK);
        assertThat(updated.getWeight()).isEqualTo(85.0f);
        assertThat(updated.getNationality()).isEqualTo(Country.UK);
    }

    @Test
    void shouldDeletePersonSuccessfully() {
        // Given
        Location location = locationRepository.save(createLocation());
        Person person = personRepository.save(createPerson("To Delete", Color.BLUE, Color.BROWN, 70.0f, Country.USA, location));

        // When & Then
        getClient().delete("/persons/{id}", person.getId())
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(person.getId())
                .jsonPath("$.name").isEqualTo("To Delete");

        // Verify DB deletion
        assertThat(personRepository.existsById(person.getId())).isFalse();
    }

    @Test
    void shouldReturnNotFoundWhenGettingNonExistentPerson() {
        getClient().get("/persons/{id}", 999L)
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").isEqualTo("Ошибка выполнения операции")
                .jsonPath("$.details[0].errorType").isEqualTo("SERVICE_ERROR");
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentPerson() {
        Location location = locationRepository.save(createLocation());
        PersonRequest updateRequest = createValidPersonRequest(location.getId());

        getClient().putWithBody("/persons/{id}", updateRequest, 999L)
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentPerson() {
        getClient().delete("/persons/{id}", 999L)
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidPersonRequests")
    void shouldReturnBadRequestWhenCreatingPersonWithInvalidData(
            String ignored, PersonRequest invalidRequest, String expectedErrorField) {

        getClient().post("/persons", invalidRequest)
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message").isEqualTo("Ошибка валидации данных")
                .jsonPath("$.details[0].field").isEqualTo(expectedErrorField);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidPersonRequests")
    void shouldReturnBadRequestWhenUpdatingPersonWithInvalidData(
            String ignored, PersonRequest invalidRequest, String expectedErrorField) {

        Location location = locationRepository.save(createLocation());
        Person person = personRepository.save(createPerson("Valid Person", Color.BLUE, Color.BROWN, 70.0f, Country.USA, location));

        getClient().putWithBody("/persons/{id}", invalidRequest, person.getId())
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.details[0].field").isEqualTo(expectedErrorField);
    }

    @Test
    void shouldReturnBadRequestWhenCreatingPersonWithMultipleErrors() {
        PersonRequest request = createPersonRequest("", null, null, -5f, null, null);

        getClient().post("/persons", request)
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.details.length()").isEqualTo(6);
    }

    private PersonRequest createValidPersonRequest(Long locationId) {
        return createPersonRequest("John Doe", Color.BLUE, Color.BROWN, 75.5f, Country.USA, locationId);
    }

    private Person createPerson(String name, Color eyeColor, Color hairColor, Float weight, Country nationality, Location location) {
        return Person.builder()
                .name(name)
                .eyeColor(eyeColor)
                .hairColor(hairColor)
                .location(location)
                .weight(weight)
                .nationality(nationality)
                .build();
    }

    private Location createLocation() {
        return Location.builder()
                .x(1)
                .y(1L)
                .z(1L)
                .build();
    }

}