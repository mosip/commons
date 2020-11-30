package io.mosip.kernel.syncdata.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import io.mosip.kernel.syncdata.entity.ReasonList;

/**
 * 
 * @author Srinivasan
 *
 */
public interface ReasonListRepository extends JpaRepository<ReasonList, String> {
	/**
	 * Method to find list of ReasonList created , updated or deleted time is
	 * greater than lastUpdated timeStamp.
	 * 
	 * @param lastUpdated      timeStamp - last updated timestamp
	 * @param currentTimeStamp - currentTimestamp
	 * @return list of {@link ReasonList} -list of reason list
	 */
	@Query("FROM ReasonList WHERE (createdDateTime BETWEEN ?1 AND ?2) OR (updatedDateTime BETWEEN ?1 AND ?2)  OR (deletedDateTime BETWEEN ?1 AND ?2)")
	List<ReasonList> findAllLatestCreatedUpdateDeleted(LocalDateTime lastUpdated, LocalDateTime currentTimeStamp);
}
