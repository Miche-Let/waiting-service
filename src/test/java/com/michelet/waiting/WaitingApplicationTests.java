package com.michelet.waiting;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class WaitingApplicationTests {

	// Kafka — 실제 연결 없이 Mock으로 대체
	@MockBean
	KafkaTemplate<String, Object> kafkaTemplate;

	// Redis — 실제 연결 없이 Mock으로 대체
	@MockBean
	RedisConnectionFactory redisConnectionFactory;

	// QueryDSL — JPAQueryFactory Mock
	@MockBean
	JPAQueryFactory jpaQueryFactory;

	@Test
	void contextLoads() {
	}

}
