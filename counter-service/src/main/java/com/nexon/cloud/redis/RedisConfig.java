package com.nexon.cloud.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Random;

@Configuration
public class RedisConfig {

    @Autowired
    private Environment environment;


    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        String redisHost = environment.getProperty("spring.redis-host");

        JedisConnectionFactory factory = new JedisConnectionFactory();
        factory.setHostName(redisHost.split(":")[0]);
        factory.setPort(Integer.parseInt(redisHost.split(":")[1]));
        factory.setUsePool(true);
        return factory;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(jedisConnectionFactory());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate() {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(jedisConnectionFactory());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }

    @Bean
    public RedisCacheManager cacheManager(JedisConnectionFactory jedisConnectionFactory) {
        RedisCacheManager cacheManager = RedisCacheManager.create(jedisConnectionFactory);
        cacheManager.setTransactionAware(true);
        return cacheManager;
    }

    @Bean
    public String myApplicationId() {
        return "" + new Random().nextInt(Integer.MAX_VALUE);
    }
}
