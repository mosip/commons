package io.mosip.kernel.syncdata.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response class for Key Pair Generation.
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Class representing a KeyPair Generator Response")
public class KeyPairGenerateResponseDto {

    /**
	 * Field for certificate
	 */
	@ApiModelProperty(notes = "X509 self-signed certificate", required = false)
    private String certificate;
    
    /**
	 * Field for CSR
	 */
	@ApiModelProperty(notes = "Certificate Signing Request Data", required = false)
	private String certSignRequest;

	/**
	 * Key creation time
	 */
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	@ApiModelProperty(notes = "Timestamp of issuance of certificate", required = true)
	private LocalDateTime issuedAt;

	/**
	 * Key expiry time
	 */
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	@ApiModelProperty(notes = "Timestamp of expiry of certificate", required = true)
    private LocalDateTime expiryAt;
    
    /**
	 * Key expiry time
	 */
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	@ApiModelProperty(notes = "Timestamp of public key", required = true)
	private LocalDateTime timestamp;

}
