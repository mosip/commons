package io.mosip.kernel.masterdata.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.kernel.masterdata.entity.Device;
import io.mosip.kernel.masterdata.entity.Machine;

/**
 * Repository function to fetching device details
 * 
 * @author Megha Tanga
 * @author Sidhant Agarwal
 * @author Ravi Kant
 * @since 1.0.0
 *
 */

@Repository
public interface DeviceRepository extends BaseRepository<Device, String> {

	/**
	 * This method trigger query to fetch the Device detail for the given id.
	 * 
	 * @param id the id of device
	 * @return the device detail
	 */
	@Query("FROM Device d where d.id = ?1 AND (d.isDeleted is null or d.isDeleted = false) AND d.isActive = true")
	List<Device> findByIdAndIsDeletedFalseOrIsDeletedIsNull(String id);

	/**
	 * This method trigger query to fetch the Device detail for the given language
	 * code.
	 * 
	 * 
	 * @param langCode language code provided by user
	 * 
	 * @return List Device Details fetched from database
	 */

	@Query("FROM Device d where d.langCode = ?1 and (d.isDeleted is null or d.isDeleted = false) AND d.isActive = true")
	List<Device> findByLangCodeAndIsDeletedFalseOrIsDeletedIsNull(String langCode);

	/**
	 * This method trigger query to fetch the Device detail for the given language
	 * code and Device Type code.
	 * 
	 * 
	 * @param langCode       language code provided by user
	 * @param deviceTypeCode device Type Code provided by user
	 * @return List Device Details fetched from database
	 * 
	 */
	@Query(value = "select d.id, d.name, d.mac_address, d.serial_num, d.ip_address, d.dspec_id, d.lang_code, d.is_active, d.validity_end_dtimes, d.zone_code, s.dtyp_code from master.device_master  d, master.device_spec s where  d.dspec_id = s.id  and d.lang_code = s.lang_code and d.lang_code = ?1 and s.dtyp_code = ?2 and (d.is_deleted is null or d.is_deleted = false) and d.is_active = true", nativeQuery = true)
	List<Object[]> findByLangCodeAndDtypeCode(String langCode, String deviceTypeCode);

	/**
	 * This method trigger query to fetch the Machine detail for the given id code.
	 * 
	 * @param deviceSpecId machineSpecId provided by user
	 * 
	 * @return MachineDetail fetched from database
	 */

	@Query("FROM Device d where d.deviceSpecId = ?1 and (d.isDeleted is null or d.isDeleted = false) and d.isActive = true")
	List<Device> findDeviceByDeviceSpecIdAndIsDeletedFalseorIsDeletedIsNull(String deviceSpecId);

	/**
	 * This method trigger query to fetch the Device detail for the given id and
	 * language code.
	 * 
	 * @param id       the id of device
	 * @param langCode language code from user
	 * @return the device detail
	 */
	@Query("FROM Device d where d.id = ?1 and d.langCode = ?2 AND (d.isDeleted is null or d.isDeleted = false) and d.isActive = true")
	Device findByIdAndLangCodeAndIsDeletedFalseOrIsDeletedIsNull(String id, String langCode);

	/**
	 * This method trigger query to fetch the Device detail those are mapped with
	 * the given regCenterId
	 * 
	 * @param regCenterId regCenterId provided by user
	 * @return Device fetched all device those are mapped with the given
	 *         registration center from database
	 */
	@Query(value = "SELECT * FROM master.device_master dm   where dm.regcntr_id=?1", countQuery = "SELECT count(*) FROM master.device_master dm  where dm.regcntr_id=?1", nativeQuery = true)
	Page<Device> findDeviceByRegCenterId(String regCenterId, Pageable pageable);

	@Query("FROM Device d where d.id = ?1 and d.langCode = ?2 AND (d.isDeleted is null or d.isDeleted = false)")
	Device findByIdAndLangCodeAndIsDeletedFalseOrIsDeletedIsNullNoIsActive(String id, String langCode);

