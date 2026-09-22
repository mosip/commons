package io.mosip.kernel.demographics.spi;

import java.util.List;
import java.util.Map;

/**
 * Normalizes demographic name and address strings before matching.
 * <p>
 * Implementations strip titles, punctuation, and language-specific variants so
 * {@link IDemoApi} matchers compare canonical forms.
 * </p>
 */
public interface IDemoNormalizer {

	/**
	 * Normalizes a name for demographic authentication.
	 * <p>
	 * Titles listed in {@code fetchTitles} for {@code language} are removed
	 * before remaining tokens are canonicalized.
	 * </p>
	 *
	 * @param nameInfo     raw name value
	 * @param language     language code of the name
	 * @param fetchTitles  map of language to honorific titles to strip
	 * @return normalized name
	 */
	public String normalizeName(String nameInfo, String language, Map<String, List<String>> fetchTitles)			;
	
	
	/**
	 * Normalizes an address for demographic authentication.
	 *
	 * @param address  raw address value
	 * @param language language code of the address
	 * @return normalized address
	 */
	public String normalizeAddress(String address, String language);
}
