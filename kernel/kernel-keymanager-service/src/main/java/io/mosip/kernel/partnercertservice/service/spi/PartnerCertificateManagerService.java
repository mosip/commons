package io.mosip.kernel.partnercertservice.service.spi;

import io.mosip.kernel.partnercertservice.dto.CACertificateRequestDto;
import io.mosip.kernel.partnercertservice.dto.CACertificateResponseDto;
import io.mosip.kernel.partnercertservice.dto.CertificateTrustRequestDto;
import io.mosip.kernel.partnercertservice.dto.CertificateTrustResponeDto;
import io.mosip.kernel.partnercertservice.dto.PartnerCertDownloadRequestDto;
import io.mosip.kernel.partnercertservice.dto.PartnerCertDownloadResponeDto;
import io.mosip.kernel.partnercertservice.dto.PartnerCertificateRequestDto;
import io.mosip.kernel.partnercertservice.dto.PartnerCertificateResponseDto;

/**
 * This interface provides the methods for Partner Certificate Management Service.
 * 
 * @author Mahammed Taheer
 * @since 1.1.2
 *
 */

public interface PartnerCertificateManagerService {
    
    /**
	 * Function to Upload CA/Sub-CA certificates
	 * 
	 * @param CACertificateRequestDto caCertResponseDto
	 * @return {@link CACertificateResponseDto} instance
	 */
    public CACertificateResponseDto uploadCACertificate(CACertificateRequestDto caCertResponseDto);

    /**
     * Function to Upload Partner certificates
     * 
     * @param PartnerCertificateRequestDto partnerCertResponseDto
     * @return {@link PartnerCertificateResponseDto} instance
    */
    public PartnerCertificateResponseDto uploadPartnerCertificate(PartnerCertificateRequestDto partnerCertResponseDto);

    /**
     * Function to Download Partner certificates
     * 
     * @param PartnerCertDownloadRequestDto certDownloadRequestDto
     * @return {@link PartnerCertDownloadResponeDto} instance
    */
    public PartnerCertDownloadResponeDto getPartnerCertificate(PartnerCertDownloadRequestDto certDownloadRequestDto);

    /**
     * Function to verify partner certificates trust.
     * 
     * @param CertificateTrustRequestDto certificateTrustRequestDto
     * @return {@link CertificateTrustResponeDto} instance
    */
    public CertificateTrustResponeDto verifyCertificateTrust(CertificateTrustRequestDto certificateTrustRequestDto);
    
}