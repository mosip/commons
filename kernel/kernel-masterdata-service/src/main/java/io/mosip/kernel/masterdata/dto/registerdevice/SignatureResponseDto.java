/*
 * 
 * 
 * 
 * 
 */
package io.mosip.kernel.masterdata.dto.registerdevice;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Crypto-Manager-Response model
 * 
 * @author Urvil Joshi
 *
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignatureResponseDto {
	/**
	 * Data Encrypted/Decrypted in BASE64 encoding
	 */
	private String data;

}
