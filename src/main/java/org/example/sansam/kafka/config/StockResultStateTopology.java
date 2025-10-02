package org.example.sansam.kafka.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.example.sansam.global.event.StockDecreaseResultEvent;
import org.example.sansam.kafka.topic.KafkaTopics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;

@Configuration
@RequiredArgsConstructor
public class StockResultStateTopology {

    public static final String SR_STORE = "sr-store";
    private final JsonSerde<StockDecreaseResultEvent> srSerde;

    @Bean
    public KTable<String, StockDecreaseResultEvent> stockResultTable(StreamsBuilder builder) {
        return builder.table(
                KafkaTopics.STOCK_DECREASE_RESULT,
                Consumed.with(Serdes.String(), srSerde),
                Materialized
                        .<String, StockDecreaseResultEvent, KeyValueStore<Bytes, byte[]>>as(SR_STORE)
                        .withKeySerde(Serdes.String())
                        .withValueSerde(srSerde)
                        .withCachingDisabled()
        );
    }

}
