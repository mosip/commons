/*
 * 
 * 
 * 
 * 
 */
package io.mosip.kernel.masterdata.dto.registerdevice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Crypto-Manager-Request model
 * 
 * @author Urvil Joshi
 *
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignatureRequestDto {

	private String applicationId;
	/**
	 * Refrence Id
	 */
	private String referenceId;
	/**
	 * Timestamp
	 */
	private String timeStamp;
	/**
	 * Data in BASE64 encoding to encrypt/decrypt
	 */
	private String data;

}
