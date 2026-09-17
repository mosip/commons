package io.mosip.kernel.core.bioapi.model;

import lombok.Data;

/**
 * Quality-check result for a single biometric sample.
 * <p>
 * Contract: returned by
 * {@link io.mosip.kernel.core.bioapi.spi.IBioApi#checkQuality}. {@code score}
 * is a 0–100 percentage. {@code analyticsInfo} may be null. Does not perform
 * I/O.
 * </p>
 *
 * @author Sanjay Murali
 */
@Data
public class QualityScore {
	
	/**
	 * Quality as a percentage in the range 0–100.
	 */
	private float score; 
	
	/**
	 * Provider-internal unscaled score retained for Bio API 0.7; {@code 0} if
	 * unused.
	 */
	private long internalScore;
	
	/**
	 * Optional analytics breakdown from the quality checker; may be null or
	 * empty.
	 */
	private KeyValuePair[] analyticsInfo;
}
