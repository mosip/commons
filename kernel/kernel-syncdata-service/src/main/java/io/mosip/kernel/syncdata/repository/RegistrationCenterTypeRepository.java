package io.mosip.kernel.syncdata.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import io.mosip.kernel.syncdata.entity.RegistrationCenterType;

/**
 * Interface for RegistrationCenterType Repository.
 * 
 * @author Sagar Mahapatra
 * @since 1.0.0
 *
 */
public interface RegistrationCenterTypeRepository extends JpaRepository<RegistrationCenterType, String> {
	/**
	 * Method to fetch the RegistartionCenterType for which the machine has been
	 * mapped.
	 * 
	 * @param machineId id of the machine
	 * @return list of {@link RegistrationCenterType} - list of registration center
	 *         type
	 */
	@Query(value = "SELECT distinct regtype.code, regtype.name, regtype.descr, regtype.lang_code, regtype.is_active, regtype.cr_by, regtype.cr_dtimes, regtype.upd_by, regtype.upd_dtimes, regtype.is_deleted, regtype.del_dtimes FROM master.reg_center_type regtype , master.registration_center rc,master.machine_master rcmd where regtype.code= rc.cntrtyp_code and rc.id=rcmd.regcntr_id and rcmd.id= ?1", nativeQuery = true)
	List<RegistrationCenterType> findRegistrationCenterTypeByMachineId(String machineId);

	/**
	 * Method to fetch the latest RegistartionCenterType for which the machine has
	 * been mapped.
	 * 
	 * @param machineId        id of the machine
	 * @param lastUpdated      timeStamp - last updated timestamp
	 * @param currentTimeStamp - current timestamp
	 * @return list of {@link RegistrationCenterType} - list of registration center
	 *         type
	 */
	@Query(value = "SELECT distinct regtype.code, regtype.name, regtype.descr, regtype.lang_code, regtype.is_active, regtype.cr_by, regtype.cr_dtimes, regtype.upd_by, regtype.upd_dtimes, regtype.is_deleted, regtype.del_dtimes FROM master.reg_center_type regtype , master.registration_center rc,master.machine_master rcmd where regtype.code= rc.cntrtyp_code and rc.id=rcmd.regcntr_id and rcmd.id= ?1 and ((regtype.cr_dtimes BETWEEN ?2 AND ?3) or (regtype.upd_dtimes BETWEEN ?2 AND ?3) or (regtype.del_dtimes BETWEEN ?2 AND ?3)) ", nativeQuery = true)
	List<RegistrationCenterType> findLatestRegistrationCenterTypeByMachineId(String machineId,
			LocalDateTime lastUpdated, LocalDateTime currentTimeStamp);
}
