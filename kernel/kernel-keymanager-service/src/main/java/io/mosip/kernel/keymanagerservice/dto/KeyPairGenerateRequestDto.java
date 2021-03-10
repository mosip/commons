package io.mosip.kernel.keymanagerservice.dto;

import javax.validation.constraints.NotBlank;

import io.mosip.kernel.keymanagerservice.constant.KeymanagerConstant;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Crypto-Manager-Request model
 * 
 * @author Mahammed Taheer
 *
 * @since 1.0.10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Model representing a Keypair Generation Request")
public class KeyPairGenerateRequestDto {

    	
	/**
	 * Application Id For Generating KeyPair
	 */
	@ApiModelProperty(notes = "Application ID", example = "KERNEL", required = true)
	@NotBlank(message = KeymanagerConstant.INVALID_REQUEST)
	private String applicationId;
	
	/**
	 * Reference Id For Generating KeyPair
	 */
	@ApiModelProperty(notes = "Reference ID", example = "", required = false)
    private String referenceId;

    /**
	 * Force Flag
	 */
	@ApiModelProperty(notes = "Flag to force new generation of KeyPair by invalidating existing keys.", example = "false", required = false)
	private Boolean force;

	/**
	 * Common Name For Generating Certificate or CSR
	 */
	@ApiModelProperty(notes = "Common Name (CN)", example = "MOSIP", required = true)
	@NotBlank(message = KeymanagerConstant.INVALID_REQUEST)
	private String commonName;

	/**
	 * Organization Unit For Generating Certificate or CSR
	 */
	@ApiModelProperty(notes = "Organization Unit (OU)", example = "MOSIP-TECH-CENTER", required = false)
	private String organizationUnit;

	/**
	 * Organization For Generating Certificate or CSR
	 */
	@ApiModelProperty(notes = "Organization (O)", example = "IIITB", required = false)
	private String organization;

	/**
	 * Location For Generating Certificate or CSR
	 */
	@ApiModelProperty(notes = "Location (L)", example = "BANGALORE", required = false)
	private String location;

	/**
	 * State For Generating Certificate or CSR
	 */
	@ApiModelProperty(notes = "State (ST)", example = "KA", required = false)
	@NotBlank(message = KeymanagerConstant.INVALID_REQUEST)
	private String state;

	/**
	 * Country For Generating Certificate or CSR
	 */
	@ApiModelProperty(notes = "Country (C)", example = "IN", required = false)
	private String country;
}
