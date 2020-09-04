package io.mosip.kernel.clientcrypto.dto;

import io.mosip.kernel.keymanagerservice.constant.KeymanagerConstant;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Model representing to fetch signing public key from TPM request")
public class PublicKeyRequestDto {

    /**
     * server profile name
     */
    @ApiModelProperty(notes = "Server Profile (Prod / QA / Dev)", required = true)
    @NotBlank(message = KeymanagerConstant.INVALID_REQUEST)
    String serverProfile;


}
