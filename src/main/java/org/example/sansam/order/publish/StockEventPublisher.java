package org.example.sansam.order.publish;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.sansam.global.event.StockDecreaseRequestedEvent;
import org.example.sansam.global.event.StockIncreaseRequestedEvent;
import org.example.sansam.kafka.topic.KafkaTopics;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class StockEventPublisher {

    private final @Qualifier("decreaseKafkaTemplate")
    KafkaTemplate<Long, StockDecreaseRequestedEvent> kafkaDecreaseTemplate;

    private final @Qualifier("increaseKafkaTemplate")
    KafkaTemplate<Long, StockIncreaseRequestedEvent> kafkaIncreaseTemplate;

    public void publishStockDecreaseEvent(StockDecreaseRequestedEvent event){
        Long key =  Integer.toUnsignedLong(event.aggregateId().hashCode());
        kafkaDecreaseTemplate.send(KafkaTopics.STOCK_DECREASE_REQUEST,key,event)
                .whenComplete((result,ex)-> {
                    if(ex!=null){
                        log.error("Kafka로 재고 감소 이벤트 publishing 실패, eventId={}",event.eventId(),ex);
                    }else{
                        log.error("Kafka로 이벤트 publishing 성공, topic={}, partition ={}, offset ={}",
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }

    public void publishStockIncreaseEvent(StockIncreaseRequestedEvent event){
        Long key =  Integer.toUnsignedLong(event.aggregateId().hashCode());
        kafkaIncreaseTemplate.send(KafkaTopics.STOCK_INCREASE_REQUEST,key,event)
                .whenComplete((result,exception)->{
                    if(exception!=null) {
                        log.error("Kafka로 재고 증가 이벤트 publishing 실패, eventId={}", event.eventId(), exception);
                    }else{
                        log.info("Kafka로 재고 증가 이벤트 publishing 성공, topic={}, partition ={}, offset={}",
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }

}
