package io.mosip.kernel.biosdk.provider.spi;

import java.util.List;
import java.util.Map;

import io.mosip.kernel.biometrics.constant.BiometricFunction;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.core.bioapi.exception.BiometricException;
import io.mosip.kernel.core.cbeffutil.entity.BIR;

public interface iBioProviderApi {
	
	/**
	 * loading of SDK based on the provided params
	 * and other initialization stuff
	 * @param params
	 */
	Map<BiometricType, List<BiometricFunction>> init(Map<BiometricType, Map<String, String>> params) throws BiometricException;
	
	/**
	 * 1:1 match
	 * 
	 * @param sample
	 * @param record
	 * @param modality
	 * @return
	 */
	boolean verify(List<BIR> sample, List<BIR> record, BiometricType modality, Map<String, String> flags);	
	
	/**
	 * 1:n match
	 * 
	 * @param sample
	 * @param gallery
	 * @param modality
	 * @return
	 */
	Map<String, Boolean> identify(List<BIR> sample, Map<String, List<BIR>> gallery, BiometricType modality, Map<String, String> flags);
	
	
	/**
	 * Score provided by SDK, later should be added in BIR "others" attribute
	 * @param sample
	 * @return
	 */
	float[] getSegmentQuality(BIR[] sample, Map<String, String> flags);
	
	/**
	 * 
	 * @param sample
	 * @return
	 */
	Map<BiometricType, Float> getModalityQuality(BIR[] sample, Map<String, String> flags);
	
	
	/**
	 * 
	 * @param sample
	 * @return
	 */
	List<BIR> extractTemplate(List<BIR> sample, Map<String, String> flags);

}
