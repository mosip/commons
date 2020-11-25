package io.mosip.kernel.syncdata.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.syncdata.entity.ApplicantValidDocument;
import io.mosip.kernel.syncdata.entity.id.ApplicantValidDocumentID;

/**
 * Repository to handle CRUD operations for {@link ApplicantValidDocument}
 * 
 * @author Srinivasan
 *
 */
@Repository
public interface ApplicantValidDocumentRespository
		extends JpaRepository<ApplicantValidDocument, ApplicantValidDocumentID> {

	@Query("FROM ApplicantValidDocument avd WHERE (createdDateTime BETWEEN ?1 AND ?2) OR (updatedDateTime BETWEEN ?1 AND ?2)  OR (deletedDateTime BETWEEN ?1 AND ?2) ")
	public List<ApplicantValidDocument> findAllByTimeStamp(LocalDateTime lastUpdatedTime,
			LocalDateTime currentTimeStamp);
}
