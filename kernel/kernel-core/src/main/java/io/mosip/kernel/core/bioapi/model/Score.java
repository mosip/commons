package io.mosip.kernel.core.bioapi.model;

import lombok.Data;

/**
 * Single-modality match score retained for Bio API 0.7 compatibility.
 * <p>
 * Contract: used inside {@link CompositeScore#getIndividualScores()}. Score
 * ranges are provider-defined. {@code analyticsInfo} may be null. Does not
 * perform I/O.
 * </p>
 */
@Data
public class Score {
	
	/**
	 * Scaled score, typically 0–100; provider-defined.
	 */
	private float scaleScore;	  
	/**
	 * Provider-internal unscaled score; {@code 0} if unused.
	 */
	private long internalScore;
	/**
	 * Optional analytics breakdown; may be null or empty.
	 */
	private KeyValuePair[] analyticsInfo;

}
