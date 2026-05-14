package com.michelet.waiting;

import com.michelet.waiting.domain.repository.WaitingOutboxRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class WaitingApplicationTests {

	@MockBean
	RedisConnectionFactory redisConnectionFactory;

	@MockBean
	ReactiveRedisConnectionFactory reactiveRedisConnectionFactory;

	@MockBean
	JPAQueryFactory jpaQueryFactory;

	@MockBean
	WaitingOutboxRepository waitingOutboxRepository;

	@Test
	void contextLoads() {}
}