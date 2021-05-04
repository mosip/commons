package io.mosip.kernel.keymigrate.dto;
import javax.validation.constraints.NotBlank;

import io.mosip.kernel.keymanagerservice.constant.KeymanagerConstant;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ZK Encrypted Key Data for migration response message.
 * 
 * @author Mahammed Taheer
 * @since 1.1.6
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "ZK Encrypted Key Data response")
public class ZKKeyResponseDto {


    /**
	 * Key Index for the encrypted key data.
	 */
	@ApiModelProperty(notes = "Key Index", example = "index")
	@NotBlank(message = KeymanagerConstant.INVALID_REQUEST)
	private int keyIndex;

	/**
	 * Migrate response Message.
	 */
	@ApiModelProperty(notes = "Status of Migrated ZK key.", example = "String")
	@NotBlank(message = KeymanagerConstant.INVALID_REQUEST)
	private String statusMessage;
}
