package com.api.send.message.rabbitmq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MensagemWhatsAppConsumerDTO implements Serializable {
    private String numero;
    private String mensagem;
}
