package io.mosip.kernel.websub.api.config.publisher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RestTemplateHelper {

	@Value("${mosip.auth.filter_disable:true}")
	boolean isAuthFilterDisable;

	@Qualifier("websubRestTemplate")
	@Autowired(required = false)
	private RestTemplate websubRestTemplate;

	@Autowired(required = false)
	@Qualifier("selfTokenRestTemplate") 
	private RestTemplate selfTokenRestTemplate;

	public RestTemplate getRestTemplate() {
		return isAuthFilterDisable?websubRestTemplate:selfTokenRestTemplate;
	}
}