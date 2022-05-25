package com.jazara.icu.auth.service;

import com.jazara.icu.auth.config.rabbitmq.ConfigureRabbitMq;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class ProduceCamService {

    private final RabbitTemplate rabbitTemplate;

    public ProduceCamService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public String produceMessage(String url) {
        rabbitTemplate.convertAndSend(ConfigureRabbitMq.EXCHANGE_NAME, "myRoutingKey.messages",
                url);
        return "Url(" + url + ")" + " has been produced.";
    }
}