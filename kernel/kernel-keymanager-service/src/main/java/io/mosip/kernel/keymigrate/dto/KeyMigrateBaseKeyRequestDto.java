package io.mosip.kernel.keymigrate.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.mosip.kernel.keymanagerservice.constant.KeymanagerConstant;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * To insert Base Key in Key Manager using Key Migration.
 * 
 * @author Mahammed Taheer
 * @since 1.1.5
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Model representing a base key migration from one HSM to another")
public class KeyMigrateBaseKeyRequestDto {

    /**
	 * Application Id For Migrating KeyPair
	 */
	@ApiModelProperty(notes = "Application ID", example = "REGISTRATION", required = true)
	@NotBlank(message = KeymanagerConstant.INVALID_REQUEST)
	private String applicationId;
	
	/**
	 * Reference Id For Migrating KeyPair
	 */
	@ApiModelProperty(notes = "Reference ID", example = "", required = true)
    @NotBlank(message = KeymanagerConstant.INVALID_REQUEST)
    private String referenceId;

	/**
	 * Encrypted Private Key data For Migrating KeyPair
	 */
	@ApiModelProperty(notes = "Encrypted Private Key Data", example = " ", required = true)
	@NotBlank(message = KeymanagerConstant.INVALID_REQUEST)
	private String encryptedKeyData;
	
	/**
	 * Certificate For Migrating KeyPair
	 */
	@ApiModelProperty(notes = "Certificate Data", example = "", required = true)
    @NotBlank(message = KeymanagerConstant.INVALID_REQUEST)
    private String certificateData;
    
	/**
	 * Start key validity of the migrating key
	 */
	@ApiModelProperty(notes = "Timestamp of start key validity", example = "2021-01-01T00:00:00.000Z", required = true)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime notBefore;

	/**
	 *  End key validity of the migrating key
	 */
	@ApiModelProperty(notes = "Timestamp of end key validity", example = "2023-01-01T00:00:00.000Z", required = true)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime notAfter;
}
