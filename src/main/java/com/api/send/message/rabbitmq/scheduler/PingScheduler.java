package com.api.send.message.rabbitmq.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PingScheduler {

    private static final Logger logger = LoggerFactory.getLogger(PingScheduler.class);

    @Value("${app.ping.url}")
    private String pingUrl;

    private final RestTemplate restTemplate;

    public PingScheduler(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Scheduled(fixedRate = 300000) // 300000 ms = 5 minutos
    public void pingService() {
        try {
            String response = restTemplate.getForObject(pingUrl, String.class);
            logger.info("Ping bem-sucedido. Resposta: {}", response);
        } catch (Exception e) {
            logger.error("Erro ao realizar ping para {}: {}", pingUrl, e.getMessage());
        }
    }
}