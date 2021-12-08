package io.mosip.kernel.applicanttype.api.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "io.mosip.kernel.applicanttype.api.*")
public class ApplicantTypeBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApplicantTypeBootApplication.class, args);
	}

}
