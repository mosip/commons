package io.mosip.kernel.keymanagerservice.dto;

import javax.validation.constraints.NotBlank;

import io.mosip.kernel.keymanagerservice.constant.KeymanagerConstant;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CSR-Request model
 * 
 * @author Mahammed Taheer
 *
 * @since 1.0.10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Model representing a Uploading CA signed Certificate Request")
public class UploadCertificateRequestDto {

    	
	/**
	 * Application Id For Uploading Certificate
	 */
	@ApiModelProperty(notes = "Application ID", example = "KERNEL", required = true)
	@NotBlank(message = KeymanagerConstant.INVALID_REQUEST)
	private String applicationId;
	
	/**
	 * Reference Id For Uploading Certificate
	 */
	@ApiModelProperty(notes = "Reference ID", example = "", required = false)
    private String referenceId;

	/**
	 * Certificate Data
	 */
	@ApiModelProperty(notes = "X509 PEM Encoded Certificate", example = "", required = true)
	@NotBlank(message = KeymanagerConstant.INVALID_REQUEST)
	private String certificateData;

}
