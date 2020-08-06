package io.mosip.kernel.bioextractor.integration;

import static io.mosip.kernel.bioextractor.constant.BioExtractorConfigKeyConstants.DATA_SHARE_SERVICE_CREATE_SHARE_URL;
import static io.mosip.kernel.bioextractor.constant.BioExtractorConfigKeyConstants.DATA_SHARE_SERVICE_POLICY_ID;
import static io.mosip.kernel.bioextractor.constant.BioExtractorConfigKeyConstants.DATA_SHARE_SERVICE_SUBSCRIBER_ID;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import io.mosip.kernel.bioextractor.constant.BiometricExtractionErrorConstants;
import io.mosip.kernel.bioextractor.exception.BiometricExtractionException;
import io.mosip.kernel.bioextractor.service.helper.RestHelper;
@Component
public class DataShareManager {
	
	@Autowired
	private RestHelper restHelper;
	
	@Value("${" + DATA_SHARE_SERVICE_CREATE_SHARE_URL + "}")
	private String createShareUrl;
	
	@Value("${" + DATA_SHARE_SERVICE_POLICY_ID + "}")
	private String policyId;
	
	@Value("${" + DATA_SHARE_SERVICE_SUBSCRIBER_ID + "}")
	private String subscriberId;
	
	
	public <R> R downloadObject(String url, Class<R> clazz, BiometricExtractionErrorConstants errConst) throws BiometricExtractionException {
		return restHelper.doGet(url, clazz, null, errConst);
	}

	public String uploadBytes(byte [] bytes, BiometricExtractionErrorConstants errConst) throws BiometricExtractionException {
		Map<String, String> pathParams = new LinkedHashMap<>();
		pathParams.put("", policyId);
		
		MultiValueMap<String, Object> body
		  = new LinkedMultiValueMap<>();
		body.add("file", new ByteArrayResource(bytes));

		Map<String, Object> response = restHelper.doPost(createShareUrl, pathParams, MediaType.MULTIPART_FORM_DATA, body, null, errConst, Map.class);
		
		if(response.get("errors") instanceof List || ((List)response.get("errors")).isEmpty()) {
			throw new BiometricExtractionException(errConst);
		} else {
			Map<String, Object> dataShare = (Map<String, Object>)response.get("dataShare");
			return (String) dataShare.get("url");
		}
		
	}
}
