package io.mosip.kernel.keymanagerservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.keymanagerservice.entity.PartnerCertificateStore;

/**
 * This interface PartnerCertificateStoreRepository for CRUD operations for Partner certificates.
 * 
 * @author Mahammed Taheer
 * @since 1.1.2
 *
 */
@Repository
public interface PartnerCertificateStoreRepository extends JpaRepository<PartnerCertificateStore, String> {

	/**
	 * Function to find Partner Certificates by Certificate Subject and Certificate Issuer. 
	 * 
	 * @param certSubject Certificate Subject
     * @param cercertIssuertSubject Certificate Issuer
	 * @return list of PartnerCertificateStore
	 */
    List<PartnerCertificateStore> findByCertSubjectAndCertIssuer(String certSubject, String certIssuer);
    

    /**
	 * Function to find Partner Certificate by Certificate thumbprint. 
	 * 
	 * @param certThumbprint Certificate Thumbprint
	 * @return PartnerCertificateStore
	 */
	PartnerCertificateStore findByCertThumbprint(String certThumbprint);

	/**
	 * Function to find CACertificates by Certificate Subject. 
	 * 
	 * @param certSubject Certificate Subject
	 * @return list of PartnerCertificateStore
	 */
	List<PartnerCertificateStore> findByCertSubject(String certSubject);
	
	/**
	 * Function to find Partner Certificate by Certificate ID. 
	 * 
	 * @param certId Certificate ID
	 * @return PartnerCertificateStore
	 */
	PartnerCertificateStore findByCertId(String certId);

	 /**
	 * Function to find Partner Certificate by Certificate thumbprint. 
	 * 
	 * @param certThumbprint Certificate Thumbprint
	 * @param partnerDomain Partner Domain
	 * @return PartnerCertificateStore
	 */
	List<PartnerCertificateStore> findByCertThumbprintAndPartnerDomain(String certThumbprint, String partnerDomain);
}
