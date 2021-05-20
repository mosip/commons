package io.mosip.kernel.keymanagerservice.dto;

import javax.validation.constraints.NotBlank;

import io.mosip.kernel.keymanagerservice.constant.KeymanagerConstant;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Revoke Base Key -Request model
 * 
 * @author Mahammed Taheer
 *
 * @since 1.1.6
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Model representing to revoke any base key.")
public class RevokeKeyRequestDto {

    	
	/**
	 * Application Id For Key to be revoked
	 */
	@ApiModelProperty(notes = "Application ID", example = "REGISTRATION", required = true)
	@NotBlank(message = KeymanagerConstant.INVALID_REQUEST)
	private String applicationId;
	
	/**
	 * Reference Id For Key to be revoked
	 */
	@ApiModelProperty(notes = "Reference ID", example = "1001_1001", required = true)
    private String referenceId;

	/**
	 * Disable auto generation of key.
	 */
	@ApiModelProperty(notes = "Flag to stop auto generation of new key pair", example = "false", required = false)
	private Boolean disableAutoGen;

}
