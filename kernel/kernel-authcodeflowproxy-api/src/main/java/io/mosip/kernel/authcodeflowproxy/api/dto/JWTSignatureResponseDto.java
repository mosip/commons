package io.mosip.kernel.authcodeflowproxy.api.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author Mahammed Taheer
 * @since 1.2.0-SNAPSHOT
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JWTSignatureResponseDto {

	/**
	 * encrypted data
	 */
	private String jwtSignedData;

	/**
	 * response time.
	 */
	private LocalDateTime timestamp;
}
