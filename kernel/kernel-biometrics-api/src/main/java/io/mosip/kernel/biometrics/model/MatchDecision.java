package io.mosip.kernel.biometrics.model;

import java.util.HashMap;
import java.util.Map;

import io.mosip.kernel.biometrics.constant.BiometricType;
import lombok.Data;


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
	
	public MatchDecision(int galleryIndex) {
		this.analyticsInfo = new HashMap<String, String>();
		this.decisions = new HashMap<BiometricType, Decision>();
		this.galleryIndex = galleryIndex; 
	}
	
}

