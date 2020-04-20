package io.mosip.kernel.masterdata.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.kernel.masterdata.entity.DynamicField;


/**
 * 
 * @author anusha
 *
 */
@Repository
public interface DynamicFieldRepository extends BaseRepository<DynamicField, String> {

	/**
	 * Get All dynamic fields based on pagination
	 * 
	 * @param langCode
	 * @param pageable
	 * @return
	 */
	@Query(value="SELECT * FROM master.dynamic_field WHERE (is_deleted is null OR is_deleted = false) and lang_code=?1",
			countQuery="SELECT COUNT(id) FROM master.dynamic_field WHERE (is_deleted is null OR is_deleted = false) and lang_code=?1",
			nativeQuery = true)
	Page<DynamicField> findAllDynamicFieldsByLangCode(String langCode, Pageable pageable);
	
	/**
	 * Get All dynamic fields based on pagination
	 * @param pageable
	 * @return
	 */
	@Query(value="SELECT * FROM master.dynamic_field WHERE (is_deleted is null OR is_deleted = false)", 
			countQuery="SELECT COUNT(id) FROM master.dynamic_field WHERE (is_deleted is null OR is_deleted = false)",
			nativeQuery= true)
	Page<DynamicField> findAllDynamicFields(Pageable pageable);
	
	/**
	 * Get dynamic field based on id
	 * @param id
	 * @return
	 */
	@Query("FROM DynamicField WHERE (isDeleted is null OR isDeleted = false) and id=?1")
	DynamicField findDynamicFieldById(String id);
	
	/**
	 *  Get dynamic field based on id and langCode
	 * @param id
	 * @param langCode
	 * @return
	 */
	@Query("FROM DynamicField WHERE (isDeleted is null OR isDeleted = false) and id=?1 and langCode=?2")
	DynamicField findDynamicFieldByIdAndLangCode(String id, String langCode);
	
	/**
	 * 
	 * @param fieldName
	 * @return
	 */
	@Query("FROM DynamicField WHERE lower(name)=lower(?1)")
	List<DynamicField> findAllDynamicFieldByName(String fieldName);
	
	/**
	 * 
	 * @param fieldName
	 * @param langCode
	 * @return
	 */
	@Query("FROM DynamicField WHERE lower(name)=lower(?1) and langCode=?2")
	DynamicField findDynamicFieldByNameAndLangCode(String fieldName, String langCode);
	
	/**
	 *  Update all the fields of dynamic field except name
	 *  
	 * @param id
	 * @param description
	 * @param langCode
	 * @param dataType
	 * @param isActive
	 * @param updatedDateTime
	 * @param updatedBy
	 * @return
	 */
	@Modifying
	@Query("UPDATE DynamicField SET description=?2, langCode=?3, dataType=?4, isActive=?5 , updatedDateTime=?6, updatedBy=?7"
			+ " WHERE (isDeleted is null OR isDeleted = false) and id=?1")
	int updateDynamicField(String id, String description, String langCode, String dataType, boolean isActive, 
			LocalDateTime updatedDateTime, String updatedBy);
	
	/**
	 * Update dynamic field value specific to a language code
	 * 
	 * @param id
	 * @param valueJson
	 * @param langCode
	 * @param updatedDateTime
	 * @param updatedBy
	 * @return
	 */
	@Modifying
	@Query("UPDATE DynamicField SET valueJson=?2, updatedDateTime=?4, updatedBy=?5"
			+ " WHERE (isDeleted is null OR isDeleted = false) and id=?1 and langCode=?3")
	int updateDynamicFieldValue(String id, String valueJson, String langCode, LocalDateTime updatedDateTime, String updatedBy);
	
	/**
	 * update isDeleted as true
	 * @param id
	 * @param updatedDateTime
	 * @param updatedBy
	 * @return
	 */
	@Modifying
	@Query("UPDATE DynamicField SET isDeleted=true, updatedDateTime=?2, updatedBy=?3 WHERE (isDeleted is null OR isDeleted = false) and id=?1")
	int deleteDynamicField(String id, LocalDateTime updatedDateTime, String updatedBy);
}
