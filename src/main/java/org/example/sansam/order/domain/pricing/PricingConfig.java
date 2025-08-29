package org.example.sansam.order.domain.pricing;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PricingConfig {

    @Bean
    public PricingPolicy pricingPolicy() {
        return new BasicPricingPolicy();
    }
}
