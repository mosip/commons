package io.mosip.kernel.config.server.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class RefreshController {

    private static final Logger logger = LoggerFactory.getLogger(RefreshController.class);

    @Autowired
    private DiscoveryClient discoveryClient;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("#{${mosip.config.dnd.services}}")
    private List<String> dndServices;

    private String URL_TEMPLATE = "%s/actuator/refresh";
    private RestTemplate restTemplate;

    @GetMapping("/refresh")
    public Map<String, String> refreshContext(@RequestParam("servicename") String serviceName) {
        logger.info("refreshContext invoked with (sanitized) serviceName : {}", serviceName.replaceAll("[\n\r\t]", "_"));
        Map<String, String> result = new HashMap<>();

        if(Objects.nonNull(discoveryClient)) {
            try {
                List<String> serviceIds = serviceName.isBlank() ? discoveryClient.getServices() :
                        discoveryClient.getServices().stream()
                                .filter(s -> s.contains(serviceName) || s.equals(serviceName)).collect(Collectors.toList());

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

    private boolean isDNDService(String serviceId) {
        if(serviceId.equals(applicationName) || (dndServices!=null && dndServices.contains(serviceId))) {
            logger.info("DND service found, ignoring refresh attempt! serviceId : {} ", serviceId);
            return true;
        }
        return false;
    }

    private void invokeRefreshActuatorEndpoint(String serviceId, List<ServiceInstance> instances, Map<String, String> result) {
        if(Objects.nonNull(instances)) {
            restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> httpEntity = new HttpEntity<String>(null, headers);

            for (ServiceInstance instance : instances) {
                logger.info("Refresh actuator invoked on serviceId: {} and instance : {} ", serviceId, instance.getUri());
                String url = String.format(URL_TEMPLATE, instance.getUri().toString());
                ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
                result.put(url, resp.getStatusCode().toString());
                logger.info("{} response : {}", url, resp);
            }
        }
    }
}
