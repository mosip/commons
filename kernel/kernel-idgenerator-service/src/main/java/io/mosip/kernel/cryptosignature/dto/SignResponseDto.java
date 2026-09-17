package io.mosip.kernel.cryptosignature.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Keymanager sign API response mapped by {@link io.mosip.kernel.cryptosignature.service.impl.SignatureUtilImpl}.
 *
 * @author Srinivasan
 * @since 1.0.0
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignResponseDto {

	/**
	 * Digital signature of the requested payload.
	 */
	private String signature;

	/**
	 * UTC timestamp returned by keymanager with the signature.
	 */
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime timestamp;
}
