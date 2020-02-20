package io.mosip.kernel.masterdata.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.kernel.masterdata.entity.Location;
import io.mosip.kernel.masterdata.entity.id.CodeAndLanguageCodeID;

/**
 * This interface is JPA repository class which interacts with database and does
 * the CRUD function. It is extended from {@link BaseRepository}
 * 
 * @author Srinivasan
 * @author uday kumar
 * @author Sidhant Agarwal
 *
 */
@Repository
public interface LocationRepository extends BaseRepository<Location, CodeAndLanguageCodeID> {

	@Query("FROM Location WHERE (isDeleted is null OR isDeleted = false) AND isActive = true")
	List<Location> findLocationHierarchyByIsDeletedIsNullOrIsDeletedFalse();

	@Query(value = "FROM Location l where l.code=?1 and l.langCode=?2 and (l.isDeleted is null or l.isDeleted=false) and l.isActive = true")
	List<Location> findLocationHierarchyByCodeAndLanguageCode(String locCode, String languagecode);

	@Query(value = "FROM Location l where l.code=?1 and l.langCode=?2 and (l.isDeleted is null or l.isDeleted=false)")
	Location findLocationByCodeAndLanguageCode(String locCode, String languagecode);

	@Query(value = "FROM Location l where parentLocCode=?1 and langCode=?2 and (l.isDeleted is null or l.isDeleted=false) and l.isActive=true")
	List<Location> findLocationHierarchyByParentLocCodeAndLanguageCode(String parentLocCode, String languageCode);

	@Query(value = "select distinct hierarchy_level, hierarchy_level_name, is_active from master.location where lang_code=?1 and (is_deleted='f' or is_deleted is null) and is_active='t' ", nativeQuery = true)
	List<Object[]> findDistinctLocationHierarchyByIsDeletedFalse(String langCode);

	@Query(value = "FROM Location l where l.code=?1 and (l.isDeleted is null or l.isDeleted=false) and l.isActive = true")
	List<Location> findByCode(String locationCode);

	/**
	 *
	 * @param hierarchyName - hierarchy name
	 * @return List
	 */
	@Query(value = "FROM Location l where LOWER(l.hierarchyName)=LOWER(?1) AND (l.isDeleted is null OR l.isDeleted=false) AND l.isActive=true")
	List<Location> findAllByHierarchyNameIgnoreCase(String hierarchyName);

	/**
	 *
	 * @param langCode language code
	 * @param level    hierarchy level
	 * @return List of Locations
	 * 
	 */
	@Query(value = "FROM Location l where l.langCode=?1 and l.hierarchyLevel >=?2 and (l.isDeleted is null or l.isDeleted=false) and l.isActive = true")
	List<Location> getAllLocationsByLangCodeAndLevel(String langCode, Short level);

	/**
	 * checks whether the location name is valid location or not
	 * 
	 * @param locationName location name
	 * @return {@link Boolean} true or false
	 */
	@Query(value = "SELECT EXISTS(select name FROM master.location where (LOWER(name)=LOWER(?1)) and (is_active=true) and (is_deleted is null or is_deleted =false))", nativeQuery = true)
	boolean isLocationNamePresent(String locationName);

	/**
	 * give list of the immediate Locations for the given parent location code
	 * 
	 * @param locationName location name
	 * @return {@link Boolean} true or false
	 */
	@Query("FROM Location l where l.parentLocCode=?1 and l.langCode=?2 and l.isActive=true and (l.isDeleted is null or l.isDeleted=false)")
	List<Location> findDistinctByparentLocCode(String parentLocCode, String langCode);

	/**
	 * give list of the immediate Locations for the given parent location code
	 * 
	 * @param locationName location name
	 * @return {@link Boolean} true or false
	 */
	@Query("SELECT distinct l.code FROM Location l where l.parentLocCode=?1 GROUP BY l.code")
	List<String> findDistinctByparentLocCode(String parentLocCode);

	@Query(value = "select distinct name from master.location where hierarchy_level = ?1 and lang_code = ?2", nativeQuery = true)
	List<String> filterByDistinctHierarchyLevel(int hierarchyLevel, String langCode);

	@Query(value = "select name from master.location where hierarchy_level = ?1 and lang_code = ?2", nativeQuery = true)
	List<String> filterByHierarchyLevel(int hierarchyLevel, String langCode);

