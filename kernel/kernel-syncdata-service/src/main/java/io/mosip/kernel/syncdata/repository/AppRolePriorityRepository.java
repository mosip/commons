package io.mosip.kernel.syncdata.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.syncdata.entity.AppRolePriority;
import io.mosip.kernel.syncdata.entity.id.IdAndLanguageCodeID;

/**
 * AppRolePriorityRepository.
 *
 * @author Srinivasan
 * @since 1.0.0
 */
@Repository
public interface AppRolePriorityRepository extends JpaRepository<AppRolePriority, IdAndLanguageCodeID> {

	/**
	 * Find by last updated and current time stamp.
	 *
	 * @param lastUpdatedTime  the last updated time
	 * @param currentTimeStamp the current time stamp
	 * @return {@link AppRolePriority}
	 */
	@Query("FROM AppRolePriority WHERE (createdDateTime BETWEEN ?1 AND ?2) OR (updatedDateTime BETWEEN ?1 AND ?2)  OR (deletedDateTime BETWEEN ?1 AND ?2)")
	List<AppRolePriority> findByLastUpdatedAndCurrentTimeStamp(LocalDateTime lastUpdatedTime,
			LocalDateTime currentTimeStamp);
}
