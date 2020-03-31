package io.mosip.kernel.smsserviceprovider.msg91;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/*
 * (non-Javadoc)
 * IdValidator Boot Application for SpringBootTest
 */

@SpringBootApplication
@ComponentScan(basePackages = "io.mosip.kernel.*")
public class SMSServiceProviderBootApplication {
	public static void main(String[] args) {
		SpringApplication.run(SMSServiceProviderBootApplication.class, args);
	}
}
