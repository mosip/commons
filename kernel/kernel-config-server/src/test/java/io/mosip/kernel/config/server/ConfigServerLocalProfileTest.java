package io.mosip.kernel.config.server;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

/**
 * Boots config-server with {@code local,native} (classpath config, no Git).
 */
@SpringBootTest(classes = ConfigServerBootApplication.class)
@ActiveProfiles({ "local", "native" })
class ConfigServerLocalProfileTest {

	@Autowired
	private Environment environment;

	@Test
	void contextLoads() {
		assertNotNull(environment);
	}

	@Test
	void localProfileDefinesPortAndNativeSearch() {
		assertNotNull(environment.getProperty("server.port"));
		assertNotNull(environment.getProperty("spring.cloud.config.server.native.search-locations"));
	}
}
