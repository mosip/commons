package io.mosip.kernel.zkcryptoservice.dto;

import java.util.List;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Zero Knowledge Encrypt/Decrypt Request DTO.
 * 
 * @author Mahammed Taheer
 * @since 1.1.2
*/

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Model representing response a list of data attributes for encrypt/decrypt")
public class ZKCryptoResponseDto {
    
    /**
	 * zero knowledge Data Attributes to encrypt/decrypt.
	 */
    List<CryptoDataDto> zkDataAttributes;
    
    /**
	 * Encrypted Random Key 
	 */
    String encryptedRandomKey;

    /**
	 * Index of the random key used.
	 */
    String rankomKeyIndex;
}