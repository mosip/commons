package io.mosip.kernel.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = {"io.mosip.kernel.auth.controller","io.mosip.kernel.auth.config","io.mosip.kernel.auth.adapter.*","${mosip.iam.impl.package}"})
public class AuthBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthBootApplication.class, args);

	}
}