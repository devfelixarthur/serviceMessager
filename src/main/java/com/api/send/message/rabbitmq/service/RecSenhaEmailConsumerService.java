package com.api.send.message.rabbitmq.service;

import com.api.send.message.rabbitmq.configuration.RabbitMQConfig;
import com.api.send.message.rabbitmq.dto.ConsumerEmailRecuperacaoSenhaDTO;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

@Service
public class RecSenhaEmailConsumerService {
    private final RabbitTemplate rabbitTemplate;
    private final MessageConverter messageConverter;
    private final BrevoEmailService brevoEmailService;

    public RecSenhaEmailConsumerService(RabbitTemplate rabbitTemplate, MessageConverter messageConverter, BrevoEmailService brevoEmailService) {
        this.rabbitTemplate = rabbitTemplate;
        this.messageConverter = messageConverter;
        this.brevoEmailService = brevoEmailService;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME_REC_SENHA, containerFactory = "rabbitListenerContainerFactory")
    public void consumirMensagem(ConsumerEmailRecuperacaoSenhaDTO mensagem, Message rawMessage, Channel channel) {
        long deliveryTag = rawMessage.getMessageProperties().getDeliveryTag();

        Integer retryCount = (Integer) rawMessage.getMessageProperties()
                .getHeaders()
                .getOrDefault("x-retry-count", 0);

        try {
            System.out.printf("Tentativa %d: Enviando mensagem para %s%n", retryCount + 1, mensagem.getEmail());

            ClassPathResource resource = new ClassPathResource("templates/EmailRecSenhaTemplate.html");
            String htmlContent = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

            String emailContent = htmlContent.replace("[URL_RECUPERACAO]", mensagem.getUrlRecuperacao());

            brevoEmailService.enviarEmail(mensagem.getEmail(), emailContent);

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
