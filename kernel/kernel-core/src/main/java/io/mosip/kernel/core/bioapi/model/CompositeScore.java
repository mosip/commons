package io.mosip.kernel.core.bioapi.model;

import lombok.Data;

/**
 * Combined match score plus per-modality breakdown, retained for Bio API 0.7
 * compatibility.
 * <p>
 * Contract: used as a match-result payload. {@code individualScores} and
 * {@code analyticsInfo} may be null or empty. Score ranges are
 * provider-defined. Does not perform I/O.
 * </p>
 */
@Data
public class CompositeScore {

	/**
	 * Scaled composite score, typically 0–100; provider-defined.
	 */
	private float scaledScore;
	/**
	 * Provider-internal unscaled score; {@code 0} if unused.
	 */
	private long internalScore;
	/**
	 * Per-sample or per-modality scores; may be null or empty.
	 */
	private Score[] individualScores;
	/**
	 * Optional analytics key/value pairs from the matcher; may be null or empty.
	 */
	private KeyValuePair[] analyticsInfo;
	
}
