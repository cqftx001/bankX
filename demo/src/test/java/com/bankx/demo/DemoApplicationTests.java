package com.bankx.demo;

import com.bankx.demo.security.properties.JwtProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DemoApplicationTests {

	@Autowired
	private JwtProperties jwtProperties;

	@Test
	void testVariable() {
		System.out.println("ENCODED_SECRET_KEY=" + System.getenv("ENCODED_SECRET_KEY"));
		System.out.println("JWT_TTL=" + jwtProperties.getTtl());
		System.out.println("OSS_ENDPOINT=" + System.getenv("OSS_ENDPOINT"));
	}
}