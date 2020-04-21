package io.mosip.kernel.masterdata.service;

import io.mosip.kernel.masterdata.dto.IdSchemaPublishDto;
import io.mosip.kernel.masterdata.dto.IdentitySchemaDto;
import io.mosip.kernel.masterdata.dto.getresponse.IdSchemaResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.PageDto;

/**
 * Methods to create/ update / publish schema
 *  
 * @author anusha
 *
 */

public interface IdentitySchemaService {
	
	static final String STATUS_DRAFT = "DRAFT";
	static final String STATUS_PUBLISHED = "PUBLISHED";
	
	/**
	 * Fetch active and latest schema_versioned identity schema
	 * @return
	 */
	public IdSchemaResponseDto getLatestSchema();
	
	/**
	 * Fetch active identity schema based on version
	 * @return
	 */
	public IdSchemaResponseDto getIdentitySchema(double idVersion);
	
	/**
	 * Fetches all active schema's both in DRAFT and PUBLISHED status
	 * @return
	 */
	public PageDto<IdSchemaResponseDto> getAllSchema(int pageNumber, int pageSize, String sortBy, String orderBy);
	
	/**
	 * Create new schema in DRAFT status
	 * @param dto
	 * @return
	 */
	public IdSchemaResponseDto createSchema(IdentitySchemaDto dto);
	
	/**
	 * update schema and its status
	 * @param dto
	 * @return
	 */
	public IdSchemaResponseDto updateSchema(String id, IdentitySchemaDto dto);
	
	/**
	 * update identity_schema in DRAFT status to PUBLISHED status and also increment schema_version by 0.1
	 * 
	 * @param dto
	 * @return
	 */
	public String publishSchema(IdSchemaPublishDto dto);
	
	/**
	 * update only is_deleted flag of Identity_schema in DRAFT status
	 * it is not allowed to delete PUBLISHED schema
	 * @param id
	 * @return
	 */
	public String deleteSchema(String id);
	
}
