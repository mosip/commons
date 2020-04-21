package io.mosip.kernel.masterdata.service;

import io.mosip.kernel.masterdata.dto.TemplateTypeDto;
import io.mosip.kernel.masterdata.dto.TemplateTypeResponseDto;
import io.mosip.kernel.masterdata.entity.id.CodeAndLanguageCodeID;
import io.mosip.kernel.masterdata.exception.DataNotFoundException;
import io.mosip.kernel.masterdata.exception.MasterDataServiceException;

/**
 * 
 * @author Uday Kumar
 * @author Megha Tanga
 * @since 1.0.0
 *
 */

public interface TemplateTypeService {

	/**
	 * Method to create template type based on provided
	 * 
	 * @param tempalteType
	 *            dto with Template Type .
	 * @return {@linkplain CodeAndLanguageCodeID}
	 */
	public CodeAndLanguageCodeID createTemplateType(TemplateTypeDto tempalteType);

	/**
	 * This abstract method to fetch TemplateType details for given TemplateType
	 * code and language code
	 * 
	 * @param templateTypeCode
	 *            TemplateType given by user
	 * @param langCode
	 *            Language code given by user
	 * @return TemplateTypeResponseDto TemplateType Detail for given TemplateType
	 *         code and language code
	 * @throws MasterDataServiceException
	 *             if any error occurs while retrieving TemplateType Details
	 * @throws DataNotFoundException
	 *             if no TemplateType found
	 *
	 */

	public TemplateTypeResponseDto getTemplateTypeCodeandLangCode(String templateTypeCode, String langCode);

	/**
	 * This abstract method to fetch TemplateType details for given language code
	 * 
	 * @param langCode
	 *            Language code given by user
	 * @return TemplateTypeResponseDto TemplateType Detail for given
	 *         language code
	 * @throws MasterDataServiceException
	 *             if any error occurs while retrieving TemplateType Details
	 * @throws DataNotFoundException
	 *             if no TemplateType found
	 *
	 */
	public TemplateTypeResponseDto getTemplateTypeLangCode(String langCode);
}