	/**
	 * This method trigger query to fetch the Device id's those are mapped with the
	 * regCenterId
	 * 
	 * @return Device id's fetched for all device those are mapped with the
	 *         registration center from database
	 */
	@Query(value = "select d.id from master.device_master d where d.regcntr_id is not null and d.lang_code=?1", nativeQuery = true)
	List<String> findMappedDeviceId(String langCode);

	/**
	 * This method trigger query to fetch the Device id's those are not mapped with
	 * the regCenterId
	 * 
	 * @return Device id's fetched for all device those are not mapped with the
	 *         registration center from database
	 */
	@Query(value = "select d.id from master.device_master d where d.regcntr_id is null and d.lang_code=?1", nativeQuery = true)
	List<String> findNotMappedDeviceId(String langCode);

	@Query(value = "Select * from master.device_spec ds where (ds.is_deleted is null or ds.is_deleted = false) and ds.is_active = true and ds.dtyp_code IN (select code from master.device_type dt where dt.name=?1) and ds.lang_code=?2", nativeQuery = true)
	List<Object[]> findDeviceSpecByDeviceTypeNameAndLangCode(String typeName, String langCode);

	@Query(value = "select d.name from master.registration_center r,master.device_master d where r.id=d.regcntr_id   and r.lang_code=d.lang_code and d.id in(?1) and d.lang_code=?2", nativeQuery = true)
	List<String> findDeviceNameByDevicesAndLangCode(List<String> devices, String langCode);

	/**
	 * This method trigger query to fetch the Device detail for the given id and
	 * language code for both is_Deleted false or true or null and isActive true or
	 * false
	 * 
	 * @param id       the id of device
	 * @param langCode language code from user
	 * @return the device detail
	 */
	@Query("FROM Device d where d.id = ?1 and d.langCode = ?2")
	Device findByIdAndLangCode(String id, String langCode);

	/**
	 * This method trigger query to fetch the Machine detail for the given id code.
	 * 
	 * @param id machine Id provided by user
	 * 
	 * @return MachineDetail fetched from database
	 */

	@Query("FROM Device d where d.id = ?1 and (d.isDeleted is null or d.isDeleted = false)")
	List<Device> findDeviceByIdAndIsDeletedFalseorIsDeletedIsNullNoIsActive(String id);

	/**
	 * Method to decommission the Device
	 * 
	 * @param deviceID               the device id which needs to be decommissioned.
	 * @param deCommissionedBy       the user name retrieved from the context who
	 *                               performs this operation.
	 * @param deCommissionedDateTime date and time at which the center was
	 *                               decommissioned.
	 * @return the number of device decommissioned.
	 */
	@Query("UPDATE Device d SET d.isDeleted = true, d.isActive = false, d.updatedBy = ?2, d.updatedDateTime = ?3, d.deletedDateTime=?3 WHERE d.id=?1 and (d.isDeleted is null or d.isDeleted =false)")
	@Modifying
	@Transactional
	int decommissionDevice(String id, String deCommissionedBy, LocalDateTime deCommissionedDateTime);

	/**
	 * This method trigger query to fetch the Machine detail for the given id code.
	 * 
	 * @param id machine Id provided by user
	 * 
	 * @return MachineDetail fetched from database
	 */

	@Query("FROM Device d where d.serialNum = ?1 and (d.isDeleted is null or d.isDeleted = false)")
	List<Device> findDeviceBySerialNumberAndIsDeletedFalseorIsDeletedIsNullNoIsActive(String serialNum);
	
	@Query(value = "select count(*) from master.device_master where regcntr_id=?1 and (is_deleted is null or is_deleted=false);", nativeQuery = true)
	long countCenterDevices(String id);
	@Query("FROM Device WHERE (isDeleted is null or isDeleted =false) and isActive = true")
	List<Device> getAllDevicesList();
	@Query("FROM Device WHERE regCenterId=?1 and(isDeleted is null or isDeleted =false) and isActive = true")
	List<Device> findByRegIdAndIsDeletedFalseOrIsDeletedIsNull(String id);

}
