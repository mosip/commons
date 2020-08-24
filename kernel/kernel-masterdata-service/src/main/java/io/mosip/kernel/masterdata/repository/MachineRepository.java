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
import io.mosip.kernel.masterdata.entity.Machine;

/**
 * Repository to perform CRUD operations on Machine.
 * 
 * @author Megha Tanga
 * @author Sidhant Agarwal
 * @author Ravi Kant
 * @since 1.0.0
 *
 */

@Repository
public interface MachineRepository extends BaseRepository<Machine, String> {
	// PagingAndSortingRepository<Machine, Integer>
	/**
	 * This method trigger query to fetch the all Machine details.
	 * 
	 * @return List MachineDetail fetched from database
	 * 
	 */
	@Query("FROM Machine where (isDeleted is null OR isDeleted = false) AND isActive = true")
	List<Machine> findAllByIsDeletedFalseOrIsDeletedIsNull();

	/**
	 * This method trigger query to fetch the Machine detail for the given machine
	 * id and language code.
	 * 
	 * 
	 * @param id       Machine Id provided by user
	 * @param langCode language code provided by user
	 * @return List MachineDetail fetched from database
	 */

	@Query("FROM Machine m where m.id = ?1 and m.langCode = ?2 and (m.isDeleted is null or m.isDeleted = false) and m.isActive = true")
	List<Machine> findAllByIdAndLangCodeAndIsDeletedFalseorIsDeletedIsNull(String id, String langCode);

	/**
	 * This method trigger query to fetch the Machine detail for the given language
	 * code.
	 * 
	 * @param langCode langCode provided by user
	 * 
	 * @return List MachineDetail fetched from database
	 */
	@Query("FROM Machine m where m.langCode = ?1 and (m.isDeleted is null or m.isDeleted = false) and m.isActive = true")
	List<Machine> findAllByLangCodeAndIsDeletedFalseOrIsDeletedIsNull(String langCode);

	/**
	 * This method trigger query to fetch the Machine detail for the given id code.
	 * 
	 * @param id machine Id provided by user
	 * 
	 * @return MachineDetail fetched from database
	 */

	@Query("FROM Machine m where m.id = ?1 and (m.isDeleted is null or m.isDeleted = false) and m.isActive = true")
	List<Machine> findMachineByIdAndIsDeletedFalseorIsDeletedIsNull(String id);

	/**
	 * This method trigger query to fetch the Machine detail for the given id code.
	 * 
	 * @param machineSpecId machineSpecId provided by user
	 * 
	 * @return MachineDetail fetched from database
	 */

	@Query("FROM Machine m where m.machineSpecId = ?1 and (m.isDeleted is null or m.isDeleted = false) and m.isActive = true")
	List<Machine> findMachineBymachineSpecIdAndIsDeletedFalseorIsDeletedIsNull(String machineSpecId);

	/**
	 * This method trigger query to fetch the Machine detail for the given id and
	 * language code.
	 * 
	 * @param id       machine Id provided by user
	 * @param langCode machine language code by user
	 * 
	 * @return MachineDetail fetched from database
	 */

	@Query("FROM Machine m where m.id = ?1 and m.langCode = ?2 and (m.isDeleted is null or m.isDeleted = false) AND m.isActive = true")
	Machine findMachineByIdAndLangCodeAndIsDeletedFalseorIsDeletedIsNull(String id, String langCode);

	@Query("FROM Machine m where m.id = ?1 and m.langCode = ?2")
	Machine findMachineByIdAndLangCode(String id, String langCode);

	/**
	 * This method trigger query to fetch the Machine detail those are mapped with
	 * the given regCenterId
	 * 
	 * @param regCenterId regCenterId provided by user
	 * @return Machine fetch the list of Machine details those are mapped with the
	 *         given regCenterId
	 */
	@Query(value = "SELECT * FROM master.machine_master mm  where mm.regcntr_id =?1", countQuery = "SELECT count(*) FROM  master.machine_master mm   where mm.regcntr_id =?1", nativeQuery = true)
	Page<Machine> findMachineByRegCenterId(String regCenterId, Pageable pageable);

	@Query("FROM Machine m where m.id = ?1 and m.langCode = ?2 and (m.isDeleted is null or m.isDeleted = false)")
	Machine findMachineByIdAndLangCodeAndIsDeletedFalseorIsDeletedIsNullWithoutActiveStatusCheck(String id,
			String langCode);

	@Query(value = "select m.id from master.machine_master m where m.regcntr_id is not null  and m.lang_code=?1", nativeQuery = true)
	List<String> findMappedMachineId(String langCode);

	@Query(value = "select m.id from master.machine_master m where m.regcntr_id is  null and m.lang_code=?1", nativeQuery = true)
	List<String> findNotMappedMachineId(String langCode);

	@Query(value = "Select * from master.machine_spec ms where (ms.is_deleted is null or ms.is_deleted = false) and ms.is_active = true and ms.mtyp_code IN (select code from master.machine_type mt where mt.name=?1) and ms.lang_code=?2", nativeQuery = true)
	List<Object[]> findMachineSpecByMachineTypeNameAndLangCode(String name, String langCode);

	/**
	 * This method trigger query to fetch the Machine detail for the given id code.
	 * 
	 * @param id machine Id provided by user
	 * 
	 * @return MachineDetail fetched from database
	 */

	@Query("FROM Machine m where m.id = ?1 and (m.isDeleted is null or m.isDeleted = false)")
	List<Machine> findMachineByIdAndIsDeletedFalseorIsDeletedIsNullNoIsActive(String id);

	/**
	 * Method to decommission the Machine
	 * 
	 * @param machineID              the machine id which needs to be
	 *                               decommissioned.
	 * @param deCommissionedBy       the user name retrieved from the context who
	 *                               performs this operation.
	 * @param deCommissionedDateTime date and time at which the center was
	 *                               decommissioned.
	 * @return the number of machine decommissioned.
	 */
	@Query("UPDATE Machine m SET m.isDeleted = true, m.isActive = false, m.updatedBy = ?2, m.updatedDateTime=?3, m.deletedDateTime=?3 WHERE m.id=?1 and (m.isDeleted is null or m.isDeleted =false)")
	@Modifying
	@Transactional
	int decommissionMachine(String id, String deCommissionedBy, LocalDateTime deCommissionedDateTime);
	
	@Query(value = "select count(*) from master.machine_master where regcntr_id=?1 and (is_deleted is null or is_deleted=false)", nativeQuery = true)
	long countCenterMachines(String id);

	@Query("FROM Machine WHERE (isDeleted is null or isDeleted =false) and isActive = true")
	List<Machine> getAllMachines();

	@Query("FROM Machine WHERE regCenterId=?1 and (isDeleted is null or isDeleted =false) and isActive = true")
	List<Machine> findByRegIdAndIsDeletedFalseOrIsDeletedIsNull(String regId);
}
