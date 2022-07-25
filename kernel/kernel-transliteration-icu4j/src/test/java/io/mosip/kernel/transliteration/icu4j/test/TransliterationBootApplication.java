package io.mosip.kernel.transliteration.icu4j.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "io.mosip.kernel.transliteration.icu4j.*")
public class TransliterationBootApplication {
	public static void main(String[] args) {
		SpringApplication.run(TransliterationBootApplication.class, args);
	}
}
