package io.mosip.kernel.masterdata.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.kernel.masterdata.entity.MOSIPDeviceServiceHistory;

/**
 * Repository for MOSIP Device Service.
 *
 * @author Megha Tanga
 * @author Srinivasan
 * @since 1.0.0
 */

@Repository
public interface MOSIPDeviceServiceHistoryRepository extends BaseRepository<MOSIPDeviceServiceHistory, String> {

	/**
	 * Find by id and is active is true.
	 *
	 * @param swVersion   the sw version
	 * @param effiveTimes the effive times
	 * @return {@link MOSIPDeviceServiceHistory}
	 */
	@Query(value = "(select * from mosip_device_service_h dsh where sw_version = ?1 and eff_dtimes<= ?2 and (is_deleted is null or is_deleted =false) and is_active=true ORDER BY eff_dtimes DESC) LIMIT 1", nativeQuery = true)
	List<MOSIPDeviceServiceHistory> findByIdAndIsActiveIsTrueAndByEffectiveTimes(String swVersion,
			LocalDateTime effiveTimes);

	/**
	 * Find by id and D provider id.
	 *
	 * @param id               the id
	 * @param deviceProviderId the device provider id
	 * @param effTimes         the eff times
	 * @return {@link MOSIPDeviceServiceHistory}
	 */
	@Query(value = "(select * from mosip_device_service_h dsh where id = ?1 and dprovider_id=?2 and eff_dtimes<= ?3 and (is_deleted is null or is_deleted =false) ORDER BY eff_dtimes DESC) LIMIT 1", nativeQuery = true)
	MOSIPDeviceServiceHistory findByIdAndDProviderId(String id, String deviceProviderId, LocalDateTime effTimes);

	/**
	 * Find by device detail history.
	 *
	 * @param version         the version
	 * @param deviceTypeCode  the device type code
	 * @param devicesTypeCode the devices type code
	 * @param make            the make
	 * @param model           the model
	 * @param dp              the dp
	 * @param effTimes        the eff times
	 * @return {@link MOSIPDeviceServiceHistory}
	 */
	@Query(value = "(select * from mosip_device_service_h where sw_version=?1 and dtype_code=?2 and dstype_code=?3 and make=?4 and model=?5 and dprovider_id=?6 and eff_dtimes<=?7 and (is_deleted is null or is_deleted =false) and is_active=true ORDER BY eff_dtimes DESC) LIMIT 1", nativeQuery = true)
	MOSIPDeviceServiceHistory findByDeviceDetailHistory(String version, String deviceTypeCode, String devicesTypeCode,
			String make, String model, String dp, LocalDateTime effTimes);
}
