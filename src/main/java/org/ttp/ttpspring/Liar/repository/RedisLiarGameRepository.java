package org.ttp.ttpspring.Liar.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.ttp.ttpspring.Liar.model.LiarGame;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Repository
public class RedisLiarGameRepository {
    private final RedisTemplate<String, LiarGame> redisTemplate;
    private static final String KEY_PREFIX = "liar_game:";
    private static final long GAME_EXPIRATION = 3600; // 1시간

    public RedisLiarGameRepository(RedisTemplate<String, LiarGame> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(String gameId, LiarGame game) {
        String key = KEY_PREFIX + gameId;
        redisTemplate.opsForValue().set(key, game, GAME_EXPIRATION, TimeUnit.SECONDS);
    }

    public Optional<LiarGame> findById(String gameId) {
        String key = KEY_PREFIX + gameId;
        LiarGame game = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(game);
    }

    public List<LiarGame> findAll() {
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return new ArrayList<>();
        }
        
        return keys.stream()
            .map(key -> redisTemplate.opsForValue().get(key))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public void delete(String gameId) {
        String key = KEY_PREFIX + gameId;
        redisTemplate.delete(key);
    }

    public void deleteAll() {
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    public boolean exists(String gameId) {
        String key = KEY_PREFIX + gameId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void refreshExpiration(String gameId) {
        String key = KEY_PREFIX + gameId;
        redisTemplate.expire(key, GAME_EXPIRATION, TimeUnit.SECONDS);
    }

    public long count() {
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
        return keys != null ? keys.size() : 0;
    }
}
