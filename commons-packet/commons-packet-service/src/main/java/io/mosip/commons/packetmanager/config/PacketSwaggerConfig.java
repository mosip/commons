package io.mosip.commons.packetmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * The Class RegistrationStatusConfig.
 */
@Configuration
@EnableSwagger2
public class PacketSwaggerConfig {

	/**
	 * Registration status bean.
	 *
	 * @return the docket
	 */
	@Bean
	@Primary
	public Docket swaggerBean() {
		return new Docket(DocumentationType.SWAGGER_2).groupName("Packet manager").select()
				.apis(RequestHandlerSelectors.basePackage("io.mosip.commons.packetmanager.controller"))
				.paths(PathSelectors.ant("/*")).build();
	}
	
}
