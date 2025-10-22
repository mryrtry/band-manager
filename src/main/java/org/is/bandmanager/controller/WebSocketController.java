package org.is.bandmanager.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.Instant;

@Controller
public class WebSocketController {

    @MessageMapping("/test")
    @SendTo("/topic/test")
    public String handleTestMessage(String message) {
        return "Echo: " + message + " | Time: " + Instant.now();
    }

}