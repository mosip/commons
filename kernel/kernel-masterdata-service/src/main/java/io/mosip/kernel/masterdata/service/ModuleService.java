package io.mosip.kernel.masterdata.service;

import io.mosip.kernel.masterdata.dto.ModuleResponseDto;
import io.mosip.kernel.masterdata.exception.DataNotFoundException;
import io.mosip.kernel.masterdata.exception.MasterDataServiceException;

/**
 * 
 * @author Megha Tanga
 * @since 1.0.0
 *
 */

public interface ModuleService {

	/**
	 * This abstract method to fetch Module details for given Module id and language
	 * code
	 * 
	 * @param id
	 *            Module id given by user
	 * @param langCode
	 *            Language code given by user
	 * @return ModuleResponseDto Module Detail for given Module id and language code
	 * @throws MasterDataServiceException
	 *             if any error occurs while retrieving Module Details
	 * @throws DataNotFoundException
	 *             if no Module found
	 *
	 */

	public ModuleResponseDto getModuleIdandLangCode(String id, String langCode);

	/**
	 * This abstract method to fetch Module details for given language code
	 * 
	 * @param langCode
	 *            Language code given by user
	 * @return ModuleResponseDto Module Detail for given language code
	 * @throws MasterDataServiceException
	 *             if any error occurs while retrieving Module Details
	 * @throws DataNotFoundException
	 *             if no Module found
	 *
	 */
	public ModuleResponseDto getModuleLangCode(String langCode);
}
