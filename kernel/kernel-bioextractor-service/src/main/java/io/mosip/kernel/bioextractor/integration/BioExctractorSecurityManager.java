package io.mosip.kernel.bioextractor.integration;
import static io.mosip.kernel.bioextractor.config.constant.BioExtractorConfigKeyConstants.KERNEL_DECRYPT_URL;
import static io.mosip.kernel.bioextractor.config.constant.BiometricExtractionErrorConstants.DECRYPTION_ERROR;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.auth.adapter.model.AuthUserDetails;
import io.mosip.kernel.bioextractor.exception.BiometricExtractionException;
import io.mosip.kernel.bioextractor.service.helper.RestHelper;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.DateUtils;

@Component
public class BioExctractorSecurityManager {
	
	private static final String TIME_STAMP = "timeStamp";

	private static final String REFERENCE_ID = "referenceId";

	private static final String APPLICATION_ID = "applicationId";

	private static final String DATA = "data";

	@Autowired
	private RestHelper restHelper;
	
	@Value("${" + KERNEL_DECRYPT_URL + "}")
	private String decryptUrl;
	
	@SuppressWarnings("unchecked")
	public byte [] decrypt(String encryptedData, String appId, String refId, Map<String, String> headersMap) throws BiometricExtractionException {
		RequestWrapper<Map<String, Object>> requestWrapper = restHelper.createRequtestWrapper();
		Map<String, Object> request = new HashMap<>();
		request.put(DATA, encryptedData);
		request.put(APPLICATION_ID, appId);
		request.put(REFERENCE_ID, refId);
		request.put(TIME_STAMP, DateUtils.getUTCCurrentDateTime());
		requestWrapper.setRequest(request);
		ResponseWrapper<Map<String, Object>>  response = restHelper.doPost(decryptUrl, requestWrapper, headersMap, DECRYPTION_ERROR, ResponseWrapper.class);
		return ((String)(response.getResponse()).get(DATA)).getBytes();
		
	}


}
