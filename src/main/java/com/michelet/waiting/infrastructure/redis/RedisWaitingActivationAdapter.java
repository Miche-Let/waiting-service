package com.michelet.waiting.infrastructure.redis;

import com.michelet.waiting.application.port.WaitingActivationPort;
import com.michelet.waiting.domain.exception.WaitingErrorCode;
import com.michelet.waiting.domain.exception.WaitingException;
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
public class RedisWaitingActivationAdapter implements WaitingActivationPort {

    private final RedisTemplate<String,String> redisTemplate;
    private static final String PREFIX = "waiting:queue:";
    private static final String SEQ_PREFIX = "waiting:seq:";

    // key craete - "waiting:queue:{restaurantId}"
    // 식당별 독립적인 대기열 관리
    private String buildKey(UUID restaurantId){
        Objects.requireNonNull(restaurantId, "restaurantId must not be null");
        return PREFIX + restaurantId;
    }

    // seqKey create - "waiting:seq:{restaurantId}"
    // 식당별 시퀀스 관리
    private String buildSeqKey(UUID restaurantId){
        Objects.requireNonNull(restaurantId, "restaurantId must not be null");
        return SEQ_PREFIX + restaurantId;
    }

    // 대기열 등록
    // INCR로 시퀀스 생성 후 score로 사용
    @Override
    public void add(UUID restaurantId, String token) {

        Objects.requireNonNull(token, "token must not be null");
        if (token.isBlank())
            throw new WaitingException(WaitingErrorCode.INVALID_TOKEN);

        String key = buildKey(restaurantId);
        Long sequence = redisTemplate.opsForValue().increment(buildSeqKey(restaurantId));

        if (sequence == null)
            throw new WaitingException(WaitingErrorCode.QUEUE_SEQUENCE_FAILED);
        redisTemplate.opsForZSet()
                .add(key, token, sequence);
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
        String key = buildKey(restaurantId);
        Long count = redisTemplate.opsForZSet().size(key);
        return count != null ? count : 0L;
    }

    // 앞에서 N개 토큰 꺼내기 - 스케줄러에서 호출
    // ZPOPMIN -> score 낮은 순 N개 추출
    @Override
    public List<String> popNextTokens(UUID restaurantId, int count) {
        String key = buildKey(restaurantId);
        if(count <= 0) return List.of();
        Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet()
                .popMin(key, count);

        if (tuples == null || tuples.isEmpty()) return List.of();

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
