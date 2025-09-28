package org.example.sansam.stockreservation.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.global.event.StockDecreaseResultEvent;
import org.example.sansam.kafka.topic.KafkaTopics;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockEventListener {

    @KafkaListener(
            topics ={KafkaTopics.STOCK_DECREASE_CONFIRM, KafkaTopics.STOCK_DECREASE_REJECTED},
            containerFactory = "kafkaResultListenerContainerFactory"
    )
    public void onResult(StockDecreaseResultEvent e) {
        if(e.type().equals("STOCK_DECREASE_CONFIRM")) {
            //TODO: 어떻게 재고 감소의 결과를 받아서, 결제에 대한 승인을 마무리 지을 것인가.

        }
    }
}
