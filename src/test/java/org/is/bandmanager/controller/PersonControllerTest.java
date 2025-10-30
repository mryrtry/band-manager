package org.is.bandmanager.controller;

import org.is.bandmanager.config.IntegrationTest;
import org.is.bandmanager.dto.request.PersonRequest;
import org.is.bandmanager.model.Location;
import org.is.bandmanager.model.Person;
import org.is.bandmanager.repository.LocationRepository;
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
import static org.is.bandmanager.model.Color.BLACK;
import static org.is.bandmanager.model.Color.BLUE;
import static org.is.bandmanager.model.Country.USA;

@IntegrationTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PersonControllerTest extends AbstractIntegrationTest {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private LocationRepository locationRepository;

    @LocalServerPort
    private int port;

    private WebTestClient webTestClient;

    private Location savedLocation;

    @BeforeAll
    void setClient() {
        this.webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    @BeforeEach
    void setUp() {
        personRepository.deleteAll();
        locationRepository.deleteAll();

        // Создаем тестовую локацию для использования в тестах
        savedLocation = locationRepository.save(Location.builder().x(10).y(20L).z(30L).build());
    }

    @AfterAll
    void cleanUp() {
        personRepository.deleteAll();
        locationRepository.deleteAll();
    }

    private PersonRequest createSamplePersonRequest() {
        return PersonRequest.builder().name("John Doe").eyeColor(BLUE).hairColor(BLACK).locationId(savedLocation.getId()) // Используем ID существующей локации
                .weight(70f).nationality(USA).build();
    }

    @Test
    void shouldCreatePersonSuccessfully() {
        PersonRequest request = createSamplePersonRequest();

        webTestClient.post().uri("/persons").contentType(MediaType.APPLICATION_JSON).bodyValue(request).exchange().expectStatus().isCreated().expectBody().jsonPath("$.id").exists().jsonPath("$.name").isEqualTo("John Doe").jsonPath("$.location.y").isEqualTo(20).jsonPath("$.weight").isEqualTo(70);

        List<Person> persons = personRepository.findAll();
        assertThat(persons).hasSize(1);
        assertThat(persons.get(0).getLocation().getId()).isEqualTo(savedLocation.getId());
        assertThat(persons.get(0).getLocation().getY()).isEqualTo(20L);
    }

    @Test
    void shouldReturnBadRequestWhenNameIsBlank() {
        PersonRequest request = createSamplePersonRequest();
        request.setName("");

        webTestClient.post().uri("/persons").contentType(MediaType.APPLICATION_JSON).bodyValue(request).exchange().expectStatus().isBadRequest().expectBody().jsonPath("$.status").isEqualTo(400).jsonPath("$.message").isEqualTo("Ошибка валидации данных").jsonPath("$.details[0].field").isEqualTo("name").jsonPath("$.details[0].message").isEqualTo("Person.Name не может быть пустым").jsonPath("$.details[0].errorType").isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void shouldReturnBadRequestWhenWeightIsNegative() {
        PersonRequest request = createSamplePersonRequest();
        request.setWeight(-5f);

        webTestClient.post().uri("/persons").contentType(MediaType.APPLICATION_JSON).bodyValue(request).exchange().expectStatus().isBadRequest().expectBody().jsonPath("$.status").isEqualTo(400).jsonPath("$.message").isEqualTo("Ошибка валидации данных").jsonPath("$.details[0].field").isEqualTo("weight").jsonPath("$.details[0].message").isEqualTo("Person.Weight должно быть > 0").jsonPath("$.details[0].errorType").isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void shouldReturnBadRequestWhenLocationIdIsNull() {
        PersonRequest request = createSamplePersonRequest();
        request.setLocationId(null);

        webTestClient.post().uri("/persons").contentType(MediaType.APPLICATION_JSON).bodyValue(request).exchange().expectStatus().isBadRequest().expectBody().jsonPath("$.status").isEqualTo(400).jsonPath("$.message").isEqualTo("Ошибка валидации данных").jsonPath("$.details[0].field").isEqualTo("locationId").jsonPath("$.details[0].message").isEqualTo("Person.LocationId не может быть пустым").jsonPath("$.details[0].errorType").isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void shouldReturnNotFoundWhenLocationIdDoesNotExist() {
        PersonRequest request = createSamplePersonRequest();
        request.setLocationId(999L); // Несуществующий ID

        webTestClient.post().uri("/persons").contentType(MediaType.APPLICATION_JSON).bodyValue(request).exchange().expectStatus().isNotFound().expectBody().jsonPath("$.status").isEqualTo(404).jsonPath("$.message").isEqualTo("Ошибка выполнения операции");
    }

    @Test
    void shouldReturnBadRequestWhenMultipleValidationErrors() {
        PersonRequest request = createSamplePersonRequest();
        request.setName("");
        request.setWeight(-10f);
        request.setLocationId(null);

        webTestClient.post().uri("/persons").contentType(MediaType.APPLICATION_JSON).bodyValue(request).exchange().expectStatus().isBadRequest().expectBody().jsonPath("$.status").isEqualTo(400).jsonPath("$.message").isEqualTo("Ошибка валидации данных").jsonPath("$.details.length()").isEqualTo(3);
    }

    @Test
    void shouldGetAllPersons() {
        // Создаем несколько персон
        Person person1 = personRepository.save(Person.builder().name("John Doe").eyeColor(BLUE).hairColor(BLACK).location(savedLocation).weight(70f).nationality(USA).build());

        Person person2 = personRepository.save(Person.builder().name("Jane Doe").eyeColor(BLACK).hairColor(BLUE).location(savedLocation).weight(65f).nationality(USA).build());

        webTestClient.get().uri("/persons").exchange().expectStatus().isOk().expectBody().jsonPath("$.length()").isEqualTo(2).jsonPath("$[0].id").isEqualTo(person1.getId().intValue()).jsonPath("$[1].id").isEqualTo(person2.getId().intValue());
    }

    @Test
    void shouldGetPersonById() {
        Person savedPerson = personRepository.save(Person.builder().name("John Doe").eyeColor(BLUE).hairColor(BLACK).location(savedLocation).weight(70f).nationality(USA).build());

        webTestClient.get().uri("/persons/{id}", savedPerson.getId()).exchange().expectStatus().isOk().expectBody().jsonPath("$.id").isEqualTo(savedPerson.getId()).jsonPath("$.name").isEqualTo("John Doe").jsonPath("$.location.y").isEqualTo(20);
    }

    @Test
    void shouldReturnNotFoundWhenGettingNonExistentPerson() {
        webTestClient.get().uri("/persons/{id}", 999L).exchange().expectStatus().isNotFound().expectBody().jsonPath("$.status").isEqualTo(404).jsonPath("$.message").isEqualTo("Ошибка выполнения операции").jsonPath("$.details[0].field").isEqualTo("service").jsonPath("$.details[0].errorType").isEqualTo("SERVICE_ERROR");
    }

    @Test
    void shouldUpdatePersonSuccessfully() {
        Person savedPerson = personRepository.save(Person.builder().name("John Doe").eyeColor(BLUE).hairColor(BLACK).location(savedLocation).weight(70f).nationality(USA).build());

        // Создаем новую локацию для обновления
        Location newLocation = locationRepository.save(Location.builder().x(100).y(200L).z(300L).build());

        PersonRequest updateRequest = PersonRequest.builder().name("Updated Name").eyeColor(BLACK).hairColor(BLUE).locationId(newLocation.getId()).weight(75f).nationality(USA).build();

        webTestClient.put().uri("/persons/{id}", savedPerson.getId()).contentType(MediaType.APPLICATION_JSON).bodyValue(updateRequest).exchange().expectStatus().isOk().expectBody().jsonPath("$.name").isEqualTo("Updated Name").jsonPath("$.location.y").isEqualTo(200);

        Person updatedPerson = personRepository.findById(savedPerson.getId()).orElseThrow();
        assertThat(updatedPerson.getName()).isEqualTo("Updated Name");
        assertThat(updatedPerson.getLocation().getId()).isEqualTo(newLocation.getId());
        assertThat(updatedPerson.getWeight()).isEqualTo(75f);
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentPerson() {
        PersonRequest updateRequest = createSamplePersonRequest();

        webTestClient.put().uri("/persons/{id}", 999L).contentType(MediaType.APPLICATION_JSON).bodyValue(updateRequest).exchange().expectStatus().isNotFound().expectBody().jsonPath("$.status").isEqualTo(404).jsonPath("$.message").isEqualTo("Ошибка выполнения операции").jsonPath("$.details[0].field").isEqualTo("service").jsonPath("$.details[0].errorType").isEqualTo("SERVICE_ERROR");
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingWithInvalidData() {
        Person savedPerson = personRepository.save(Person.builder().name("John Doe").eyeColor(BLUE).hairColor(BLACK).location(savedLocation).weight(70f).nationality(USA).build());

        PersonRequest invalidUpdate = PersonRequest.builder().name("").eyeColor(BLUE).hairColor(BLACK).locationId(savedLocation.getId()).weight(-5f).nationality(USA).build();

        webTestClient.put().uri("/persons/{id}", savedPerson.getId()).contentType(MediaType.APPLICATION_JSON).bodyValue(invalidUpdate).exchange().expectStatus().isBadRequest().expectBody().jsonPath("$.status").isEqualTo(400).jsonPath("$.message").isEqualTo("Ошибка валидации данных").jsonPath("$.details.length()").isEqualTo(2);
    }

    @Test
    void shouldDeletePersonSuccessfully() {
        Person savedPerson = personRepository.save(Person.builder().name("John Doe").eyeColor(BLUE).hairColor(BLACK).location(savedLocation).weight(70f).nationality(USA).build());

        webTestClient.delete().uri("/persons/{id}", savedPerson.getId()).exchange().expectStatus().isOk().expectBody().jsonPath("$.id").isEqualTo(savedPerson.getId());

        assertThat(personRepository.existsById(savedPerson.getId())).isFalse();
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentPerson() {
        webTestClient.delete().uri("/persons/{id}", 999L).exchange().expectStatus().isNotFound().expectBody().jsonPath("$.status").isEqualTo(404).jsonPath("$.message").isEqualTo("Ошибка выполнения операции").jsonPath("$.details[0].field").isEqualTo("service").jsonPath("$.details[0].errorType").isEqualTo("SERVICE_ERROR");
    }

    @Test
    void shouldReturnBadRequestWhenCreatingWithMissingRequestBody() {
        webTestClient.post().uri("/persons").contentType(MediaType.APPLICATION_JSON).exchange().expectStatus().isBadRequest().expectBody().jsonPath("$.status").isEqualTo(400).jsonPath("$.message").isEqualTo("Отсутствует тело запроса").jsonPath("$.details[0].field").isEqualTo("requestBody").jsonPath("$.details[0].errorType").isEqualTo("INVALID_JSON");
    }

}