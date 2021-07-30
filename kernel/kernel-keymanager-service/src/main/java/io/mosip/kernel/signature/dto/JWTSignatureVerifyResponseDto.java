package io.mosip.kernel.signature.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author Mahammed Taheer
 * @since 1.2.0-rc1-SNAPSHOT
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JWTSignatureVerifyResponseDto {
    
    /**
     * The Signature verification status.
    */
    private boolean signatureValid;
    
	/**
	 * The Signature validation message.
	 */
    private String message;
    
    /**
	 * The Trust validation status.
	 */
    private String trustValid;
}
