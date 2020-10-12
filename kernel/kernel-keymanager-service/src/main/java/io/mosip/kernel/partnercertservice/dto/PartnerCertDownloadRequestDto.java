package io.mosip.kernel.partnercertservice.dto;

import javax.validation.constraints.NotBlank;

import io.mosip.kernel.keymanagerservice.constant.KeymanagerConstant;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Partner Certificate Download Request DTO.
 * 
 * @author Mahammed Taheer
 * @since 1.1.2
*/

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Model representing request to download partner certificates.")
public class PartnerCertDownloadRequestDto {
    
    /**
	 * Certificate ID of Partner.
	 */
	@ApiModelProperty(notes = "Partner Certificate ID", required = true)
	@NotBlank(message = KeymanagerConstant.INVALID_REQUEST)
	String partnerCertId;
}