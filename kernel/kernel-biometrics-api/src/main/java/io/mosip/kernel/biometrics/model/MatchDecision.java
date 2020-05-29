package io.mosip.kernel.biometrics.model;

import java.util.Map;

import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.Decision;
import lombok.Data;

/**
 * The Class Score.
 * 
 * @author Manoj SP
 */
@Data
public class MatchDecision {
	
	/**
	 * refers to match position of input gallery
	 */
	private int galleryIndex;
	/** 
	 * match decision per modality, based on input modalitiesToMatch
	 */
	private Map<BiometricType, Decision> decisions;
	
	/** The analytics info - detailed breakdown and other information. */
	private Map<String, String> analyticsInfo;
	
}

