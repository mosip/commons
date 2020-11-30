package io.mosip.kernel.syncdata.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.syncdata.entity.ScreenAuthorization;
import io.mosip.kernel.syncdata.entity.id.ScreenAuthorizationID;

/**
 * The Interface ScreenAuthorizationRepository.
 *
 * @author Srinivasan
 * @since 1.0.0
 */
@Repository
public interface ScreenAuthorizationRepository extends JpaRepository<ScreenAuthorization, ScreenAuthorizationID> {

	/**
	 * Find by last updated and current time stamp.
	 *
	 * @param lastUpdateTimeStamp the last update time stamp
	 * @param currentTimeStamp    the current time stamp
	 * @return {@link ScreenAuthorization} list of ScreenAuthorization
	 */
	@Query("FROM ScreenAuthorization WHERE (createdDateTime BETWEEN ?1 AND ?2) OR (updatedDateTime BETWEEN ?1 AND ?2)  OR (deletedDateTime BETWEEN ?1 AND ?2) ")
	List<ScreenAuthorization> findByLastUpdatedAndCurrentTimeStamp(LocalDateTime lastUpdateTimeStamp,
			LocalDateTime currentTimeStamp);
}
