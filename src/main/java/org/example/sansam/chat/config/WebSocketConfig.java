package org.example.sansam.chat.config;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import org.springframework.beans.factory.annotation.Value;

@Configuration
@EnableWebSocketMessageBroker
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private MeterRegistry meterRegistry;

    @Bean
    public ThreadPoolTaskScheduler websocketBrokerTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(200); // 원하는 풀 사이즈
        scheduler.setThreadNamePrefix("ws-broker-");
        scheduler.initialize();
        return scheduler;
    }

    @Value("${cors.allowed-origins}")
    private String[] allowedOrigins;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub")
                .setTaskScheduler(websocketBrokerTaskScheduler())
                .setHeartbeatValue(new long[]{10000, 10000}); // 5초 단위
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns(allowedOrigins)
                .addInterceptors(new HttpSessionHandshakeInterceptor())
                .withSockJS();
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

                if (StompCommand.MESSAGE.equals(accessor.getCommand())) {
                    long startTime = (long) accessor.getSessionAttributes().getOrDefault("startTime", System.currentTimeMillis());
                    long duration = System.currentTimeMillis() - startTime;
                    meterRegistry.timer("ws.message.time").record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);
                }
            }
        });
    }

}