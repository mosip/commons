package io.mosip.kernel.zkcryptoservice.dto;

import lombok.Data;

/**
 * DTO class for Re-Encrypt Random Key response.
 * 
 * @author Mahammed Taheer
 * @since 1.1.2
 *
 */
@Data
public class ReEncryptRandomKeyResponseDto {
    
    /**
	 * Status of upload certificate.
	*/
    private String encryptedKey;
    
}