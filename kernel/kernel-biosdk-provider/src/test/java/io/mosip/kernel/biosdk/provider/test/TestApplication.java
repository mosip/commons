package io.mosip.kernel.biosdk.provider.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;


@Configuration
@PropertySource(value = { "application-test.properties" })
@ComponentScan(basePackages = {"io.mosip.sdk.provider.factory", "io.mosip.sdk.provider.impl"})
@SpringBootApplication
public class TestApplication {
	
	/*public static void main(String[] args) {
		SpringApplication.run(TestApplication.class, args);	
	}*/
	
	/*@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {

			System.out.println("Let's inspect the beans provided by Spring Boot:");

			String[] beanNames = ctx.getBeanDefinitionNames();
			Arrays.sort(beanNames);
			for (String beanName : beanNames) {
				System.out.println(beanName);
			}

		};
	}*/
}
