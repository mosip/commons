package io.mosip.kernel.syncdata.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.kernel.syncdata.entity.UserDetailHistoryPk;
import io.mosip.kernel.syncdata.entity.UserDetailsHistory;


/**
 * @author Sidhant Agarwal
 * @since 1.0.0
 *
 */
@Repository
public interface UserDetailsHistoryRepository extends JpaRepository<UserDetailsHistory, UserDetailHistoryPk> {

	/**
	 * @param userId    input from user
	 * @param effDTimes input from user
	 * @return list of user details for the particular input
	 */
	// select a.id,a.langCode,a.name,a.uin,a.email,a.mobile,a.statusCode
	// SELECT m.id,m.langCode,m.name,m.uin,m.email,m.mobile,m.statusCode,m.effDTimes
	@Query(value = "select * from (SELECT * FROM master.user_detail_h m WHERE id = ?1 AND eff_dtimes<= ?2 and (is_deleted is null or is_deleted =false) ORDER BY eff_dtimes DESC) a LIMIT 1", nativeQuery = true)
	List<UserDetailsHistory> getByUserIdAndTimestamp(String userId, LocalDateTime effDTimes);
	// (?2 BETWEEN effDTimes AND CURRENT_TIMESTAMP)
	@Query("From UserDetailsHistory mm WHERE mm.regCenterId =?1 AND ((mm.createdDateTime BETWEEN ?2 AND ?3) OR (mm.updatedDateTime BETWEEN ?2 AND ?3) OR (mm.deletedDateTime BETWEEN ?2 AND ?3))")
	List<UserDetailsHistory> findLatestRegistrationCenterUserHistory(String regId, LocalDateTime lastUpdated,
			LocalDateTime currentTimeStamp);

}
