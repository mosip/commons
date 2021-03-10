package io.mosip.kernel.syncdata.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.syncdata.entity.DocumentType;

/**
 * 
 * @author Abhishek Kumar
 * @author Uday Kumar
 * @since 1.0.0
 *
 */
@Repository
public interface DocumentTypeRepository extends JpaRepository<DocumentType, String> {
	/**
	 * Method to find list of DocumentType created , updated or deleted time is
	 * greater than lastUpdated timeStamp.
	 * 
	 * @param lastUpdated      timeStamp - last updated timestamp
	 * @param currentTimeStamp - currentTimestamp
	 * @return list of {@link DocumentType} - list of document type
	 */
	@Query("FROM DocumentType WHERE (createdDateTime BETWEEN ?1 AND ?2) OR (updatedDateTime BETWEEN ?1 AND ?2)  OR (deletedDateTime BETWEEN ?1 AND ?2)")
	List<DocumentType> findAllLatestCreatedUpdateDeleted(LocalDateTime lastUpdated, LocalDateTime currentTimeStamp);
}
