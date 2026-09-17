package io.mosip.kernel.config.server.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

/**
 * Full branch coverage for {@link RefreshController}.
 */
@ExtendWith(MockitoExtension.class)
class RefreshControllerTest {

	@Mock
	private DiscoveryClient discoveryClient;

	@Mock
	private RestTemplate restTemplate;

	private RefreshController controller;

	@BeforeEach
	void setUp() {
		controller = new RefreshController();
		ReflectionTestUtils.setField(controller, "discoveryClient", discoveryClient);
		ReflectionTestUtils.setField(controller, "applicationName", "kernel-config-service");
		ReflectionTestUtils.setField(controller, "dndServices", List.of("consul", "kernel-config-service"));
		ReflectionTestUtils.setField(controller, "restTemplate", restTemplate);
	}

	@Test
	void refreshWithNullDiscoveryReturnsEmptyMap() {
		ReflectionTestUtils.setField(controller, "discoveryClient", null);
		assertTrue(controller.refreshContext("").isEmpty());
	}

	@Test
	void refreshSkipsLeadingDndServicesWhenNoInstances() {
		when(discoveryClient.getServices())
				.thenReturn(List.of("kernel-config-service", "consul", "idgenerator"));
		when(discoveryClient.getInstances("idgenerator")).thenReturn(Collections.emptyList());

		assertTrue(controller.refreshContext("").isEmpty());
	}

	@Test
	void refreshFiltersByServiceNameSubstring() {
		when(discoveryClient.getServices()).thenReturn(List.of("kernel-idgenerator", "notifier"));
		when(discoveryClient.getInstances("kernel-idgenerator")).thenReturn(Collections.emptyList());

		assertTrue(controller.refreshContext("idgenerator").isEmpty());
	}

	@Test
	void refreshFiltersByExactServiceName() {
		when(discoveryClient.getServices()).thenReturn(List.of("notifier", "idgenerator"));
		when(discoveryClient.getInstances("notifier")).thenReturn(Collections.emptyList());

		assertTrue(controller.refreshContext("notifier").isEmpty());
	}

	@Test
	void refreshWithBlankServiceNameUsesAllServices() {
		when(discoveryClient.getServices()).thenReturn(List.of("notifier"));
		when(discoveryClient.getInstances("notifier")).thenReturn(Collections.emptyList());

		assertEquals(0, controller.refreshContext("   ").size());
	}

	@Test
	void refreshIgnoresNullInstanceList() {
		when(discoveryClient.getServices()).thenReturn(List.of("notifier"));
		when(discoveryClient.getInstances("notifier")).thenReturn(null);

		assertTrue(controller.refreshContext("notifier").isEmpty());
	}

	@Test
	void refreshWhenDndListNullStillAllowsNonSelfService() {
		ReflectionTestUtils.setField(controller, "dndServices", null);
		when(discoveryClient.getServices()).thenReturn(List.of("notifier"));
		when(discoveryClient.getInstances("notifier")).thenReturn(Collections.emptyList());

		assertTrue(controller.refreshContext("").isEmpty());
	}

	@Test
	void refreshSanitizesNewlinesInServiceName() {
		when(discoveryClient.getServices()).thenReturn(List.of("notifier"));

		assertTrue(controller.refreshContext("notifier\n").isEmpty());
	}

	@Test
	void refreshCatchesDiscoveryFailures() {
		when(discoveryClient.getServices()).thenThrow(new RuntimeException("discovery down"));

		assertTrue(controller.refreshContext("").isEmpty());
	}

	@Test
	void refreshPostsToActuatorAndRecordsStatus() {
		ServiceInstance instance = new DefaultServiceInstance("notifier-1", "notifier", "127.0.0.1", 8083, false);
		when(discoveryClient.getServices()).thenReturn(List.of("notifier"));
		when(discoveryClient.getInstances("notifier")).thenReturn(List.of(instance));
		when(restTemplate.exchange(eq("http://127.0.0.1:8083/actuator/refresh"), eq(HttpMethod.POST), any(),
				eq(String.class))).thenReturn(new ResponseEntity<>("[]", HttpStatus.OK));

		Map<String, String> result = controller.refreshContext("notifier");

		assertEquals(1, result.size());
		assertEquals("200 OK", result.get("http://127.0.0.1:8083/actuator/refresh"));
	}

	@Test
	void refreshPostsToMultipleInstances() {
		ServiceInstance a = new DefaultServiceInstance("n-1", "notifier", "127.0.0.1", 8083, false);
		ServiceInstance b = new DefaultServiceInstance("n-2", "notifier", "127.0.0.1", 8084, false);
		when(discoveryClient.getServices()).thenReturn(List.of("notifier"));
		when(discoveryClient.getInstances("notifier")).thenReturn(List.of(a, b));
		when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
				.thenReturn(ResponseEntity.ok("[]"));

		Map<String, String> result = controller.refreshContext("");

		assertEquals(2, result.size());
		assertTrue(result.containsKey("http://127.0.0.1:8083/actuator/refresh"));
		assertTrue(result.containsKey("http://127.0.0.1:8084/actuator/refresh"));
	}

	@Test
	void refreshDoesNotTreatUnrelatedServiceAsMatch() {
		when(discoveryClient.getServices()).thenReturn(List.of("notifier", "audit"));

		Map<String, String> result = controller.refreshContext("idgenerator");

		assertTrue(result.isEmpty());
	}

	@Test
	void refreshSkipsSelfApplicationEvenWhenDndListNull() {
		ReflectionTestUtils.setField(controller, "dndServices", null);
		when(discoveryClient.getServices()).thenReturn(List.of("kernel-config-service", "notifier"));
		when(discoveryClient.getInstances("notifier")).thenReturn(Collections.emptyList());

		assertTrue(controller.refreshContext("").isEmpty());
	}

	@Test
	void refreshSanitizesNewlinesInDndServiceId() {
		ReflectionTestUtils.setField(controller, "dndServices", List.of("consul\r"));
		when(discoveryClient.getServices()).thenReturn(List.of("consul\r", "notifier"));
		when(discoveryClient.getInstances("notifier")).thenReturn(Collections.emptyList());

		assertTrue(controller.refreshContext("").isEmpty());
	}
}
