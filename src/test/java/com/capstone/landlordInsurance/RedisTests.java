package com.capstone.landlordInsurance;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class RedisTests {

	@Autowired
	private RedisTemplate redisTemplate;

	@Test
	void testRedisDb(){
		redisTemplate.opsForValue().set("email", "gmail@email.com");
		Object email = redisTemplate.opsForValue().get("email");
		assertEquals("gmail@email.com", email);
	}

}
