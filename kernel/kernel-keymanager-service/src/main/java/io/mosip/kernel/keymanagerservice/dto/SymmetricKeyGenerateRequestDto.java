package io.mosip.kernel.keymanagerservice.dto;

import javax.validation.constraints.NotBlank;

import io.mosip.kernel.keymanagerservice.constant.KeymanagerConstant;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Symmetric Key Generate Model
 * 
 * @author Mahammed Taheer
 *
 * @since 1.1.4
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Model representing a Symmetric Key Generation Request")
public class SymmetricKeyGenerateRequestDto {

    	
	/**
	 * Application Id For Generating Symmetric Key
	 */
	@ApiModelProperty(notes = "Application ID", example = "KERNEL", required = true)
	@NotBlank(message = KeymanagerConstant.INVALID_REQUEST)
	private String applicationId;
	
	/**
	 * Reference Id For Generating Symmetric Key
	 */
    @ApiModelProperty(notes = "Reference ID", example = "IDENTITY_CACHE", required = true)
    @NotBlank(message = KeymanagerConstant.INVALID_REQUEST)
    private String referenceId;

    /**
	 * Force Flag
	 */
	@ApiModelProperty(notes = "Flag to force new generation of Symmetric Key by invalidating existing keys.", example = "false", required = false)
	private Boolean force;

}

