package io.mosip.kernel.datamapper.orika.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/*
 * (non-Javadoc)
 * Data Validator Boot Application for SpringBootTest
 */
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = "io.mosip.kernel.datamapper.orika.test")
public class DataMapperBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataMapperBootApplication.class, args);

	}
}
