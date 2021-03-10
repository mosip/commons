package io.mosip.kernel.syncdata.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.syncdata.entity.BiometricAttribute;

/**
 * 
 * @author Abhishek Kumar
 * @since 1.0.0
 *
 */
@Repository
public interface BiometricAttributeRepository extends JpaRepository<BiometricAttribute, String> {
	/**
	 * Method to find list of BiometricAttribute created , updated or deleted time
	 * is greater than lastUpdated timeStamp.
	 * 
	 * @param lastUpdated      timeStamp - last updated time
	 * @param currentTimeStamp - currentTimestamp
	 * @return list of {@link BiometricAttribute} - list of biometric attribute
	 */
	@Query("FROM BiometricAttribute WHERE (createdDateTime BETWEEN ?1 AND ?2) OR (updatedDateTime BETWEEN ?1 AND ?2)  OR (deletedDateTime BETWEEN ?1 AND ?2)")
	List<BiometricAttribute> findAllLatestCreatedUpdateDeleted(LocalDateTime lastUpdated,
			LocalDateTime currentTimeStamp);
}
