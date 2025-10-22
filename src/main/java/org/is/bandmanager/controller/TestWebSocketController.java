package org.is.bandmanager.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Controller
public class TestWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public TestWebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // Простой эхо-метод
    @MessageMapping("/test/echo")
    @SendTo("/topic/test")
    public String handleEcho(String message) {
        System.out.println("📨 Received echo message: " + message);
        return "ECHO: " + message + " | Server time: " + Instant.now();
    }

    // Метод для подписки на тестовые данные
    @MessageMapping("/test/subscribe")
    @SendTo("/topic/test-data")
    public TestResponse handleSubscribe() {
        System.out.println("📨 Received subscription request");

        // Создаем тестовые данные
        List<TestData> data = Arrays.asList(
                new TestData(1, "Metallica", "HEAVY_METAL", 4),
                new TestData(2, "The Beatles", "ROCK", 4),
                new TestData(3, "Miles Davis", "JAZZ", 5)
        );

        return new TestResponse("DATA", data, "All bands");
    }

    // Периодическая отправка данных (каждые 10 секунд)
    @Scheduled(fixedRate = 10000)
    public void sendPeriodicUpdate() {
        TestData liveData = new TestData(
                ThreadLocalRandom.current().nextInt(100, 1000),
                "Live Band " + Instant.now().getEpochSecond(),
                "POP",
                ThreadLocalRandom.current().nextInt(3, 8)
        );

        System.out.println("🔄 Sending live update: " + liveData.getName());
        messagingTemplate.convertAndSend("/topic/updates", liveData);
    }

    @Data
    @AllArgsConstructor
    public static class TestResponse {
        private String type;
        private List<TestData> data;
        private String filter;
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