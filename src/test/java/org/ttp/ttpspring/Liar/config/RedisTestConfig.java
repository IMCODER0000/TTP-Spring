package org.ttp.ttpspring.Liar.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.ttp.ttpspring.Liar.model.LiarGame;
import org.ttp.ttpspring.Liar.repository.RedisLiarGameRepository;
import org.ttp.ttpspring.Liar.service.RedisLiarGameService;

@TestConfiguration(proxyBeanMethods = false)
public class RedisTestConfig {

    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory();
    }

    @Bean
    @Primary
    public RedisTemplate<String, LiarGame> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, LiarGame> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    @Bean
    @Primary
    public RedisLiarGameRepository redisLiarGameRepository(RedisTemplate<String, LiarGame> redisTemplate) {
        return new RedisLiarGameRepository(redisTemplate);
    }

    @Bean
    @Primary
    public RedisLiarGameService redisLiarGameService(RedisLiarGameRepository repository) {
        return new RedisLiarGameService(repository);
    }
}
