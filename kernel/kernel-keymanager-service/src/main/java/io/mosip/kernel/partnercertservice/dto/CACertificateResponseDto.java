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
public class CACertificateResponseDto {
    
    /**
	 * Status of upload certificate.
	 */
	private String status;

	/**
	 * Response timestamp.
	 */
	private LocalDateTime timestamp;
}