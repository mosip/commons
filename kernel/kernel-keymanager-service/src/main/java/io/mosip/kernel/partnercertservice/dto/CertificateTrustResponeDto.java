package io.mosip.kernel.partnercertservice.dto;

import lombok.Data;

/**
 * DTO class for certificate verification response.
 * 
 * @author Mahammed Taheer
 * @since 1.1.2
 *
 */
@Data
public class CertificateTrustResponeDto {
    
    /**
	 * Status of certificate verification.
	 */
	private Boolean status;

}