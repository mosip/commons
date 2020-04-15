package io.mosip.kernel.masterdata.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.kernel.masterdata.entity.Machine;
import io.mosip.kernel.masterdata.entity.TemplateFileFormat;

/**
 * @author Neha Sinha
 * @author Megha Tanga
 * @since 1.0.0
 * 
 */
@Repository
public interface TemplateFileFormatRepository extends BaseRepository<TemplateFileFormat, String> {

	/**
	 * Get TemplateFileFormat by specific id and language code
	 * 
	 * @param code     for TemplateFileFormat
	 * @param langCode is the language code present in database
	 * @return object of {@link TemplateFileFormat}
	 */
	@Query("FROM TemplateFileFormat WHERE code =?1 AND langCode =?2 AND (isDeleted is null OR isDeleted = false) and isActive = true")
	TemplateFileFormat findByCodeAndLangCodeAndIsDeletedFalseOrIsDeletedIsNull(String code, String langCode);

	/**
	 * Delete TemplateFileFormat based on code provided.
	 * 
	 * @param updatedBy       name of user
	 * @param deletedDateTime the Date and time of deletion.
	 * @param code            the document category code.
	 * @return the integer.
	 */
	@Modifying
	@Transactional
	@Query("UPDATE TemplateFileFormat t SET t.updatedBy = ?1, t.isDeleted = true , t.deletedDateTime = ?2 WHERE t.code =?3 and (t.isDeleted is null or t.isDeleted = false)")
	int deleteTemplateFileFormat(String updatedBy, LocalDateTime deletedDateTime, String code);
	
	
	
	@Query("FROM TemplateFileFormat t where t.code = ?1 and t.langCode = ?2 and (t.isDeleted is null or t.isDeleted = false) and t.isActive = true")
	List<TemplateFileFormat> findAllByCodeAndLangCodeAndIsDeletedFalseorIsDeletedIsNull(String code, String langCode);
	
	@Query("FROM TemplateFileFormat t where  t.langCode = ?1 and (t.isDeleted is null or t.isDeleted = false) and t.isActive = true")
	List<TemplateFileFormat> findAllByLangCodeAndIsDeletedFalseorIsDeletedIsNull(String langCode);
}
