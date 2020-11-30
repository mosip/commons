package io.mosip.kernel.syncdata.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.syncdata.entity.DeviceProvider;

/**
 * The Interface DeviceProviderRepository.
 * 
 * @author Srinivasan
 * @since 1.0.0
 */
@Repository
public interface DeviceProviderRepository extends JpaRepository<DeviceProvider, String> {

	/**
	 * Find all latest created update deleted.
	 *
	 * @param lastUpdated      the last updated
	 * @param currentTimeStamp the current time stamp
	 * @return the list
	 */
	@Query("FROM DeviceProvider WHERE (createdDateTime BETWEEN ?1 AND ?2) OR (updatedDateTime BETWEEN ?1 AND ?2)  OR (deletedDateTime BETWEEN ?1 AND ?2)")
	List<DeviceProvider> findAllLatestCreatedUpdateDeleted(LocalDateTime lastUpdated, LocalDateTime currentTimeStamp);
}
