package io.mosip.kernel.core.bioapi.model;

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
	
	/** Added for backward compatibility 0.7 */
	private long internalScore;
	
	/** The analytics info - detailed breakdown and other information */
	private KeyValuePair[] analyticsInfo;
}