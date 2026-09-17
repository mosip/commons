package io.mosip.kernel.config.server.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Config-server operator API that triggers Spring Cloud {@code /actuator/refresh} on
 * discovered MOSIP services so they reload configuration without a restart.
 * <p>
 * Services listed in {@code mosip.config.dnd.services} and this config-server
 * application itself are skipped (do-not-disturb).
 * </p>
 *
 * @author Swati Raj
 * @since 1.0.0
 */
@RestController
public class RefreshController {

    private static final Logger logger = LoggerFactory.getLogger(RefreshController.class);

    /**
     * Service registry used to list application names and instances.
     */
    @Autowired
    private DiscoveryClient discoveryClient;

    /**
     * This config-server's {@code spring.application.name}; never refreshed by this API.
     */
    @Value("${spring.application.name}")
    private String applicationName;

    /**
     * Service IDs that must not receive refresh ({@code mosip.config.dnd.services}).
     */
    @Value("#{${mosip.config.dnd.services}}")
    private List<String> dndServices;

    /**
     * Per-instance refresh URL pattern: {@code {instanceUri}/actuator/refresh}.
     */
    private String URL_TEMPLATE = "%s/actuator/refresh";

    /**
     * HTTP client used to POST {@code /actuator/refresh}. Overridable in unit tests.
     */
    private RestTemplate restTemplate = new RestTemplate();

    /**
     * Refreshes matching discovered services.
     * <p>
     * An empty {@code servicename} refreshes every registered service except DND and
     * this application. A non-empty value matches service IDs that contain or equal
     * the parameter.
     * </p>
     *
     * @param serviceName substring or exact service id to refresh; blank means all eligible services
     * @return map of refresh URL to HTTP status string; empty if discovery is unavailable
     */
    @GetMapping("/refresh")
    public Map<String, String> refreshContext(@RequestParam("servicename") String serviceName) {
        logger.info("refreshContext invoked with serviceName : {}", serviceName.replaceAll("[\n\r]", "_"));
        Map<String, String> result = new HashMap<>();

        if(Objects.nonNull(discoveryClient)) {
            try {
                List<String> serviceIds = serviceName.isBlank() ? discoveryClient.getServices() :
                        discoveryClient.getServices().stream()
                                .filter(s -> s.contains(serviceName)).collect(Collectors.toList());

                logger.info("shortlisted serviceIds : {}", serviceIds);

                serviceIds.stream()
                        .dropWhile(s -> isDNDService(s) )
                        .forEach(s -> this.invokeRefreshActuatorEndpoint(s,discoveryClient.getInstances(s),result));

            } catch (Throwable t) {
                logger.error("Failed to refresh contexts", t);
            }
        }
        logger.info("refreshContext completed");
        return result;
    }

    /**
     * Whether {@code serviceId} is this config-server or listed in {@code mosip.config.dnd.services}.
     *
     * @param serviceId discovered application name
     * @return {@code true} if refresh must be skipped
     */
    private boolean isDNDService(String serviceId) {
        if(serviceId.equals(applicationName) || (dndServices!=null && dndServices.contains(serviceId))) {
            logger.info("DND service found, ignoring refresh attempt! serviceId : {} ", serviceId.replaceAll("[\n\r]", "_"));
            return true;
        }
        return false;
    }

    /**
     * POSTs {@code /actuator/refresh} on every instance of {@code serviceId} and records statuses.
     *
     * @param serviceId discovered application name
     * @param instances instances of that service; ignored when {@code null}
     * @param result    mutable map filled with URL to HTTP status
     */
    private void invokeRefreshActuatorEndpoint(String serviceId, List<ServiceInstance> instances, Map<String, String> result) {
        if(Objects.nonNull(instances)) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> httpEntity = new HttpEntity<String>(null, headers);

            for (ServiceInstance instance : instances) {
                logger.info("Refresh actuator invoked on serviceId: {} and instance : {} ", serviceId.replaceAll("[\n\r]", "_"), instance.getUri());
                String url = String.format(URL_TEMPLATE, instance.getUri().toString());
                ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
                result.put(url, resp.getStatusCode().toString());
                logger.info("{} response : {}", url.replaceAll("[\n\r]", "_"), resp);
            }
        }
    }
}
