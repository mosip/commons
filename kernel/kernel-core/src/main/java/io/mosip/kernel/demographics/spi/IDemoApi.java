package io.mosip.kernel.demographics.spi;

import java.util.Map;

/**
 * Demographic matching SPI used during identity authentication.
 * <p>
 * Implementations compare request and gallery demographic strings with exact,
 * partial, or phonetic strategies. Call {@link #init()} once before matching.
 * </p>
 *
 * @author Nagarjuna
 */
public interface IDemoApi {

	/**
	 * Loads matcher resources (dictionaries, phonetic engines, or caches).
	 */
	void init();
	
	/**
	 * Scores an exact string match between request and gallery values.
	 *
	 * @param reqInfo    demographic value from the authentication request
	 * @param entityInfo demographic value from the stored identity
	 * @param flags      matcher options (algorithm-specific; may be empty)
	 * @return match score as defined by the implementation
	 */
	int doExactMatch(String reqInfo, String entityInfo, Map<String, String> flags);
	
	/**
	 * Scores a partial (substring / token) match between request and gallery values.
	 *
	 * @param reqInfo    demographic value from the authentication request
	 * @param entityInfo demographic value from the stored identity
	 * @param flags      matcher options (algorithm-specific; may be empty)
	 * @return match score as defined by the implementation
	 */
	int doPartialMatch(String reqInfo, String entityInfo, Map<String, String> flags);
	
	/**
	 * Scores a phonetic match between request and gallery values for {@code language}.
	 *
	 * @param reqInfo    demographic value from the authentication request
	 * @param entityInfo demographic value from the stored identity
	 * @param language   language code used to select the phonetic encoder
	 * @param flags      matcher options (algorithm-specific; may be empty)
	 * @return match score as defined by the implementation
	 */
	int doPhoneticsMatch(String reqInfo, String entityInfo, String language, Map<String, String> flags);
}
