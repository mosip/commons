package io.mosip.kernel.keymigrate.dto;

import java.time.LocalDateTime;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response class for Key Pair Migration.
 * 
 * @author Mahammed Taheer
 * @since 1.1.15
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Class representing a Migration of KeyPair Response")
public class KeyMigrateBaseKeyResponseDto {
    
    /**
	 * Status of key migration.
	 */
	private String status;

	/**
	 * Timestamp.
	 */
	private LocalDateTime timestamp;
}
