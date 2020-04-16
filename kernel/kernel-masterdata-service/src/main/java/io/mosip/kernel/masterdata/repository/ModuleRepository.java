package io.mosip.kernel.masterdata.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.kernel.masterdata.entity.ModuleDetail;

/**
 * 
 * @author Megha Tanga
 * @since 1.0.0
 *
 */
@Repository
public interface ModuleRepository extends BaseRepository<ModuleDetail, String> {

	/**
	 * This method trigger query to fetch the Module detail for the given Module id
	 * and language code.
	 * 
	 * @param id
	 *            Module id provided by user
	 * @param langCode
	 *            language code provided by user
	 * @return List Module fetched from database
	 */

	@Query("FROM ModuleDetail t where t.id = ?1 and t.langCode = ?2 and (t.isDeleted is null or t.isDeleted = false) and t.isActive = true")
	List<ModuleDetail> findAllByIdAndLangCodeAndIsDeletedFalseorIsDeletedIsNull(String id, String langCode);

	/**
	 * This method trigger query to fetch the Module detail for the given language
	 * code.
	 * 
	 * @param langCode
	 *            langCode provided by user
	 * 
	 * @return List Module fetched from database
	 */
	@Query("FROM ModuleDetail t where t.langCode = ?1 and (t.isDeleted is null or t.isDeleted = false) and t.isActive = true")
	List<ModuleDetail> findAllByLangCodeAndIsDeletedFalseOrIsDeletedIsNull(String langCode);

}
