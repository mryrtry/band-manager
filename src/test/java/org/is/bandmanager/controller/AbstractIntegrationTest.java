package org.is.bandmanager.controller;

import org.is.auth.dto.request.UserRequest;
import org.is.auth.repository.UserRepository;
import org.is.bandmanager.util.TestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER;

    static {
        POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:15-alpine")
                .withDatabaseName("band_manager_test")
                .withUsername("test_user")
                .withPassword("test_password");
        POSTGRESQL_CONTAINER.start();
    }

    protected TestClient client;

    @Autowired
    UserRepository userRepository;

    @Autowired
    WebTestClient webTestClient;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRESQL_CONTAINER::getDriverClassName);
    }

    protected TestClient getClient() {
        if (client == null) {
            userRepository.deleteAll();
            if (client == null) {
                client = new TestClient(
                        webTestClient,
                        new UserRequest("mryrt", "password")
                );
            }
        }
        return client;
    }

}