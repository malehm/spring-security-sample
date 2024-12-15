package com.malem.sample.spring.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

import io.restassured.RestAssured;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class SpringSecurityApplicationTests {
	
	@LocalServerPort
    private Integer port;
	
	@BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

	@Test
	void contextLoads() {
		RestAssured
		.when()
		.get("/api/v1/prices")
		.then()
		.statusCode(200)
		;
	}

}
