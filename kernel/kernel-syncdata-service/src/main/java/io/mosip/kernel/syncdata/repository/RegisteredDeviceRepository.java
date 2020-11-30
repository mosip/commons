package io.mosip.kernel.syncdata.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.syncdata.entity.RegisteredDevice;

/**
 * The Interface RegisteredDeviceRepository.
 * 
 * @author Srinivasan
 */
@Repository
public interface RegisteredDeviceRepository extends JpaRepository<RegisteredDevice, String> {

	/**
	 * Find by code and is active is true.
	 *
	 * @param deviceCode the device code
	 * @return the registered device
	 */
	RegisteredDevice findByCodeAndIsActiveIsTrue(String deviceCode);

	/**
	 * Find all latest created update deleted.
	 *
	 * @param lastUpdated      the last updated
	 * @param currentTimeStamp the current time stamp
	 * @return List of {@link RegisteredDevice}
	 */
	@Query(value = "Select * from master.registered_device_master where code IN(select id from master.device_master where regcntr_id=?1) and (cr_dtimes BETWEEN ?2 AND ?3) OR (upd_dtimes BETWEEN ?2 AND ?3)  OR (del_dtimes BETWEEN ?2 AND ?3)", nativeQuery = true)
	List<RegisteredDevice> findAllLatestCreatedUpdateDeleted(String regId, LocalDateTime lastUpdated,
			LocalDateTime currentTimeStamp);
}
