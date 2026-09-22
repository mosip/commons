package io.mosip.kernel.core.bioapi.spi;

import io.mosip.kernel.core.bioapi.model.KeyValuePair;
import io.mosip.kernel.core.bioapi.model.MatchDecision;
import io.mosip.kernel.core.bioapi.model.QualityScore;
import io.mosip.kernel.core.bioapi.model.Response;
import io.mosip.kernel.core.cbeffutil.entity.BIR;



/**
 * SPI for quality check, match, template extraction, and segmentation of CBEFF
 * biometric records.
 * <p>
 * Contract: implementations typically call an ABIS or local SDK (HTTP or JNI).
 * {@code sample} must be non-null with a populated BDB. {@code flags} may be
 * null or empty. Gallery arrays must be non-null; empty gallery yields empty
 * match results. Call from IDA or registration after CBEFF parsing.
 * </p>
 *
 * @author Sanjay Murali
 * @author Manoj SP
 * @see io.mosip.kernel.core.cbeffutil.entity.BIR
 */
public interface IBioApi {

	/**
	 * Scores the quality of a single biometric sample.
	 * <p>
	 * Contract: does not mutate {@code sample}. Implementations may perform
	 * HTTP or native SDK I/O.
	 * </p>
	 *
	 * @param sample never-null CBEFF BIR containing the biometric image or
	 *               template
	 * @param flags  optional provider-specific options; may be null or empty
	 * @return never-null response whose body is a {@link QualityScore}; HTTP-like
	 *         {@code statusCode} indicates success or failure
	 */
	Response<QualityScore> checkQuality(BIR sample, KeyValuePair[] flags);

	/**
	 * Compares a probe sample against a gallery of BIRs and returns match
	 * decisions.
	 * <p>
	 * Contract: gallery order is preserved in the result array. Implementations
	 * may perform HTTP or native SDK I/O.
	 * </p>
	 *
	 * @param sample  never-null probe BIR
	 * @param gallery never-null array of candidate BIRs; may be empty
	 * @param flags   optional provider-specific options; may be null or empty
	 * @return never-null response whose body is one {@link MatchDecision} per
	 *         gallery entry
	 */
	Response<MatchDecision[]> match(BIR sample, BIR[] gallery, KeyValuePair[] flags);

	/**
	 * Extracts a biometric template from a sample image BIR.
	 * <p>
	 * Contract: the returned BIR holds the template BDB. Implementations may
	 * perform HTTP or native SDK I/O.
	 * </p>
	 *
	 * @param sample never-null image BIR
	 * @param flags  optional provider-specific options; may be null or empty
	 * @return never-null response whose body is the template {@link BIR}
	 */
	Response<BIR> extractTemplate(BIR sample, KeyValuePair[] flags);

	/**
	 * Segments a composite biometric image into multiple BIRs (for example a
	 * slap into individual fingers).
	 * <p>
	 * Contract: implementations may perform HTTP or native SDK I/O.
	 * </p>
	 *
	 * @param sample never-null composite image BIR
	 * @param flags  optional provider-specific options; may be null or empty
	 * @return never-null response whose body is the segment {@link BIR} array
	 */
	Response<BIR[]> segment(BIR sample, KeyValuePair[] flags);
}
