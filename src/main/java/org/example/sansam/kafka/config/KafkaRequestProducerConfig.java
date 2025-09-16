package org.example.sansam.kafka.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.example.sansam.global.event.StockDecreaseRequestedEvent;
import org.example.sansam.global.event.StockIncreaseRequestedEvent;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;


@Configuration(proxyBeanMethods = false)
public class KafkaRequestProducerConfig {

    private static final Class<?> LONG_SER   = org.apache.kafka.common.serialization.LongSerializer.class;
    private static final Class<?> JSON_SER   = org.springframework.kafka.support.serializer.JsonSerializer.class;

    @Bean(name = "decreaseProducerFactory")
    public ProducerFactory<Long, StockDecreaseRequestedEvent> decreasePF(KafkaProperties props) {
        Map<String, Object> conf = props.buildProducerProperties();
        conf.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LONG_SER);
        conf.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JSON_SER);
        conf.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, true);
        // 소량/저지연
        conf.put(ProducerConfig.LINGER_MS_CONFIG, 0);
        conf.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "none");
        return new DefaultKafkaProducerFactory<>(conf);
    }

    @Bean(name = "decreaseKafkaTemplate")
    public KafkaTemplate<Long, StockDecreaseRequestedEvent> decreaseKT(
            ProducerFactory<Long, StockDecreaseRequestedEvent> pf) {
        return new KafkaTemplate<>(pf);
    }

    @Bean(name = "increaseProducerFactory")
    public ProducerFactory<Long, StockIncreaseRequestedEvent> increasePF(KafkaProperties props) {
        Map<String, Object> conf = props.buildProducerProperties();
        conf.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LONG_SER);
        conf.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JSON_SER);
        conf.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, true);
        conf.put(ProducerConfig.LINGER_MS_CONFIG, 0);
        conf.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "none");
        return new DefaultKafkaProducerFactory<>(conf);
    }

    @Bean(name = "increaseKafkaTemplate")
    public KafkaTemplate<Long, StockIncreaseRequestedEvent> increaseKT(
            ProducerFactory<Long, StockIncreaseRequestedEvent> pf) {
        return new KafkaTemplate<>(pf);
    }
}
