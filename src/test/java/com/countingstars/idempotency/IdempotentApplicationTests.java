package com.countingstars.idempotency;

import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class IdempotentApplicationTests {

	@Autowired
	private RedissonClient redissonClient;


	@Test
	void contextLoads() {
		RLock lock = redissonClient.getLock("test_lock");
		lock.lock();
		System.out.println("aaaaaaa");
		lock.unlock();
	}

}
