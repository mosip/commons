package io.mosip.kernel.masterdata.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.kernel.masterdata.entity.IdentitySchema;

@Repository
public interface IdentitySchemaRepository extends BaseRepository<IdentitySchema, String>{
	
	/**
	 * Find identity schema by id
	 * @return
	 */
	@Query("FROM IdentitySchema WHERE (isDeleted is null OR isDeleted = false) and id = ?1")
	IdentitySchema findIdentitySchemaById(String id);

	/**
	 * Get latest published identity schema
	 * 
	 * @return IdentitySchema
	 */
	@Query("FROM IdentitySchema WHERE idVersion = (select max(b.idVersion) from IdentitySchema b where "
			+ "(b.isDeleted is null OR b.isDeleted = false) AND b.isActive = true And b.status='PUBLISHED')")
	IdentitySchema findLatestPublishedIdentitySchema();
	
	
	/**
	 * Get published identity schema based on idVersion
	 * 
	 * @param ver
	 * @return
	 */
	@Query("FROM IdentitySchema WHERE idVersion=?1 AND isActive = true AND status='PUBLISHED'")
	IdentitySchema findPublishedIdentitySchema(double ver);
	
	/**
	 * Get All Identity schema based on pagination
	 * 
	 * @param isActive
	 * @param pageable
	 * @return
	 */
	@Query(value="select * FROM master.identity_schema WHERE is_active=?1 AND (is_deleted is null OR is_deleted = false)", 
			countQuery="select count(id) FROM master.identity_schema WHERE is_active=?1 AND (is_deleted is null OR is_deleted = false)",
			nativeQuery= true)
	Page<IdentitySchema> findAllIdentitySchema(boolean isActive, Pageable pageable);
	
	/**
	 * update idAttributeJson in identity_schema table based on id
	 * update is allowed only on schema's in DRAFT status
	 * 
	 * @param id
	 * @param idAttributeJson
	 * @param isActive
	 * @param updatedDateTime
	 * @param updatedBy
	 * @return
	 */
	@Modifying
	@Query("UPDATE IdentitySchema i SET i.idAttributeJson=?2, i.isActive=?3 , i.updatedDateTime=?4, i.updatedBy=?5 WHERE i.id =?1 AND i.status='DRAFT' AND (i.isDeleted is null or i.isDeleted =false)")
	int updateIdentitySchema(String id, String idAttributeJson, boolean isActive, LocalDateTime updatedDateTime, String updatedBy);
	
	/**
	 * update identity_schema in DRAFT status to PUBLISHED status and also increment schema_version by 0.1
	 * 
	 * @param id
	 * @param schemaJson
	 * @param effectiveFrom
	 * @param updatedDateTime
	 * @param updatedBy
	 * @return
	 */
	@Modifying
	@Query("UPDATE IdentitySchema i SET i.idVersion=?6, i.schemaJson=?2, i.effectiveFrom=?3, i.status='PUBLISHED', i.isActive=true ,i.updatedDateTime=?4, i.updatedBy=?5 "
			+ "WHERE i.id =?1 AND i.status='DRAFT' AND (i.isDeleted is null OR i.isDeleted =false)")
	int publishIdentitySchema(String id, String schemaJson, LocalDateTime effectiveFrom, LocalDateTime updatedDateTime, String updatedBy, double idVersion);
	
	/**
	 * update only is_deleted flag of Identity_schema in DRAFT status
	 * 
	 * @param id
	 * @param deletedDateTime
	 * @param updatedBy
	 * @return
	 */
	@Modifying
	@Query("UPDATE IdentitySchema g SET g.updatedBy=?3 , g.isDeleted =true , g.deletedDateTime = ?2 WHERE g.id =?1 and (g.isDeleted is null or g.isDeleted =false)")
	int deleteIdentitySchema(String id, LocalDateTime deletedDateTime, String updatedBy);
	
}
