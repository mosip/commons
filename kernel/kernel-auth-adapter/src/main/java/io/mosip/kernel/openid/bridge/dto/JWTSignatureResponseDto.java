package io.mosip.kernel.openid.bridge.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Keymanager response after signing {@link JWSSignatureRequestDto}: compact JWS
 * and the time of the response.
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
	 * Compact JWS/JWT produced by keymanager; used as OAuth
	 * {@code client_assertion} when private-key JWT client auth is enabled.
	 */
	private String jwtSignedData;

	/**
	 * UTC (or server) timestamp when keymanager returned the signature.
	 */
	private LocalDateTime timestamp;
}
