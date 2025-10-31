package org.is.bandmanager.util;

import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

public class IntegrationTestUtil {

    public static WebTestClient.ResponseSpec performPost(WebTestClient client, String uri, Object body) {
        return client.post().uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange();
    }

    public static WebTestClient.ResponseSpec performPut(WebTestClient client, String uri, Object... uriVariables) {
        return client.put().uri(uri, uriVariables)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange();
    }

    public static WebTestClient.ResponseSpec performPutWithBody(WebTestClient client, String uri, Object body, Object... uriVariables) {
        return client.put().uri(uri, uriVariables)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange();
    }

    public static WebTestClient.ResponseSpec performGet(WebTestClient client, String uri, Object... uriVariables) {
        return client.get().uri(uri, uriVariables)
                .exchange();
    }

    public static WebTestClient.ResponseSpec performDelete(WebTestClient client, String uri, Object... uriVariables) {
        return client.delete().uri(uri, uriVariables)
                .exchange();
    }

}