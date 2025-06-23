package com.api.send.message.rabbitmq.service;

import com.api.send.message.rabbitmq.configuration.RabbitMQConfig;
import com.api.send.message.rabbitmq.dto.MensagemWhatsAppConsumerDTO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppConsumerService {

    private final WahaService wahaService;
    private final RabbitTemplate rabbitTemplate;
    private final MessageConverter messageConverter;

    public WhatsAppConsumerService(WahaService wahaService, RabbitTemplate rabbitTemplate, MessageConverter messageConverter) {
        this.wahaService = wahaService;
        this.rabbitTemplate = rabbitTemplate;
        this.messageConverter = messageConverter;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME_WHATSAPP, containerFactory = "rabbitListenerContainerFactory")
    public void consumirMensagem(MensagemWhatsAppConsumerDTO mensagem, Message rawMessage, Channel channel) {
        long deliveryTag = rawMessage.getMessageProperties().getDeliveryTag();

        Integer retryCount = (Integer) rawMessage.getMessageProperties()
                .getHeaders()
                .getOrDefault("x-retry-count", 0);

        try {
            System.out.printf("Tentativa %d: Enviando mensagem para %s%n", retryCount + 1, mensagem.getNumero());

            wahaService.enviarMensagem(mensagem.getNumero(), mensagem.getMensagem());

            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            System.err.println("Erro ao enviar mensagem para WAHA: " + e.getMessage());

            try {
                if (retryCount >= 9) {
                    System.err.println("Mensagem atingiu o limite de tentativas. Será descartada.");
                    channel.basicAck(deliveryTag, false);
                } else {
                    channel.basicAck(deliveryTag, false);

                    MessageProperties newProps = new MessageProperties();
                    newProps.setContentType("application/json");
                    newProps.setHeader("x-retry-count", retryCount + 1);

                    Message novaMensagem = messageConverter.toMessage(mensagem, newProps);

                    rabbitTemplate.send("exchange.whatsapp", "whatsapp.send", novaMensagem);

                    System.out.printf("Mensagem reencaminhada com tentativa %d%n", retryCount + 1);
                }

            } catch (Exception ex) {
                System.err.println("Erro ao tentar reenviar a mensagem: " + ex.getMessage());
            }
        }
    }
}
