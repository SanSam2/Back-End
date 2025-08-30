package org.example.sansam.payment.policy;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CancellationPolicyConfig {

    @Bean
    public CancellationPolicy cancellationPolicy(){
        return new DefaultCancellationPolicy();
    }
}
