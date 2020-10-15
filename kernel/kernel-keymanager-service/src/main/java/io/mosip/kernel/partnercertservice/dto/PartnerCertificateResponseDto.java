package io.mosip.kernel.partnercertservice.dto;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * DTO class for upload certificate response.
 * 
 * @author Mahammed Taheer
 * @since 1.1.2
 *
 */
@Data
public class PartnerCertificateResponseDto {
    
    /**
	 * Field for certificate
	 */
	private String signedCertificateData;
	
	/**
	 * Field for certificateId
	 */
    private String certificateId;
    
    /**
	 * Field for Response time
	 */
	private LocalDateTime timestamp;

}