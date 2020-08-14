package io.mosip.commons.packetmanager.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "io.mosip.commons.packetmanager.*")
//@Profile("test")
//@Import(TestSecurityConfig.class)
public class TestBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestBootApplication.class, args);
	}
}