package io.mosip.commons.packet.dto.packet;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 
 * @author Sowmya
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor

public class CryptomanagerRequestDto {
	/**
	 * Application id of decrypting module
	 */
	
	@NotBlank(message = "should not be null or empty")
	private String applicationId;
	/**
	 * Refrence Id
	 */
	
	private String referenceId;
	/**
	 * Timestamp
	 */

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	@NotNull
	private LocalDateTime timeStamp;
	/**
	 * Data in BASE64 encoding to encrypt/decrypt
	 */
	
	@NotBlank(message = "should not be null or empty")
	private String data;

	private Boolean prependThumbprint;

	/**
	 * salt in BASE64 encoding for encrypt/decrypt
	 */

	@NotBlank(message = "should not be null or empty")
	private String salt;

	/**
	 * aad in BASE64 encoding for encrypt/decrypt
	 */

	@NotBlank(message = "should not be null or empty")
	private String aad;
}
