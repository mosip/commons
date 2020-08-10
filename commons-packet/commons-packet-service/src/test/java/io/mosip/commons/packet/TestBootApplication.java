package io.mosip.commons.packet;

import io.mosip.commons.packet.config.PacketManagerSecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = "io.mosip.commons.packet.*")
@Import(PacketManagerSecurityConfig.class)
public class TestBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestBootApplication.class, args);
	}
}
