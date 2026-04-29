package com.michelet.waiting.domain.entity;

import com.michelet.waiting.domain.enums.WaitingStatus;
import com.michelet.waiting.domain.exception.WaitingErrorCode;
import com.michelet.waiting.domain.exception.WaitingException;
import com.michelet.waiting.domain.vo.WaitingToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WaitingTest {
    private static final UUID USER_ID       = UUID.randomUUID();
    private static final UUID RESTAURANT_ID = UUID.randomUUID();

    // ── create() ────────────────────────────────────────────────

    @Test
    @DisplayName("대기 등록 시 WAITING 상태와 토큰이 생성된다")
    void create_success() {
        Waiting waiting = Waiting.create(USER_ID, RESTAURANT_ID);

        assertThat(waiting.getId()).isNotNull();              // UUID 즉시 생성
        assertThat(waiting.getUserId()).isEqualTo(USER_ID);
        assertThat(waiting.getRestaurantId()).isEqualTo(RESTAURANT_ID);
        assertThat(waiting.getToken()).isNotNull();
        assertThat(waiting.getToken().value()).isNotBlank();
        assertThat(waiting.getStatus()).isEqualTo(WaitingStatus.WAITING);
        assertThat(waiting.getEnteredAt()).isNotNull();
    }

    @Test
    @DisplayName("create() 를 두 번 호출하면 서로 다른 ID와 토큰이 생성된다")
    void create_generates_unique_id_and_token() {
        Waiting a = Waiting.create(USER_ID, RESTAURANT_ID);
        Waiting b = Waiting.create(USER_ID, RESTAURANT_ID);

        assertThat(a.getId()).isNotEqualTo(b.getId());
        assertThat(a.getToken().value()).isNotEqualTo(b.getToken().value());
    }

    // ── activate() ──────────────────────────────────────────────

    @Test
    @DisplayName("WAITING 상태에서 activate() 호출 시 ACTIVE 로 전환된다")
    void activate_success() {
        Waiting waiting = Waiting.create(USER_ID, RESTAURANT_ID);

        waiting.activate();

        assertThat(waiting.getStatus()).isEqualTo(WaitingStatus.ACTIVE);
    }

    @Test
    @DisplayName("WAITING 이 아닌 상태에서 activate() 호출 시 WaitingException 이 발생한다")
    void activate_fail_when_already_active() {
        Waiting waiting = Waiting.create(USER_ID, RESTAURANT_ID);
        waiting.activate(); // ACTIVE 로 전환

        assertThatThrownBy(waiting::activate)
                .isInstanceOf(WaitingException.class)
                .hasMessage(WaitingErrorCode.INVALID_STATE.getMessage());
    }

    // ── cancel() ────────────────────────────────────────────────

    @Test
    @DisplayName("WAITING 상태에서 cancel() 호출 시 CANCELLED 로 전환된다")
    void cancel_success() {
        Waiting waiting = Waiting.create(USER_ID, RESTAURANT_ID);

        waiting.cancel();

        assertThat(waiting.getStatus()).isEqualTo(WaitingStatus.CANCELLED);
    }

    @Test
    @DisplayName("ACTIVE 상태에서 cancel() 호출 시 WaitingException 이 발생한다")
    void cancel_fail_when_active() {
        Waiting waiting = Waiting.create(USER_ID, RESTAURANT_ID);
        waiting.activate();

        assertThatThrownBy(waiting::cancel)
                .isInstanceOf(WaitingException.class)
                .hasMessage(WaitingErrorCode.INVALID_STATE.getMessage());
    }

    // ── expire() ────────────────────────────────────────────────

    @Test
    @DisplayName("WAITING 상태에서 expire() 호출 시 EXPIRED 로 전환된다")
    void expire_success() {
        Waiting waiting = Waiting.create(USER_ID, RESTAURANT_ID);

        waiting.expire();

        assertThat(waiting.getStatus()).isEqualTo(WaitingStatus.EXPIRED);
    }

    @Test
    @DisplayName("ACTIVE 상태에서 expire() 호출 시 WaitingException 이 발생한다")
    void expire_fail_when_active() {
        Waiting waiting = Waiting.create(USER_ID, RESTAURANT_ID);
        waiting.activate();

        assertThatThrownBy(waiting::expire)
                .isInstanceOf(WaitingException.class)
                .hasMessage(WaitingErrorCode.INVALID_STATE.getMessage());
    }

    // ── isExpired() ─────────────────────────────────────────────

    @Test
    @DisplayName("ACTIVE 전환 후 10분 초과 시 isExpired() 는 true 를 반환한다")
    void isExpired_true_after_10minutes_from_activated() {
        Waiting waiting = Waiting.restore(
                UUID.randomUUID(), USER_ID, RESTAURANT_ID,
                WaitingToken.generate(),
                WaitingStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now().minusMinutes(11)  // activatedAt 11분 전
        );
        assertThat(waiting.isExpired()).isTrue();
    }

    @Test
    @DisplayName("enteredAt 이 10분 이내이면 isExpired() 는 false 를 반환한다")
    void isExpired_false_within_10_minutes() {
        Waiting waiting = Waiting.restore(
                UUID.randomUUID(), USER_ID, RESTAURANT_ID,
                WaitingToken.generate(),
                WaitingStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now().minusMinutes(9)  // activatedAt 9분 전
        );

        assertThat(waiting.isExpired()).isFalse();
    }

    // ── isActive() ──────────────────────────────────────────────

    @Test
    @DisplayName("ACTIVE 상태이면 isActive() 는 true 를 반환한다")
    void isActive_true_when_active() {
        Waiting waiting = Waiting.create(USER_ID, RESTAURANT_ID);
        waiting.activate();

        assertThat(waiting.isActive()).isTrue();
    }

    @Test
    @DisplayName("WAITING 상태는 enteredAt 이 오래되어도 isExpired() 는 false 를 반환한다")
    void  isExpired_false_when_waiting_even_if_entered_at_old() {
        Waiting waiting = Waiting.restore(
                UUID.randomUUID(), USER_ID, RESTAURANT_ID,
                WaitingToken.generate(),
                WaitingStatus.WAITING,
                LocalDateTime.now().minusHours(1),
                null);

        assertThat(waiting.isExpired()).isFalse();
    }

    @Test
    @DisplayName("WAITING 상태이면 isActive() 는 false 를 반환한다")
    void isActive_false_when_waiting() {
        Waiting waiting = Waiting.create(USER_ID, RESTAURANT_ID);

        assertThat(waiting.isActive()).isFalse();
    }
}
