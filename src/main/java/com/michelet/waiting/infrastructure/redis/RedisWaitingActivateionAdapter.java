package com.michelet.waiting.infrastructure.redis;

import com.michelet.waiting.application.port.WaitingActivationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RedisWaitingActivateionAdapter implements WaitingActivationPort {

    private final RedisTemplate<String,String> redisTemplate;
    private static final String PREFIX = "waiting:queue:";

    // key craete - "waiting:queue:{restaurantId}"
    // 식당별 독립적인 대기열 관리
    private String buildKey(UUID restaurantId){
        Objects.requireNonNull(restaurantId, "restaurantId must not be null");
        return PREFIX + restaurantId;
    }

    // 대기열 등록
    // score = 현재 시각 -> 먼저 들어올 수록 낮은 rank
    @Override
    public void add(UUID restaurantId, String token) {
        String key = buildKey(restaurantId);
        redisTemplate.opsForZSet()
                .add(key, token, System.currentTimeMillis());
    }

    // 순번 조회
    // ZRANK 0 based 반환이라 +1 해서 1 based로 변환
    @Override
    public Long getPosition(UUID restaurantId, String token) {
        String key = buildKey(restaurantId);
        Long rank = redisTemplate.opsForZSet().rank(key,token);
        return rank != null ? rank + 1 : null;
    }

    // 전체 대기 인원 조회
    // Redis ZCARD
    @Override
    public Long countWaiting(UUID restaurantId) {
        Long count = redisTemplate.opsForZSet().size(buildKey(restaurantId));
        return count != null ? count : 0L;
    }

    // 앞에서 N개 토큰 꺼내기 - 스케줄러에서 호출
    // ZPOPMIN -> score 낮은 순 N개 추출
    @Override
    public List<String> popNextTokens(UUID restaurantId, int count) {
        Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet()
                .popMin(buildKey(restaurantId), count);

        if(tuples == null) return List.of();

        return tuples.stream()
                .map(ZSetOperations.TypedTuple::getValue)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public void remove(UUID restaurantId, String token) {
        redisTemplate.opsForZSet().remove(buildKey(restaurantId), token);
    }
}
