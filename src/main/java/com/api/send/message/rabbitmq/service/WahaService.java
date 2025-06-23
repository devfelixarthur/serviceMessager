package com.api.send.message.rabbitmq.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class WahaService {

    private static final String WAHA_URL = "http://localhost:3000/api/sendText";

    private final RestTemplate restTemplate = new RestTemplate();



    public void enviarMensagem(String numero, String mensagem) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("chatId", numero + "@c.us");
        body.put("text", mensagem);
        body.put("session", "default");

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(WAHA_URL, request, String.class);

        System.out.println("WAHA response: " + response.getBody());
    }
}
