package io.mosip.kernel.masterdata.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.kernel.masterdata.entity.TemplateType;

/**
 * 
 * @author Uday Kumar
 * @author Megha Tanga
 * @since 1.0.0
 *
 */
@Repository
public interface TemplateTypeRepository extends BaseRepository<TemplateType, String> {

	/**
	 * This method trigger query to fetch the TemplateType detail for the given
	 * TemplateType id and language code.
	 * 
	 * 
	 * @param code
	 *            TemplateType code provided by user
	 * @param langCode
	 *            language code provided by user
	 * @return List TemplateType fetched from database
	 */

	@Query("FROM TemplateType t where t.code = ?1 and t.langCode = ?2 and (t.isDeleted is null or t.isDeleted = false) and t.isActive = true")
	List<TemplateType> findAllByCodeAndLangCodeAndIsDeletedFalseorIsDeletedIsNull(String code, String langCode);

	/**
	 * This method trigger query to fetch the TemplateType detail for the given
	 * language code.
	 * 
	 * @param langCode
	 *            langCode provided by user
	 * 
	 * @return List TemplateType fetched from database
	 */
	@Query("FROM TemplateType t where t.langCode = ?1 and (t.isDeleted is null or t.isDeleted = false) and t.isActive = true")
	List<TemplateType> findAllByLangCodeAndIsDeletedFalseOrIsDeletedIsNull(String langCode);

}
