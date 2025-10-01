package org.example.sansam.kafka.config;

import org.example.sansam.global.event.StockDecreaseResultEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;

@Configuration
public class SerdeConfig {

    @Bean
    public JsonSerde<StockDecreaseResultEvent> stockDecreaseResultSerde() {
        JsonSerde<StockDecreaseResultEvent> s = new JsonSerde<>(StockDecreaseResultEvent.class);
        s.deserializer().ignoreTypeHeaders();
        return s;
    }
}
