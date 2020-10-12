package io.mosip.kernel.partnercertservice.dto;

import javax.validation.constraints.NotBlank;

import io.mosip.kernel.keymanagerservice.constant.KeymanagerConstant;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CA/Sub-CA Certificate Request DTO.
 * 
 * @author Mahammed Taheer
 * @since 1.1.2
*/

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Model representing request to upload CA/Sub-CA certificates.")
public class CACertificateRequestDto {
    
    /**
	 * Certificate Data of CA or Sub-CA.
	 */
	@ApiModelProperty(notes = "X509 Certificate Data", required = true)
	@NotBlank(message = KeymanagerConstant.INVALID_REQUEST)
	String certificateData;

	 /**
	 * Certificate Data of CA or Sub-CA.
	 */
	@ApiModelProperty(notes = "Partner Domain", required = true)
	@NotBlank(message = KeymanagerConstant.INVALID_REQUEST)
	String partnerDomain;
}