package org.example.sansam.order.domain.ordernumber;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;
import java.util.UUID;


@Configuration
public class OrderNumberingConfig {

    @Bean
    public Clock orderClock() {
        return Clock.system(ZoneId.of("Asia/Seoul"));
    }

    @Bean
    public OrderNumberPolicy orderNumberPolicy(Clock orderClock) {
        return new TimestampUuidOrderNumberPolicy(orderClock, UUID::randomUUID);
    }
}