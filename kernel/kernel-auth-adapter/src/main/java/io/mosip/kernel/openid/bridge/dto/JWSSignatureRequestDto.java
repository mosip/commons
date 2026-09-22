package io.mosip.kernel.openid.bridge.dto;

import jakarta.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for MOSIP keymanager JWT/JWS signing, used to build a
 * private-key JWT {@code client_assertion} for the Keycloak token endpoint
 * (OAuth 2.0 private_key_jwt client authentication).
 *
 * @author Mahammed Taheer
 * @since 1.2.0-SNAPSHOT
 *
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JWSSignatureRequestDto {

	/**
	 * Base64-encoded JSON claims to sign (typically {@code sub}, {@code iss},
	 * {@code aud}, {@code exp}, {@code iat}, {@code jti} for a client assertion).
	 */
    @NotBlank
    @ApiModelProperty(notes = "Base64 encoded JSON Data to sign", example = "ewogICAiYW55S2V5IjogIlRlc3QgSnNvbiIKfQ", required = true)
	private String dataToSign;

	/**
	 * MOSIP keymanager application id that identifies the signing key (for example
	 * {@code KERNEL}).
	 */
	@ApiModelProperty(notes = "Application id to be used for signing", example = "KERNEL", required = false)
	private String applicationId;

	/**
	 * Keymanager reference id for the signing key (for example {@code SIGN}).
	 */
	@ApiModelProperty(notes = "Refrence Id", example = "SIGN", required = false)
	private String referenceId;

	/**
	 * When {@code true}, include the payload in the JWS (detached vs attached JWS).
	 */
	@ApiModelProperty(notes = "Flag to include payload in  JWT Signature Header.", example = "false", required = false)
	private Boolean includePayload;

	/**
	 * When {@code true}, include the signing certificate in the JWS header
	 * ({@code x5c}).
	 */
	@ApiModelProperty(notes = "Flag to include certificate in  JWT Signature Header.", example = "false", required = false)
	private Boolean includeCertificate;

	/**
	 * When {@code true}, include the SHA-256 certificate hash in the JWS header
	 * ({@code x5t#S256}).
	 */
	@ApiModelProperty(notes = "Flag to include certificate hash(sha256) in  JWT Signature Header.", example = "false", required = false)
	private Boolean includeCertHash;

	/**
	 * Certificate URL to place in the JWS header ({@code x5u}), when required.
	 */
	@ApiModelProperty(notes = "Flag to include certificate URL in  JWT Signature Header.", required = false)
	private String certificateUrl;

	/**
	 * When {@code true}, keymanager validates that {@link #dataToSign} decodes to
	 * well-formed JSON before signing.
	 */
	@ApiModelProperty(notes = "Flag to validate inputted JSON to be a valid JSON.", required = false)
	private Boolean validateJson;

	/**
	 * When {@code true}, the payload is Base64URL-encoded as a JWS header
	 * {@code b64} parameter during signing.
	 */
	@ApiModelProperty(notes = "Flag to determine the inputted data to be Base64URL encoded in signature process", required = false)
	private Boolean b64JWSHeaderParam;

	/**
	 * JWS algorithm name. MOSIP keymanager currently documents {@code PS256}.
	 */
	@ApiModelProperty(notes = "JWS Algorithm to use for data signing. Current supported Algorithm PS256.", required = false)
	private String signAlgorithm;
}
