package io.mosip.kernel.biosdk.provider.spi;

import java.util.List;
import java.util.Map;

import io.mosip.kernel.biometrics.constant.BiometricFunction;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.core.bioapi.exception.BiometricException;
import io.mosip.kernel.logger.logback.util.MetricTag;


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
	boolean verify(List<BIR> sample, List<BIR> record,
				   @MetricTag(value = "modality", extractor = "arg.value") BiometricType modality,
				   Map<String, String> flags);
	
	/**
	 * 1:n match
	 * 
	 * @param sample
	 * @param gallery
	 * @param modality
	 * @return
	 */
	Map<String, Boolean> identify(List<BIR> sample, Map<String, List<BIR>> gallery,
								  @MetricTag(value = "modality", extractor = "arg.value") BiometricType modality,
								  Map<String, String> flags);
	
	
	/**
	 * Score provided by SDK, later should be added in BIR "others" attribute
	 * @param sample
	 * @return
	 */
	float[] getSegmentQuality(@MetricTag(value = "modality",
			extractor = "int size = arg.length; String[] names = new String[size];for(int i=0;i<size;i++){ names[i] = " +
					"arg[i].bdbInfo.getSubtype().toString().replaceAll('\\\\[|\\\\]|,','');}" +
					"return java.util.Arrays.toString(names);") BIR[] sample,
							  Map<String, String> flags);
	
	/**
	 * 
	 * @param sample
	 * @return
	 */
	Map<BiometricType, Float> getModalityQuality(@MetricTag(value = "modality",
			extractor = "int size = arg.length; String[] names = new String[size];for(int i=0;i<size;i++){ names[i] = " +
					"arg[i].bdbInfo.getSubtype().toString().replaceAll('\\\\[|\\\\]|,','');}" +
					"return java.util.Arrays.toString(names);") BIR[] sample, Map<String, String> flags);
	
	
	/**
	 * 
	 * @param sample
	 * @return
	 */
	List<BIR> extractTemplate(@MetricTag(value = "modality",
			extractor = "int size = arg.size(); String[] names = new String[size];for(int i=0;i<size;i++){ names[i] = " +
					"arg.get(i).bdbInfo.getSubtype().toString().replaceAll('\\\\[|\\\\]|,','');}" +
					"return java.util.Arrays.toString(names);") List<BIR> sample, Map<String, String> flags);

}
