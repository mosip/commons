/*
 * 
 * 
 * 
 * 
 */
package io.mosip.kernel.cryptosignature.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel(description = "Model representing a Key-Manager-Service Request")
public class SignatureRequestDto {

	
	@ApiModelProperty(notes = "Data to sign", required = true)
	// @NotBlank(message = CryptomanagerConstant.INVALID_REQUEST)
	private String data;

}
