package io.mosip.kernel.biometrics.spi;

import java.util.List;
import java.util.Map;

import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.model.SDKInfo;
import io.mosip.kernel.biometrics.model.MatchDecision;
import io.mosip.kernel.biometrics.model.QualityCheck;
import io.mosip.kernel.biometrics.model.Response;


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
	 * @param flags
	 * @return
	 */
	Response<MatchDecision[]> match(BiometricRecord sample, BiometricRecord[] gallery, List<BiometricType> modalitiesToMatch, Map<String, String> flags);

	/**
	 * Extract template
	 * Need to sign the extracted template
	 *  
	 * @param sample
	 * @param flags
	 * @return
	 */
	Response<BiometricRecord> extractTemplate(BiometricRecord sample, Map<String, String> flags);

	/**
	 * It segment the single biometric image into multiple biometric images. Eg:
	 * Split the thumb slab into multiple fingers
	 *
	 * @param sample the sample
	 * @param flags  the flags
	 * @return the response
	 */
	Response<BiometricRecord> segment(BIR sample, Map<String, String> flags);
	
	/**
	 * Converts the provided BDBData from source format to target format for all segments
	 * 
	 * @param sample
	 * @param sourceFormat
	 * @param targetFormat
	 * @param sourceParams
	 * @param targetParams
	 * @return
	 */
	BiometricRecord convertFormat(BiometricRecord sample, String sourceFormat, String targetFormat, Map<String, String> sourceParams, 
			Map<String, String> targetParams);
	
	
}
