package io.mosip.kernel.auth.config.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.kernel.auth.config.OpenApiProperties;
import io.mosip.kernel.auth.config.OpenApiProperties.InfoProperty;
import io.mosip.kernel.auth.config.OpenApiProperties.LicenseProperty;
import io.mosip.kernel.auth.config.OpenApiProperties.Server;
import io.mosip.kernel.auth.config.OpenApiProperties.Service;
import io.mosip.kernel.auth.config.SwaggerConfig;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Swagger UI Authorize button is an OpenAPI header apiKey named Authorization.
 */
public class SwaggerConfigTest {

	@Test
	public void openApiRegistersAuthorizationHeaderApiKey() {
		SwaggerConfig config = new SwaggerConfig();
		ReflectionTestUtils.setField(config, "openApiProperties", sampleProperties());

		OpenAPI api = config.openApi();
		SecurityScheme scheme = api.getComponents().getSecuritySchemes().get(SwaggerConfig.AUTHORIZATION_SCHEME);

		assertNotNull(scheme);
		assertEquals(SecurityScheme.Type.APIKEY, scheme.getType());
		assertEquals(SecurityScheme.In.HEADER, scheme.getIn());
		assertEquals(SwaggerConfig.AUTHORIZATION_SCHEME, scheme.getName());
		assertFalse(api.getSecurity().isEmpty());
		assertEquals(SwaggerConfig.AUTHORIZATION_SCHEME, api.getSecurity().get(0).keySet().iterator().next());
	}

	private static OpenApiProperties sampleProperties() {
		LicenseProperty license = new LicenseProperty();
		license.setName("Mosip");
		license.setUrl("https://docs.mosip.io/platform/license");
		InfoProperty info = new InfoProperty();
		info.setTitle("Auth Manager Service");
		info.setDescription("Rest Endpoints for operations related to auth");
		info.setVersion("1.0");
		info.setLicense(license);
		Server server = new Server();
		server.setUrl("http://localhost:8091/v1/authmanager");
		server.setDescription("Auth Manager Service");
		Service service = new Service();
		service.setServers(List.of(server));
		OpenApiProperties properties = new OpenApiProperties();
		properties.setInfo(info);
		properties.setService(service);
		return properties;
	}
}
