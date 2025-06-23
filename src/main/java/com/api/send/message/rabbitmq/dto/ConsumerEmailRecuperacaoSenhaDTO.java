package com.api.send.message.rabbitmq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsumerEmailRecuperacaoSenhaDTO {
    private String email;
    private String token;
    private String urlRecuperacao;
}

