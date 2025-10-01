package org.example.sansam.stockreservation.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.example.sansam.global.event.StockDecreaseResultEvent;
import org.example.sansam.kafka.config.StockResultStateTopology;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockResultView {

    private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    private ReadOnlyKeyValueStore<String, StockDecreaseResultEvent> store() {
        KafkaStreams ks = streamsBuilderFactoryBean.getKafkaStreams();
        if (ks == null) {
            throw new IllegalStateException("Kafka Stream이 아직 시작하지 않았습니다.");
        }

        return ks.store(
                StoreQueryParameters.fromNameAndType(
                        StockResultStateTopology.SR_STORE,
                        QueryableStoreTypes.keyValueStore()
                )
        );
    }

    public StockDecreaseResultEvent get(String orderId) {
        return store().get(orderId);
    }


    public StockDecreaseResultEvent waitGet(String orderId, Duration maxWait) {
        long end = System.nanoTime() + maxWait.toNanos();
        while (System.nanoTime() < end) {
            try {
                StockDecreaseResultEvent resultEvent = get(orderId);
                if (resultEvent != null)
                    return resultEvent;
            } catch (InvalidStateStoreException e) {
                //워밍업 대기
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {

            }
        }
        return null;
    }

}
