package io.mosip.kernel.zkcryptoservice.dto;

import javax.validation.constraints.NotBlank;

import io.mosip.kernel.keymanagerservice.constant.KeymanagerConstant;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Crypto Data for encrypt/decrypt.
 * 
 * @author Mahammed Taheer
 * @since 1.1.2
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Model representing a identifier & value for encrypt/decrypt")
public class CryptoDataDto {

    /**
	 * identifier for the value to encrypt/decrypt.
	 */
	@ApiModelProperty(notes = "Identifier", example = "name", required = true)
	@NotBlank(message = KeymanagerConstant.INVALID_REQUEST)
	private String identifier;

	/**
	 * actual data to encrypt/decrypt.
	 */
	@ApiModelProperty(notes = "Data to Encrypt/Decrypt", example = "Plain/Encrypted String", required = true)
	@NotBlank(message = KeymanagerConstant.INVALID_REQUEST)
	private String value;
    
}