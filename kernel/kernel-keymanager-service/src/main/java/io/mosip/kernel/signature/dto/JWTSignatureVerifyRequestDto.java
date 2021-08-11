
package io.mosip.kernel.signature.dto;

import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author Mahammed Taheer
 * @since 1.2.0-rc1
 *
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JWTSignatureVerifyRequestDto {

    @NotBlank
    @ApiModelProperty(notes = "JWT Signature data to verify", example = "eyJhbGciOiJIU.ewogICAiYW55S2V.5IjogIlRlc3QgSnNvbiIKfQ", required = true)
    private String jwtSignatureData;
    
    @ApiModelProperty(notes = "Base64 encoded actual data used for signing", example = "ewogICAiYW55S2V5IjogIlRlc3QgSnNvbiIKfQ", required = false)
    private String actualData;

	/**
	 * Application id of decrypting module
	 */
	@ApiModelProperty(notes = "Application id to be used for verification", example = "KERNEL", required = false)
	private String applicationId;

	/**
	 * Refrence Id
	 */
	@ApiModelProperty(notes = "Refrence Id", example = "SIGN", required = false)
	private String referenceId;

	/**
	 * Certificate to be use in JWT Signature verification.
	 */
	@ApiModelProperty(notes = "Certificate to be use in JWT Signature verification.", example = "", required = false)
	private String certificateData;

	/**
	 * Flag to validate against trust store.
	 */
	@ApiModelProperty(notes = "Flag to validate against trust store.", example = "false", required = false)
	private Boolean validateTrust;

	/**
	 * Domain to be considered to validate trust store
	 */
	@ApiModelProperty(notes = "Domain to be considered to validate trust store.", example = "", required = false)
	private String domain;

}
