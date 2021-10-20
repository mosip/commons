package io.mosip.kernel.keymanagerservice.dto;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * DTO class for Symmetric Key Generate response.
 * 
 * @author Mahammed Taheer
 * @since 1.1.4
 *
 */
@Data
public class SymmetricKeyGenerateResponseDto {
    
    /**
	 * Status of Key Generation.
	 */
	private String status;

	/**
	 * Timestamp.
	 */
	private LocalDateTime timestamp;
}