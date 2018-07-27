package com.nexon.cloud;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.Map;

@Service
public class CacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private ValueOperations valueOperations;

    @PostConstruct
    private void init() {
        valueOperations = redisTemplate.opsForValue();
    }

    public void save(String uri, byte[] content, Map headers) {
        CacheEntity entity = new CacheEntity();
        entity.setContent(new String(content, Charset.forName("UTF-8")));
        entity.setHeaders(headers);

        try {
            String s = new ObjectMapper().writeValueAsString(entity);
            valueOperations.set(uri, s);
        } catch (Exception ex) {

        }

    }

    public CacheEntity load(String uri) {
        try {
            String s = (String) valueOperations.get(uri);
            if (s != null) {
                return new ObjectMapper().readValue(s, CacheEntity.class);
            } else {
                return null;
            }
        } catch (Exception ex) {
            return null;
        }
    }
}
