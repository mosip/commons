package io.mosip.kernel.syncdata.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.syncdata.entity.ValidDocument;
import io.mosip.kernel.syncdata.entity.id.ValidDocumentID;

@Repository
public interface ValidDocumentRepository extends JpaRepository<ValidDocument, ValidDocumentID> {
	/**
	 * Method to find list of Title created , updated or deleted time is greater
	 * than lastUpdated timeStamp.
	 * 
	 * @param lastUpdated      timeStamp - last updated time stamp
	 * @param currentTimeStamp - current time stamp
	 * @return list of {@link ValidDocument} - list of validDocument
	 */
	@Query("FROM ValidDocument WHERE (createdDateTime BETWEEN ?1 AND ?2) OR (updatedDateTime BETWEEN ?1 AND ?2)  OR  (deletedDateTime BETWEEN ?1 AND ?2)")
	List<ValidDocument> findAllLatestCreatedUpdateDeleted(LocalDateTime lastUpdated, LocalDateTime currentTimeStamp);
}