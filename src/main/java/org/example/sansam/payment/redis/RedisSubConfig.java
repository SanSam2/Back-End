package org.example.sansam.payment.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class RedisSubConfig {

    @Bean
    RedisMessageListenerContainer srListenerContainer(
            RedisConnectionFactory cf, MessageListenerAdapter adapter) {
        var c = new RedisMessageListenerContainer();
        c.setConnectionFactory(cf);
        c.addMessageListener(adapter, new ChannelTopic("sr:ch"));
        return c;
    }

    @Bean
    MessageListenerAdapter srListenerAdapter(SRNotifier notifier) {
        // handleMessage(String message)
        return new MessageListenerAdapter(notifier, "handleMessage");
    }
}
