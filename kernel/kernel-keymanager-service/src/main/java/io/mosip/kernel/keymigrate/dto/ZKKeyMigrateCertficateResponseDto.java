package io.mosip.kernel.keymigrate.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response class for ZK temporary key.
 * 
 * @author Mahammed Taheer
 * @since 1.1.6
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Class representing a Certificate Response for ZK migration.")
public class ZKKeyMigrateCertficateResponseDto {

    /**
	 * Field for certificate
	 */
	@ApiModelProperty(notes = "X509 self-signed certificate", required = false)
    private String certificate;
    
    /**
	 * Key expiry time
	 */
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	@ApiModelProperty(notes = "Timestamp of public key", required = true)
	private LocalDateTime timestamp;

}
