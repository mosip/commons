package io.mosip.kernel.biometrics.spi;

import java.util.List;
import java.util.Map;

import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.model.MatchDecision;
import io.mosip.kernel.biometrics.model.QualityCheck;
import io.mosip.kernel.biometrics.model.Response;
import io.mosip.kernel.biometrics.model.SDKInfo;


/**
 * The Interface IBioApi.
 * 
 * @author Sanjay Murali
 * @author Manoj SP
 * 
 */
public interface IBioApi {
	
	/**
	 * All the initialization process, for eg: license initialization
	 * Possible values in initparams
	 * environment
	 * 
	 * @param initParams
	 */
	SDKInfo init(Map<String, String> initParams);

	/**
	 * It checks the quality of the provided biometric image and render the
	 * respective quality score per modality.
	 * 
	 * if modalitiesToCheck is null/empty, score is provided for each modality found in the gallery record.
	 * 
	 * @param sample
	 * @param modalitiesToCheck
	 * @param flags
	 * @return
	 */
	Response<QualityCheck> checkQuality(BiometricRecord sample, List<BiometricType> modalitiesToCheck, Map<String, String> flags);

	/**
	 * It compares the biometrics and provide the respective matching decision per modality for each 
	 * item in gallery based on modalitiesToMatch argument.
	 * 
	 * if modalitiesToMatch is null/empty, match is provided for each modality found in the gallery record.
	 * 
	 * pass transactionID in flags - to log during this method call
	 * 
	 * @param sample
	 * @param gallery
	 * @param modalitiesToMatch
	 * @param flags
	 * @return
	 */
	Response<MatchDecision[]> match(BiometricRecord sample, BiometricRecord[] gallery, List<BiometricType> modalitiesToMatch, Map<String, String> flags);

	/**
	 * Extract template
	 * Need to sign the extracted template
	 * if modalitiesToExtract is null/empty, template is extracted for each modality found in the sample.
	 * 
	 * @param sample
	 * @param modalitiesToExtract
	 * @param flags
	 * @return
	 */
	Response<BiometricRecord> extractTemplate(BiometricRecord sample, List<BiometricType> modalitiesToExtract, Map<String, String> flags);

	/**
	 * It segment the single biometric image into multiple biometric images. Eg:
	 * Split the thumb slab into multiple fingers
	 * if modalitiesToSegment is null/empty, each modality found in the sample is segmented.
	 * 
	 * @param sample
	 * @param modalitiesToSegment
	 * @param flags
	 * @return
	 */
	Response<BiometricRecord> segment(BiometricRecord sample, List<BiometricType> modalitiesToSegment, Map<String, String> flags);
	
	/**
	 * Converts the provided BDBData from source format to target format for all segments
	 * if modalitiesToConvert is null/empty, each modality found in the sample is converted to target format(sample must be in sourceFormat).
	 * 
	 * @param sample
	 * @param sourceFormat
	 * @param targetFormat
	 * @param sourceParams
	 * @param targetParams
	 * @param modalitiesToConvert
	 * @return
	 */
	Response<BiometricRecord> convertFormat(BiometricRecord sample, String sourceFormat, String targetFormat, Map<String, String> sourceParams, 
			Map<String, String> targetParams, List<BiometricType> modalitiesToConvert);
		
}
