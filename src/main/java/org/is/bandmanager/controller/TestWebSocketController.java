package org.is.bandmanager.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Controller
public class TestWebSocketController {

    // Простой эхо-тест
    @MessageMapping("/test/echo")
    @SendTo("/topic/test")
    public String handleEcho(String message) {
        return "Echo: " + message + " | Server time: " + Instant.now();
    }

    // Тест с JSON объектом
    @MessageMapping("/test/subscribe")
    @SendTo("/topic/test-data")
    public TestResponse handleSubscribe(TestRequest request) {
        System.out.println("Received subscription: " + request.getFilter());

        // Имитируем данные
        List<TestData> data = List.of(
                new TestData(1, "Test Band 1", "ROCK", 5),
                new TestData(2, "Test Band 2", "JAZZ", 3),
                new TestData(3, "Test Band 3", "ROCK", 7)
        );

        return new TestResponse("DATA", data, request.getFilter());
    }

    @Scheduled(fixedRate = 5000)
    @SendTo("/topic/updates")
    public TestData sendPeriodicUpdate() {
        return new TestData(
                ThreadLocalRandom.current().nextInt(100),
                "Live Band " + Instant.now().getEpochSecond(),
                "POP",
                ThreadLocalRandom.current().nextInt(2, 10)
        );
    }

    @Data
    @AllArgsConstructor
    public static class TestRequest {
        private String filter;
        private String sort;
        private int page;
    }

    @Data
    @AllArgsConstructor
    public static class TestResponse {
        private String type;
        private List<TestData> data;
        private Object filter;
    }

    @Data
    @AllArgsConstructor
    public static class TestData {
        private Integer id;
        private String name;
        private String genre;
        private Integer participants;
    }

}

