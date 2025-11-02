package org.is.bandmanager.util;

import lombok.NoArgsConstructor;
import org.is.auth.dto.request.UserRequest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;

@NoArgsConstructor
public class TestClient {

    private String authToken;

    private WebTestClient client;

    public TestClient(WebTestClient client, UserRequest request) {
        Map<String, Object> response = client.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .returnResult()
                .getResponseBody();

        if (response == null) {
            throw new IllegalStateException("Registration response is null");
        }

        @SuppressWarnings("unchecked")
        Map<String, String> tokens = (Map<String, String>) response.get("tokens");

        if (tokens == null) {
            throw new IllegalStateException("Tokens not found in response");
        }

        this.authToken = tokens.get("access_token");

        if (this.authToken == null) {
            throw new IllegalStateException("Access token not found in tokens");
        }

        this.client = client;
    }

    public WebTestClient.ResponseSpec post(String uri, Object body, Object... uriVariables) {
        return withAuth(
                client.post().uri(uri, uriVariables)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(body))
                .exchange();
    }

    public WebTestClient.ResponseSpec putWithBody(String uri, Object body, Object... uriVariables) {
        return withAuth(
                client.put().uri(uri, uriVariables)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(body))
                .exchange();
    }

    public WebTestClient.ResponseSpec put(String uri, Object... uriVariables) {
        return withAuth(
                client.put().uri(uri, uriVariables)
                        .contentType(MediaType.APPLICATION_JSON))
                .exchange();
    }

    public WebTestClient.ResponseSpec get(String uri, Object... uriVariables) {
        return withAuth(
                client.get().uri(uri, uriVariables))
                .exchange();
    }

    public WebTestClient.ResponseSpec delete(String uri, Object... uriVariables) {
        return withAuth(
                client.delete().uri(uri, uriVariables))
                .exchange();
    }

    public WebTestClient.RequestHeadersSpec<?> withAuth(WebTestClient.RequestHeadersSpec<?> request) {
        if (authToken != null) {
            request.header("Authorization", "Bearer " + authToken);
        }
        return request;
    }

}