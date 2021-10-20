package io.mosip.kernel.keymanagerservice.dto;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * DTO class for upload certificate response.
 * 
 * @author Mahammed Taheer
 * @since 1.0.10
 *
 */
@Data
public class UploadCertificateResponseDto {
    
    /**
	 * Status of upload certificate.
	 */
	private String status;

	/**
	 * Timestamp.
	 */
	private LocalDateTime timestamp;
}