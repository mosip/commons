package io.mosip.kernel.authcodeflowproxy.api.dto;

import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author Mahammed Taheer
 * @since 1.2.0-SNAPSHOT
 *
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JWSSignatureRequestDto {

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
	@ApiModelProperty(notes = "Flag to include certificate URL in  JWT Signature Header.", required = false)
	private String certificateUrl;

	/**
	 * Validate inputted JSON to be valid JSON 
	 */
	@ApiModelProperty(notes = "Flag to validate inputted JSON to be a valid JSON.", required = false)
	private Boolean validateJson;

	/**
	 * Flag to determine the inputted data to be Base64URL encoded in signature process.  
	 */
	@ApiModelProperty(notes = "Flag to determine the inputted data to be Base64URL encoded in signature process", required = false)
	private Boolean b64JWSHeaderParam;

	/**
	 * JWS Algorithm to use for data signing. Current supported Algorithm PS256
	 */
	@ApiModelProperty(notes = "JWS Algorithm to use for data signing. Current supported Algorithm PS256.", required = false)
	private String signAlgorithm;
}