	@Query(value = "FROM Location WHERE langCode=?1 AND (isDeleted is null or isDeleted=false)")
	List<Location> findAllByLangCodeNonDeleted(String langCode);

	@Query("FROM Location l where l.hierarchyLevel=?1 and l.name =?2 and l.langCode=?3 and l.isActive=?4 and (l.isDeleted is null or l.isDeleted=false)")
	Location findLocationByHierarchyLevel(short hierachyLevel, String value, String langCode, boolean isActive);

	@Query("FROM Location l where l.langCode=?1 and l.isActive=true and (l.isDeleted is null or l.isDeleted=false)")
	List<Location> findAllByLangCode(String langCode);

	@Query("FROM Location l where l.langCode=?1 and l.isActive=?2 and (l.isDeleted is null or l.isDeleted=false)")
	List<Location> findAllByLangCode(String langCode, boolean isActive);

	@Query("FROM Location l where  l.hierarchyLevel=?1 and lower(l.name) like ?2 and l.langCode=?3 and l.isActive=?4 and (l.isDeleted is null or l.isDeleted=false)")
	List<Location> findLocationByHierarchyLevelContains(short hierarchyLevel, String value, String langCode,
			boolean isActive);

	@Query("FROM Location l where  l.hierarchyLevel=?1 and lower(l.name) like ?2 and l.langCode=?3 and l.isActive=?4 and (l.isDeleted is null or l.isDeleted=false)")
	List<Location> findLocationByHierarchyLevelStartsWith(Short hierarchyLevel, String value, String langCode,
			boolean isActive);

	@Query("SELECT l.hierarchyName FROM Location l where l.isActive=true and (l.isDeleted is null or l.isDeleted=false)")
	List<String> findLocationAllHierarchyNames();

	@Query("FROM Location l where l.hierarchyName=?1 and l.langCode=?2 and l.isActive=true and (l.isDeleted is null or l.isDeleted=false)")
	List<Location> findAllHierarchyNameAndNameValueForEmptyTextFilter(String hierarchyName, String langCode);

	@Query("FROM Location l where l.hierarchyName=?1 and lower(l.name) like ?2 and l.langCode=?3 and l.isActive=true and (l.isDeleted is null or l.isDeleted=false)")
	List<Location> findAllHierarchyNameAndNameValueForTextFilter(String hierarchyName, String text, String langCode);

	@Query("SELECT DISTINCT l.name FROM Location l where l.hierarchyName=?1 and l.langCode=?2 and l.isActive=true and (l.isDeleted is null or l.isDeleted=false)")
	List<String> findDistinctHierarchyNameAndNameValueForEmptyTextFilter(String hierarchyName, String langCode);

	@Query("SELECT DISTINCT l.name FROM Location l where l.hierarchyName=?1 and lower(l.name) like ?2 and l.langCode=?3 and l.isActive=true and (l.isDeleted is null or l.isDeleted=false)")
	List<String> findDistinctHierarchyNameAndNameValueForTextFilter(String hierarchyName, String text, String langCode);

	@Query(value = "FROM Location l where (l.isDeleted is null or l.isDeleted=false)")
	List<Location> findAllNonDeleted();

	@Query("FROM Location l WHERE l.langCode=?1 and (l.isDeleted is null OR l.isDeleted = false) AND l.isActive = true")
	List<Location> findByLangCode(String langCode);

	@Query("FROM Location l WHERE l.name=?1 AND l.hierarchyLevel=?2 ")
	List<Location> findByNameAndLevel(String name, Short hierarchyLevel);

	@Query("FROM Location l WHERE l.name=?1 AND l.hierarchyLevel=?2 AND l.langCode=?3")
	List<Location> findByNameAndLevelLangCode(String name, Short hierarchyLevel, String langCode);

	/**
	 * give list of the immediate Locations for the given parent location code
	 * 
	 * @param locationCode   location code
	 * @param hierarchyLevel hierarchy level
	 * @return Set<Location>
	 */
	@Query("FROM Location l WHERE l.langCode=?1 AND l.hierarchyLevel=?2")
	Set<Location> findLocationByLangCodeLevel(String langCode, Short hierarchyLevel);

}
