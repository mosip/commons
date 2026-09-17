package io.mosip.kernel.core.bioapi.model;

import lombok.Data;

/**
 * Matcher decision for a single gallery candidate against a probe sample.
 * <p>
 * Contract: returned as an element of
 * {@link io.mosip.kernel.core.bioapi.spi.IBioApi#match}. {@code match} is the
 * boolean decision; {@code analyticsInfo} may be null. Does not perform I/O.
 * </p>
 *
 * @author Manoj SP
 */
@Data
public class MatchDecision {
	
	/**
	 * Whether the matcher considers the probe and this gallery entry a match.
	 */
	private boolean match;
	
	/**
	 * Optional analytics breakdown from the matcher; may be null or empty.
	 */
	private KeyValuePair[] analyticsInfo;
}
