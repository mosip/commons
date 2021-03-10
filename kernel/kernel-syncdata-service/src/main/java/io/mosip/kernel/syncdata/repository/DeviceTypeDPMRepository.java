package io.mosip.kernel.syncdata.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.syncdata.entity.DeviceTypeDPM;

/**
 * 
 * @author Srinivasan
 * @since 1.0.0
 *
 */
@Repository
public interface DeviceTypeDPMRepository extends JpaRepository<DeviceTypeDPM, String> {

	@Query("FROM DeviceTypeDPM WHERE (createdDateTime BETWEEN ?1 AND ?2) OR (updatedDateTime BETWEEN ?1 AND ?2)  OR (deletedDateTime BETWEEN ?1 AND ?2)")
	List<DeviceTypeDPM> findAllLatestCreatedUpdateDeleted(LocalDateTime lastUpdated, LocalDateTime currentTimeStamp);
}
