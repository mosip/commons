package io.mosip.kernel.biometrics.model;

import java.util.List;
import java.util.Map;

import io.mosip.kernel.biometrics.constant.BiometricFunction;
import io.mosip.kernel.biometrics.constant.BiometricType;
import lombok.Data;

@Data
public class SDKInfo {
	
	private String apiVersion;
	private String sdkVersion;
	private List<BiometricType> supportedModalities;
	private Map<BiometricFunction, List<BiometricType>> supportedMethods;
	
	/**
	 * eg: license expire details
	 */
	private Map<String, String> otherInfo;
	
}
