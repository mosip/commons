package io.mosip.kernel.demographics.spi;

import java.util.List;
import java.util.Map;

public interface IDemoNormalizer {

	/**
	 * Normalize the name attribute value(s) in demograghic authentication.
	 *
	 * @param nameInfo the name info
	 * @param language the language
	 * @param titleFetcher the title fetcher
	 * @return the string
	 * @throws IdAuthenticationBusinessException the id authentication business exception
	 */
	public String normalizeName(String nameInfo, String language, Map<String, List<String>> fetchTitles)			;
	
	
	/**
	 * Normalize the name attribute value(s) in demograghic authentication.
	 *
	 * @param address the address
	 * @param language the language
	 * @return the string
	 */
	public String normalizeAddress(String address, String language);
}
