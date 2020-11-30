package io.mosip.kernel.syncdata.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.syncdata.entity.Application;

/**
 * 
 * @author Neha
 * @since 1.0.0
 *
 */
@Repository
public interface ApplicationRepository extends JpaRepository<Application, String> {

	/**
	 * Method to find list of Application created , updated or deleted time is
	 * greater than lastUpdated timeStamp.
	 * 
	 * @param lastUpdated      - last updated Time stamp
	 * @param currentTimeStamp - currentTimestamp
	 * @return list of {@link Application} - list of applications
	 */
	@Query("FROM Application WHERE (createdDateTime BETWEEN ?1 AND ?2) OR (updatedDateTime BETWEEN ?1 AND ?2)  OR (deletedDateTime BETWEEN ?1 AND ?2)")
	List<Application> findAllLatestCreatedUpdateDeleted(LocalDateTime lastUpdated, LocalDateTime currentTimeStamp);
}
