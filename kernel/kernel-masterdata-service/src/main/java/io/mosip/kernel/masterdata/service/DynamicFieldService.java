package io.mosip.kernel.masterdata.service;

import io.mosip.kernel.masterdata.dto.DynamicFieldDto;
import io.mosip.kernel.masterdata.dto.DynamicFieldValueDto;
import io.mosip.kernel.masterdata.dto.getresponse.DynamicFieldResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.PageDto;

/**
 * Methods to create / update / inactivate / addValues dynamic field
 * 
 * @author anusha
 *
 */
public interface DynamicFieldService {
	
	/**
	 * Fetch all dynamic fields
	 * @param pageNumber
	 * @param pageSize
	 * @param sortBy
	 * @param orderBy
	 * @param langCode
	 * @return
	 */
	public PageDto<DynamicFieldResponseDto> getAllDynamicField(int pageNumber, int pageSize, String sortBy, String orderBy, String langCode);
	
	/**
	 * create dynamic field
	 * @param dto
	 * @return
	 */
	public DynamicFieldResponseDto createDynamicField(DynamicFieldDto dto);
	
	/**
	 * update dynamic field
	 * @param dto
	 * @return
	 */
	public DynamicFieldResponseDto updateDynamicField(String id, DynamicFieldDto dto);
	
	
	/**
	 * Add / updates field value based on the fieldName and langCode
	 * @param fieldId
	 * @param dto
	 * @return
	 */
	public String updateFieldValue(String fieldId, DynamicFieldValueDto dto);
	
}
