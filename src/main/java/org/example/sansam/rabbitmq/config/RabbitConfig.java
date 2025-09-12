package org.example.sansam.rabbitmq.config;


import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;

import static org.example.sansam.rabbitmq.RabbitNames.EXCHANGE;

@Configuration
public class RabbitConfig {

    @Bean
    TopicExchange stockExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }


    @Bean
    MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory cf, MessageConverter converter) {
        RabbitTemplate t = new RabbitTemplate(cf);
        t.setMessageConverter(converter);
        t.setMandatory(true);
        t.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                System.err.println("RabbitMQ message not delivered. Cause: " + cause);
            }
        });
        t.setReturnsCallback(returnedMessage -> {
            System.err.println("RabbitMQ message not routed. Message: " + returnedMessage);
        });

        t.setRetryTemplate(RetryTemplate.builder().maxAttempts(3).
                exponentialBackoff(100,2.0,2000).build());
        return t;
    }



}
