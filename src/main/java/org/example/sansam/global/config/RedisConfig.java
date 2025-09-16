package org.example.sansam.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.sansam.stockreservation.cache.SRCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

//    //Redis 연결 설정
//    @Bean
//    public RedisConnectionFactory redisConnectionFactory() {
//        // 기본적으로 application.yml 의 spring.data.redis.host/port 사용
//        return new LettuceConnectionFactory();
//    }

    //RedisTemplate 설정, Key: String, Value: JSON 직렬화
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory,
            ObjectMapper objectMapper
    ) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());

        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));

        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));

        return template;
    }

    @Bean
    public RedisTemplate<String, SRCache> srTemplate(RedisConnectionFactory cf, ObjectMapper om) {
        RedisTemplate<String, SRCache> temp = new RedisTemplate<>();
        temp.setConnectionFactory(cf);
        temp.setKeySerializer(new StringRedisSerializer());

        Jackson2JsonRedisSerializer<SRCache> valueSer = new Jackson2JsonRedisSerializer<>(SRCache.class);
        temp.setValueSerializer(valueSer);

        temp.afterPropertiesSet();
        return temp;
    }


    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory cf) {
        return new StringRedisTemplate(cf);
    }
}

