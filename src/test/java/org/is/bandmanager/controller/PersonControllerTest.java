package org.is.bandmanager.controller;

import org.is.bandmanager.config.IntegrationTest;
import org.is.bandmanager.dto.request.LocationRequest;
import org.is.bandmanager.dto.request.PersonRequest;
import org.is.bandmanager.model.Location;
import org.is.bandmanager.model.Person;
import org.is.bandmanager.repository.LocationRepository;
import org.is.bandmanager.repository.PersonRepository;
import org.junit.jupiter.api.*;
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

    @BeforeAll
    void setClient() {
        this.webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @BeforeEach
    void setUp() {
        personRepository.deleteAll();
        locationRepository.deleteAll();
    }

    @AfterAll
    void cleanUp() {
        personRepository.deleteAll();
        locationRepository.deleteAll();
    }

    private LocationRequest createSampleLocationRequest() {
        return LocationRequest.builder()
                .x(10)
                .y(20L)
                .z(30L)
                .build();
    }

    private PersonRequest createSamplePersonRequest() {
        return PersonRequest.builder()
                .name("John Doe")
                .eyeColor(BLUE)
                .hairColor(BLACK)
                .location(createSampleLocationRequest())
                .weight(70f)
                .nationality(USA)
                .build();
    }

    @Test
    void shouldCreatePersonSuccessfully() {
        PersonRequest request = createSamplePersonRequest();

        webTestClient.post()
                .uri("/persons")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.name").isEqualTo("John Doe")
                .jsonPath("$.location.y").isEqualTo(20)
                .jsonPath("$.weight").isEqualTo(70);

        List<Person> persons = personRepository.findAll();
        assertThat(persons).hasSize(1);
        assertThat(persons.get(0).getLocation().getY()).isEqualTo(20L);
    }

    @Test
    void shouldReturnBadRequestWhenNameIsBlank() {
        PersonRequest request = createSamplePersonRequest();
        request.setName("");

        webTestClient.post()
                .uri("/persons")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message").isEqualTo("Ошибка валидации данных")
                .jsonPath("$.details[0].field").isEqualTo("name")
                .jsonPath("$.details[0].message").isEqualTo("Person.Name не может быть пустым")
                .jsonPath("$.details[0].errorType").isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void shouldReturnBadRequestWhenWeightIsNegative() {
        PersonRequest request = createSamplePersonRequest();
        request.setWeight(-5f);

        webTestClient.post()
                .uri("/persons")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message").isEqualTo("Ошибка валидации данных")
                .jsonPath("$.details[0].field").isEqualTo("weight")
                .jsonPath("$.details[0].message").isEqualTo("Person.Weight должно быть > 0")
                .jsonPath("$.details[0].errorType").isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void shouldReturnBadRequestWhenLocationIsNull() {
        PersonRequest request = createSamplePersonRequest();
        request.setLocation(null);

        webTestClient.post()
                .uri("/persons")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message").isEqualTo("Ошибка валидации данных")
                .jsonPath("$.details[0].field").isEqualTo("location")
                .jsonPath("$.details[0].message").isEqualTo("Person.LocationRequest не может быть пустым")
                .jsonPath("$.details[0].errorType").isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void shouldReturnBadRequestWhenMultipleValidationErrors() {
        PersonRequest request = createSamplePersonRequest();
        request.setName("");
        request.setWeight(-10f);
        request.setLocation(null);

        webTestClient.post()
                .uri("/persons")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message").isEqualTo("Ошибка валидации данных")
                .jsonPath("$.details.length()").isEqualTo(3);
    }

    @Test
    void shouldGetAllPersons() {
        PersonRequest request1 = createSamplePersonRequest();
        PersonRequest request2 = createSamplePersonRequest();
        request2.setName("Jane Doe");

        Person p1 = personRepository.save(Person.builder()
                .name(request1.getName())
                .eyeColor(request1.getEyeColor())
                .hairColor(request1.getHairColor())
                .location(Location.builder()
                        .x(request1.getLocation().getX())
                        .y(request1.getLocation().getY())
                        .z(request1.getLocation().getZ())
                        .build())
                .weight(request1.getWeight())
                .nationality(request1.getNationality())
                .build());

        Person p2 = personRepository.save(Person.builder()
                .name(request2.getName())
                .eyeColor(request2.getEyeColor())
                .hairColor(request2.getHairColor())
                .location(Location.builder()
                        .x(request2.getLocation().getX())
                        .y(request2.getLocation().getY())
                        .z(request2.getLocation().getZ())
                        .build())
                .weight(request2.getWeight())
                .nationality(request2.getNationality())
                .build());

        webTestClient.get()
                .uri("/persons")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].id").isEqualTo(p1.getId().intValue())
                .jsonPath("$[1].id").isEqualTo(p2.getId().intValue());
    }

    @Test
    void shouldGetPersonById() {
        PersonRequest request = createSamplePersonRequest();
        Person savedPerson = personRepository.save(Person.builder()
                .name(request.getName())
                .eyeColor(request.getEyeColor())
                .hairColor(request.getHairColor())
                .location(Location.builder()
                        .x(request.getLocation().getX())
                        .y(request.getLocation().getY())
                        .z(request.getLocation().getZ())
                        .build())
                .weight(request.getWeight())
                .nationality(request.getNationality())
                .build());

        webTestClient.get()
                .uri("/persons/{id}", savedPerson.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(savedPerson.getId())
                .jsonPath("$.location.y").isEqualTo(20);
    }

    @Test
    void shouldReturnNotFoundWhenGettingNonExistentPerson() {
        webTestClient.get()
                .uri("/persons/{id}", 999L)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").isEqualTo("Ошибка выполнения операции")
                .jsonPath("$.details[0].field").isEqualTo("service")
                .jsonPath("$.details[0].errorType").isEqualTo("SERVICE_ERROR");
    }

    @Test
    void shouldUpdatePersonSuccessfully() {
        PersonRequest request = createSamplePersonRequest();
        Person savedPerson = personRepository.save(Person.builder()
                .name(request.getName())
                .eyeColor(request.getEyeColor())
                .hairColor(request.getHairColor())
                .location(Location.builder()
                        .x(request.getLocation().getX())
                        .y(request.getLocation().getY())
                        .z(request.getLocation().getZ())
                        .build())
                .weight(request.getWeight())
                .nationality(request.getNationality())
                .build());

        PersonRequest updateRequest = createSamplePersonRequest();
        updateRequest.setName("Updated Name");
        updateRequest.getLocation().setY(999L);

        webTestClient.put()
                .uri("/persons/{id}", savedPerson.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Updated Name")
                .jsonPath("$.location.y").isEqualTo(999);

        Person updatedPerson = personRepository.findById(savedPerson.getId()).orElseThrow();
        assertThat(updatedPerson.getName()).isEqualTo("Updated Name");
        assertThat(updatedPerson.getLocation().getY()).isEqualTo(999L);
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentPerson() {
        PersonRequest updateRequest = createSamplePersonRequest();

        webTestClient.put()
                .uri("/persons/{id}", 999L)
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
    void shouldReturnBadRequestWhenUpdatingWithInvalidData() {
        PersonRequest request = createSamplePersonRequest();
        Person savedPerson = personRepository.save(Person.builder()
                .name(request.getName())
                .eyeColor(request.getEyeColor())
                .hairColor(request.getHairColor())
                .location(Location.builder()
                        .x(request.getLocation().getX())
                        .y(request.getLocation().getY())
                        .z(request.getLocation().getZ())
                        .build())
                .weight(request.getWeight())
                .nationality(request.getNationality())
                .build());

        PersonRequest invalidUpdate = createSamplePersonRequest();
        invalidUpdate.setName("");
        invalidUpdate.setWeight(-5f);

        webTestClient.put()
                .uri("/persons/{id}", savedPerson.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidUpdate)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message").isEqualTo("Ошибка валидации данных")
                .jsonPath("$.details.length()").isEqualTo(2);
    }

    @Test
    void shouldDeletePersonSuccessfully() {
        PersonRequest request = createSamplePersonRequest();
        Person savedPerson = personRepository.save(Person.builder()
                .name(request.getName())
                .eyeColor(request.getEyeColor())
                .hairColor(request.getHairColor())
                .location(Location.builder()
                        .x(request.getLocation().getX())
                        .y(request.getLocation().getY())
                        .z(request.getLocation().getZ())
                        .build())
                .weight(request.getWeight())
                .nationality(request.getNationality())
                .build());

        webTestClient.delete()
                .uri("/persons/{id}", savedPerson.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(savedPerson.getId());

        assertThat(personRepository.existsById(savedPerson.getId())).isFalse();
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentPerson() {
        webTestClient.delete()
                .uri("/persons/{id}", 999L)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").isEqualTo("Ошибка выполнения операции")
                .jsonPath("$.details[0].field").isEqualTo("service")
                .jsonPath("$.details[0].errorType").isEqualTo("SERVICE_ERROR");
    }

    @Test
    void shouldReturnBadRequestWhenCreatingWithMissingRequestBody() {
        webTestClient.post()
                .uri("/persons")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message").isEqualTo("Отсутствует тело запроса")
                .jsonPath("$.details[0].field").isEqualTo("requestBody")
                .jsonPath("$.details[0].errorType").isEqualTo("INVALID_JSON");
    }

}