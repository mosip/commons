package io.mosip.kernel.keymanagerservice.dto;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * DTO class for revoke key response.
 * 
 * @author Mahammed Taheer
 * @since 1.1.6
 *
 */
@Data
public class RevokeKeyResponseDto {
    
    /**
	 * Status of revoke key.
	 */
	private String status;

	/**
	 * Timestamp.
	 */
	private LocalDateTime timestamp;
}