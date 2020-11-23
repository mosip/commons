package io.mosip.kernel.partnercertservice.dto;

import javax.validation.constraints.NotBlank;

import io.mosip.kernel.keymanagerservice.constant.KeymanagerConstant;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Partner Certificates Verify Trust Request DTO.
 * 
 * @author Mahammed Taheer
 * @since 1.1.2
*/

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Model representing request to verify Partner certificate trust.")
public class CertificateTrustRequestDto {
    
    /**
	 * Certificate Data of Partner.
	 */
	@ApiModelProperty(notes = "X509 Certificate Data", required = true)
	@NotBlank(message = KeymanagerConstant.INVALID_REQUEST)
	String certificateData;
	
    /**
	 * Partner Type.
	 */
	@ApiModelProperty(notes = "Partner Domain", required = true)
	@NotBlank(message = KeymanagerConstant.INVALID_REQUEST)
	String partnerDomain;
}