package io.mosip.kernel.syncdata.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.syncdata.entity.MachineHistory;

/**
 * Repository function to fetching machine History Details
 * 
 * @author Megha Tanga
 * @since 1.0.0
 *
 */
@Repository
public interface MachineHistoryRepository extends JpaRepository<MachineHistory, String> {

	/**
	 * This method trigger query to fetch Machine History Details based on Machine
	 * Id, language code and effective date time
	 * 
	 * @param id           Machine History id provided by user
	 * @param langCode     language code provided by user
	 * @param effectDtimes effective Date and time provided by user in the format
	 *                     "yyyy-mm-ddThh:mm:ss"
	 * @return list of {@link MachineHistory} - list of machine history
	 */
	List<MachineHistory> findByIdAndLangCodeAndEffectDateTimeLessThanEqualAndIsDeletedFalse(String id, String langCode,
			LocalDateTime effectDtimes);
	@Query("From MachineHistory mm WHERE mm.regCenterId =?1 AND ((mm.createdDateTime BETWEEN ?2 AND ?3) OR (mm.updatedDateTime BETWEEN ?2 AND ?3) OR (mm.deletedDateTime BETWEEN ?2 AND ?3))")
	List<MachineHistory> findLatestRegistrationCenterMachineHistory(String regId, LocalDateTime lastUpdated,
			LocalDateTime currentTimeStamp);
}
