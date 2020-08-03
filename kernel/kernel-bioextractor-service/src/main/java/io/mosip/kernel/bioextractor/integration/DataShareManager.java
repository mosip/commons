package io.mosip.kernel.bioextractor.integration;

import static io.mosip.kernel.bioextractor.config.constant.BiometricExtractionErrorConstants.DOWNLOAD_BIOMETRICS_ERROR;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.bioextractor.exception.BiometricExtractionException;

@Component
public class DataShareManager {
	
@Autowired
private RestTemplate restTemplate;
	
	public <R> R downloadObject(String url, Class<R> clazz) throws BiometricExtractionException {
		HttpHeaders headers = new HttpHeaders();
		HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
		try {
			ResponseEntity<R> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, clazz);
			return responseEntity.getBody();
		} catch (RestClientException e) {
			throw new BiometricExtractionException(DOWNLOAD_BIOMETRICS_ERROR, e);
		}
	}

}
