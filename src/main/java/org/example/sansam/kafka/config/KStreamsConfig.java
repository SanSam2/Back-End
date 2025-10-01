package org.example.sansam.kafka.config;


import org.apache.kafka.streams.StreamsConfig;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;

import java.util.HashMap;

@Configuration
@EnableKafkaStreams
public class KStreamsConfig {
    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    KafkaStreamsConfiguration kStreamsConfigs(KafkaProperties props,
                                              ObjectProvider<SslBundles> bundles) {
        HashMap<String, Object> conf = new HashMap<>(props.buildStreamsProperties(bundles.getIfAvailable()));
        conf.put(StreamsConfig.APPLICATION_ID_CONFIG, "sansam-stock-view");
        return new KafkaStreamsConfiguration(conf);
    }
}
