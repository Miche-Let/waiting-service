package com.michelet.waiting.infrastructure.redis;

import com.michelet.waiting.application.port.ScoredToken;
import com.michelet.waiting.application.port.WaitingActivationPort;
import com.michelet.waiting.domain.exception.WaitingErrorCode;
import com.michelet.waiting.domain.exception.WaitingException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisWaitingActivationAdapter implements WaitingActivationPort {

    private final RedisTemplate<String,String> redisTemplate;
    private static final String USER_PREFIX = "waiting:user:";
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

    // userKey create - "waiting:user:{restaurantId}:{userId}"
    // 유저별 식당 대기 등록 여부 관리
    private String buildUserKey(UUID restaurantId, UUID userId) {
        Objects.requireNonNull(restaurantId, "restaurantId must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        return USER_PREFIX + restaurantId + ":" + userId;
    }


    private void validateToken(String token){
        if(token == null || token.isBlank())
            throw new WaitingException(WaitingErrorCode.INVALID_TOKEN);
    }

    // 대기열 등록
    // INCR로 시퀀스 생성 후 score로 사용
    @Override
    public void add(UUID restaurantId, String token) {

        validateToken(token);
        String key = buildKey(restaurantId);

        Double existingScore = redisTemplate.opsForZSet().score(key, token);
        if (existingScore != null) return;

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
        validateToken(token);
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


    // 앞에서 N개 socre 포함해서 토큰 꺼내기
    // ZPOPMIN -> score 낮은 순 N개 추출
    @Override
    public List<ScoredToken> popNextTokensWithScore(UUID restaurantId, int count) {
        if (count <= 0) return List.of();
        Set<ZSetOperations.TypedTuple<String>> tuples =
        redisTemplate.opsForZSet()
                .popMin(buildKey(restaurantId), count);
        if (tuples == null || tuples.isEmpty()) return List.of();
        return tuples.stream()
                .filter(t -> t.getValue() != null && t.getScore() != null)
                .map(t -> new ScoredToken(
                        t.getValue(),
                        t.getScore().longValue()
                ))
                .toList();
    }

    @Override
    public void addWithScore(UUID restaurantId, String token, Long score) {
        validateToken(token);
        redisTemplate.opsForZSet()
                .add(buildKey(restaurantId), token, score);
    }

    @Override
    public void remove(UUID restaurantId, String token) {
        validateToken(token);
        redisTemplate.opsForZSet().remove(buildKey(restaurantId), token);
    }

    @Override
    public boolean tryAddUser(UUID restaurantId, UUID userId) {
        Objects.requireNonNull(restaurantId, "restaurantId must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        String key = buildUserKey(restaurantId, userId);
        // SETNX — 키가 없을 때만 저장, 있으면 false 반환
        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, "1");
        return Boolean.TRUE.equals(result);
    }

    // 유저 플래그 제거
    // 취소/완료/만료/ACTIVE 전환 시 호출
    @Override
    public void removeUser(UUID restaurantId, UUID userId) {
        redisTemplate.delete(buildUserKey(restaurantId, userId));
    }
}
