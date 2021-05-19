package io.mosip.kernel.keymigrate.dto;
import javax.validation.constraints.NotBlank;

import io.mosip.kernel.keymanagerservice.constant.KeymanagerConstant;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ZK Encrypted Key Data for migration.
 * 
 * @author Mahammed Taheer
 * @since 1.1.6
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "ZK Encrypted Key Data for migration")
public class ZKKeyDataDto {


    /**
	 * Key Index for the encrypted key data.
	 */
	@ApiModelProperty(notes = "Key Index", example = "index", required = true)
	@NotBlank(message = KeymanagerConstant.INVALID_REQUEST)
	private int keyIndex;

	/**
	 * ZK Encrypted Key data.
	 */
	@ApiModelProperty(notes = "Encrypted Key Data for ZK.", example = "Encrypted String", required = true)
	@NotBlank(message = KeymanagerConstant.INVALID_REQUEST)
	private String encryptedKeyData;
}
