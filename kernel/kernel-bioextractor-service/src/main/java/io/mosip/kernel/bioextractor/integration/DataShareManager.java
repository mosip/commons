package io.mosip.kernel.bioextractor.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.kernel.bioextractor.config.constant.BiometricExtractionErrorConstants;
import io.mosip.kernel.bioextractor.exception.BiometricExtractionException;
import io.mosip.kernel.bioextractor.service.helper.RestHelper;

@Component
public class DataShareManager {
	
	@Autowired
	private RestHelper restHelper;
	
	
	public <R> R downloadObject(String url, Class<R> clazz, BiometricExtractionErrorConstants errConst) throws BiometricExtractionException {
		return restHelper.doGet(url, clazz, null, errConst);
	}

}
