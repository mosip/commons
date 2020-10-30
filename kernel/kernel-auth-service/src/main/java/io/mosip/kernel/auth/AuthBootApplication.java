package io.mosip.kernel.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"io.mosip.kernel.auth.controller","io.mosip.kernel.auth.config","${mosip.auth.adapter.impl.basepackage}","${mosip.iam.impl.basepackage}", "io.mosip.kernel.core.logger.config"})
public class AuthBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthBootApplication.class, args);

	}
}