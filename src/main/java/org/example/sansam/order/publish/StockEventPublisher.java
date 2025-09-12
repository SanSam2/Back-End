package org.example.sansam.order.publish;

import lombok.RequiredArgsConstructor;
import org.example.sansam.global.event.StockDecreaseRequestedEvent;
import org.example.sansam.global.event.StockIncreaseRequestedEvent;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static org.example.sansam.rabbitmq.RabbitNames.*;


@Component
@RequiredArgsConstructor
public class StockEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public void publishDecreaseRequested(StockDecreaseRequestedEvent event) {
        rabbitTemplate.convertAndSend(
                EXCHANGE, RK_REQ, event,
                message -> {
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    return message;
                }
        );
    }

    public void publishIncreaseRequested(StockIncreaseRequestedEvent event) {
        rabbitTemplate.convertAndSend(
                EXCHANGE, RK_INC_REQ, event,
                message -> {
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    return message;
                }
        );
    }
}
