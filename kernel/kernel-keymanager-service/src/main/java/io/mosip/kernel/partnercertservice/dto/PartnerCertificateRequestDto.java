package io.mosip.kernel.partnercertservice.dto;

import javax.validation.constraints.NotBlank;

import io.mosip.kernel.keymanagerservice.constant.KeymanagerConstant;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Partner Certificates Request DTO.
 * 
 * @author Mahammed Taheer
 * @since 1.1.2
*/

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Model representing request to upload Partner certificates.")
public class PartnerCertificateRequestDto {
    
    /**
	 * Certificate Data of Partner.
	 */
	@ApiModelProperty(notes = "X509 Certificate Data", required = true)
	@NotBlank(message = KeymanagerConstant.INVALID_REQUEST)
	String certificateData;
	
	/**
	 * Certificate Data of Partner.
	 */
	@ApiModelProperty(notes = "Organization Name", required = true)
	@NotBlank(message = KeymanagerConstant.INVALID_REQUEST)
    String organizationName;
    
    /**
	 * Partner Type.
	 */
	@ApiModelProperty(notes = "Partner Domain", required = true)
	@NotBlank(message = KeymanagerConstant.INVALID_REQUEST)
	String partnerDomain;
}