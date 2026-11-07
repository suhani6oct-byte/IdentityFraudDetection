package com.fivewheels.fraud;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"fraud.seed-enabled=false", "spring.datasource.url=jdbc:h2:mem:testdb", "spring.jpa.hibernate.ddl-auto=create-drop"})
class FraudApplicationTests {

	@Test
	void contextLoads() {
	}

}