package io.mosip.kernel.signature.dto;

import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author Mahammed Taheer
 * @since 1.2.0-rc1-SNAPSHOT
 *
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JWTSignatureRequestDto {

    @NotBlank
    @ApiModelProperty(notes = "Base64 encoded JSON Data to sign", example = "ewogICAiYW55S2V5IjogIlRlc3QgSnNvbiIKfQ", required = true)
	private String dataToSign;

	/**
	 * Application id of decrypting module
	 */
	@ApiModelProperty(notes = "Application id to be used for signing", example = "KERNEL", required = false)
	private String applicationId;

	/**
	 * Refrence Id
	 */
	@ApiModelProperty(notes = "Refrence Id", example = "SIGN", required = false)
	private String referenceId;

	/**
	 * Flag to include payload in  JWT Signature Header
	 */
	@ApiModelProperty(notes = "Flag to include payload in  JWT Signature Header.", example = "false", required = false)
	private Boolean includePayload;

	/**
	 * Flag to include certificate in  JWT Signature Header
	 */
	@ApiModelProperty(notes = "Flag to include certificate in  JWT Signature Header.", example = "false", required = false)
	private Boolean includeCertificate;

	/**
	 * Flag to include certificate hash in JWT Signature Header
	 */
	@ApiModelProperty(notes = "Flag to include certificate hash(sha256) in  JWT Signature Header.", example = "false", required = false)
	private Boolean includeCertHash;

	/**
	 * Certificate URL to include in JWT Signature Header
	 */
	@ApiModelProperty(notes = "Flag to include certificate hash(sha256) in  JWT Signature Header.", required = false)
	private String certificateUrl;

}
