package io.mosip.kernel.partnercertservice.dto;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * DTO class for download of partner certificate response.
 * 
 * @author Mahammed Taheer
 * @since 1.1.2
 *
 */
@Data
public class PartnerCertDownloadResponeDto {
    
    /**
	 * Partner Certificate Data.
	 */
	private String certificateData;

	/**
	 * Response timestamp.
	 */
	private LocalDateTime timestamp;
}