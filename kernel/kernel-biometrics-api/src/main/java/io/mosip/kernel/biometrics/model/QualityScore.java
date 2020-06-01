package io.mosip.kernel.biometrics.model;

import java.util.List;

import lombok.Data;

/**
 * The Class QualityScore.
 * 
 * @author Sanjay Murali
 */
@Data
public class QualityScore {
	
	/** The score - 0 - 100 score that represents quality as a percentage */
	private float score; 
	
	private List<String> errors;
}