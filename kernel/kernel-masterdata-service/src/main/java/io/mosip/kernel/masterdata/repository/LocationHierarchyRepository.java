package io.mosip.kernel.masterdata.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.kernel.masterdata.entity.LocationHierarchy;

/**
 * 
 * @author Megha Tanga
 * @since 1.0.0
 *
 */
@Repository
public interface LocationHierarchyRepository extends BaseRepository<LocationHierarchy, String> {

	/**
	 * This method trigger query to fetch the Location Hierarchy for the given
	 * Location-Hierarchy-level and language code.
	 * 
	 * @param level
	 *            LocationHierarchy level provided by user
	 * @param langCode
	 *            language code provided by user
	 * @return List Location Hierarchy details fetched from database
	 */

	@Query("FROM LocationHierarchy l where l.hierarchyLevel = ?1 and l.langCode = ?2 and (l.isDeleted is null or l.isDeleted = false) and l.isActive = true")
	List<LocationHierarchy> findAllByLevelAndLangCodeAndIsDeletedFalseorIsDeletedIsNull(short hierarchyLevel,
			String langCode);

	/**
	 * This method trigger query to fetch the Location Hierarchy detail for the
	 * given language code.
	 * 
	 * @param langCode
	 *            langCode provided by user
	 * 
	 * @return List Location Hierarchy details fetched from database
	 */
	@Query("FROM LocationHierarchy l where l.langCode = ?1 and (l.isDeleted is null or l.isDeleted = false) and l.isActive = true")
	List<LocationHierarchy> findAllByLangCodeAndIsDeletedFalseOrIsDeletedIsNull(String langCode);
	
	@Query("FROM LocationHierarchy l where l.langCode = ?1 and hierarchyLevel = ?2 and hierarchyLevelName =?3 and (l.isDeleted is null or l.isDeleted = false) and l.isActive = true")
	LocationHierarchy findByLangCodeAndLevelAndName(String langCode,short hierarchyLevel,String hierarchyLevelName);

	@Query(value = "select hierarchy_level FROM master.loc_hierarchy_list where hierarchy_level_name = :heirarchyLevelName and lang_code=:languageCode and (is_deleted is null or is_deleted = false) and is_active = true", nativeQuery = true)
	Integer findByheirarchyLevalNameAndLangCode(@Param("heirarchyLevelName") String heirarchyLevelName,@Param("languageCode") String languageCode);
}
