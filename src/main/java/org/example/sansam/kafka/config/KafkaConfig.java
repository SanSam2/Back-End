package org.example.sansam.kafka.config;


import org.apache.kafka.clients.admin.NewTopic;
import org.example.sansam.global.event.StockDecreaseResultEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

import static org.example.sansam.kafka.topic.KafkaTopics.*;


@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic stockDecreaseTopic() {
        return TopicBuilder.name(STOCK_DECREASE_REQUEST)
                .partitions(12)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic stockDecreaseDlt(){
        return TopicBuilder.name(STOCK_DECREASE_DLT)
                .partitions(12)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic stockIncreaseTopic() {
        return TopicBuilder.name(STOCK_INCREASE_REQUEST)
                .partitions(12)
                .replicas(1)
                .build();
    }


    @Bean
    public NewTopic stockIncreaseDlt(){
        return TopicBuilder.name(STOCK_INCREASE_DLT)
                .partitions(12)
                .replicas(1)
                .build();
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, StockDecreaseResultEvent>
    kafkaResultListenerContainerFactory(
            ConsumerFactory<String, StockDecreaseResultEvent> cf
    ) {
        ConcurrentKafkaListenerContainerFactory<String, StockDecreaseResultEvent> f
                = new ConcurrentKafkaListenerContainerFactory<String, StockDecreaseResultEvent>();
        f.setConsumerFactory(cf);
        f.getContainerProperties().setPollTimeout(1500);
        f.getContainerProperties().setObservationEnabled(true);
        f.getContainerProperties().setAckMode(ContainerProperties.AckMode.BATCH);
        f.setConcurrency(12);
        return f;
    }

}
