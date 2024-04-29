package io.mosip.kernel.transliteration.icu4j.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = "io.mosip.kernel.*")
public class TransliterationBootApplication {
	public static void main(String[] args) {
		SpringApplication.run(TransliterationBootApplication.class, args);
	}
}
