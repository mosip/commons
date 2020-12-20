package io.mosip.kernel.zkcryptoservice.dto;


import java.util.List;
import javax.validation.constraints.NotBlank;

import io.mosip.kernel.keymanagerservice.constant.KeymanagerConstant;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Zero Knowledge Encrypt/Decrypt Request DTO.
 * 
 * @author Mahammed Taheer
 * @since 1.1.2
*/

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Model representing request a list of data attributes for encrypt/decrypt")
public class ZKCryptoRequestDto {
    
    /**
	 * Id used in zero knowledge.
	 */
	@ApiModelProperty(notes = "Resident ID(VID/UIN)", required = true)
	@NotBlank(message = KeymanagerConstant.INVALID_REQUEST)
	String id;
    
    /**
	 * zero knowledge Data Attributes to encrypt/decrypt.
	 */
	@ApiModelProperty(notes = "ZK Data Attributes", required = true)
	List<CryptoDataDto> zkDataAttributes;

}