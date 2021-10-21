
package io.mosip.kernel.keymanagerservice.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.keymanagerservice.entity.CACertificateStore;

/**
 * This interface CACertificateStoreRepository for CRUD operations for CA/Sub-CA certificates.
 * 
 * @author Mahammed Taheer
 * @since 1.1.2
 *
 */
@Repository
public interface CACertificateStoreRepository extends JpaRepository<CACertificateStore, String> {

	/**
	 * Function to find CACertificates by Certificate Subject and Certificate Issuer. 
	 * 
	 * @param certSubject Certificate Subject
     * @param certIssuer Certificate Issuer
	 * @return list of CACertificateStore
	 */
    List<CACertificateStore> findByCertSubjectAndCertIssuer(String certSubject, String certIssuer);
    

    /**
	 * Function to find CACertificate by Certificate thumbprint. 
	 * 
	 * @param certThumbprint Certificate Thumbprint
	 * @return CACertificateStore
	 */
	CACertificateStore findByCertThumbprint(String certThumbprint);

	/**
	 * Function to fetch all CACertificates. 
	 * 
	 * @return list of CACertificateStore
	*/
	List<CACertificateStore> findAll();
	
	/**
	 * Function to find CACertificates by Certificate Subject. 
	 * 
	 * @param certSubject Certificate Subject
	 * @return list of CACertificateStore
	 */
	List<CACertificateStore> findByCertSubject(String certSubject);
	
	/**
	 * Function to find CACertificates by Partner Domain. 
	 * 
	 * @param partnerDomain Certificate Subject
	 * @return list of CACertificateStore
	 */
	List<CACertificateStore> findByPartnerDomain(String partnerDomain);
	
	/**
	 * Function to find CACertificate by Certificate thumbprint and Partner domain. 
	 * 
	 * @param certThumbprint Certificate Thumbprint
	 * @param partnerDomain Partner Domain
	 * 
	 * @return CACertificateStore
	 */
	CACertificateStore findByCertThumbprintAndPartnerDomain(String certThumbprint, String partnerDomain);

	/**
	 * Function to find all CACertificate created , updated or deleted time is within the lastUpdated and current time
	 * @param lastUpdated
	 * @param currentTimestamp
	 * @return
	 */
	@Query("FROM CACertificateStore WHERE (createdtimes BETWEEN ?1 AND ?2) OR (updatedtimes BETWEEN ?1 AND ?2)  OR (deletedtimes BETWEEN ?1 AND ?2)")
	List<CACertificateStore> findAllLatestCreatedUpdateDeleted(LocalDateTime lastUpdated, LocalDateTime currentTimestamp);

}
