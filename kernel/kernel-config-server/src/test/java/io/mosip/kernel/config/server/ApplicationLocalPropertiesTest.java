package io.mosip.kernel.config.server;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.jupiter.api.Test;

/**
 * Ensures local-run classpath properties are packaged for {@code run-local} scripts.
 */
class ApplicationLocalPropertiesTest {

	@Test
	void applicationLocalPropertiesPresentOnClasspath() throws IOException {
		try (InputStream in = getClass().getClassLoader().getResourceAsStream("application-local.properties")) {
			assertTrue(in != null, "application-local.properties must be on the test classpath");
			Properties props = new Properties();
			props.load(in);
			assertTrue(props.containsKey("server.port"));
			assertTrue(props.containsKey("spring.cloud.config.server.native.search-locations"));
		}
	}
}
