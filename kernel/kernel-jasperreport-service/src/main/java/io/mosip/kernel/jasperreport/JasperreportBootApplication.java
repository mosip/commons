package io.mosip.kernel.jasperreport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = { "io.mosip.kernel.auditmanager.*", "io.mosip.kernel.auth.*" })
public class JasperreportBootApplication {
	/**
	 * Main method to run spring boot application
	 * 
	 * @param args args
	 */
	public static void main(String[] args) {
		SpringApplication.run(JasperreportBootApplication.class, args);
	}
}
