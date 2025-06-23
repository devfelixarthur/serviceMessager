package com.api.send.message.rabbitmq.controller;

import org.springframework.web.bind.annotation.GetMapping;

public class HealthController {
    @GetMapping("/health")
    public String healthCheck() {
        return "Serviço ativo! - " + java.time.LocalDateTime.now();
    }
}
