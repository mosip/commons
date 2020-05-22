package io.mosip.kernel.packetmanager.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.mosip.kernel.packetmanager.constants.CryptomanagerConstant;
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
	
	@NotBlank(message = CryptomanagerConstant.INVALID_REQUEST)
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
	
	@NotBlank(message = CryptomanagerConstant.INVALID_REQUEST)
	private String data;
}
