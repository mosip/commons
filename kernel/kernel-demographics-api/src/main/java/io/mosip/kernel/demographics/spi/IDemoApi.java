package io.mosip.kernel.demographics.spi;

import java.util.Map;

/**
 * The interface of IDemoApi
 * 
 * @author Nagarjuna
 *
 */
public interface IDemoApi {

	/**
	 * 
	 */
	void init();
	
	/**
	 *  fro exact match
	 * @param reqInfo
	 * @param entityInfo
	 * @param flags
	 * @return
	 */
	int doExactMatch(String reqInfo, String entityInfo, Map<String, String> flags);
	
	/**
	 *  for partila match
	 * @param reqInfo
	 * @param entityInfo
	 * @param flags
	 * @return
	 */
	int doPartialMatch(String reqInfo, String entityInfo, Map<String, String> flags);
	
	/**
	 * for phonetics match
	 * @param reqInfo
	 * @param entityInfo
	 * @param language
	 * @param flags
	 * @return
	 */
	int doPhoneticsMatch(String reqInfo, String entityInfo, String language, Map<String, String> flags);
}
