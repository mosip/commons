/*
 * 
 * 
 * 
 * 
 */
package io.mosip.kernel.cryptomanager.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Crypto-With-Pin-Response model
 * 
 * @author Mahammed Taheer
 *
 * @since 1.1.2
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Model representing a Crypto-With-Pin-Response Response")
public class CryptoWithPinResponseDto {
	/**
	 * Data Encrypted/Decrypted in String
	 */
	@ApiModelProperty(notes = "Data encrypted/decrypted in String")
	private String data;
}
