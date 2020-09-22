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
@ApiModel(description = "Model representing response for sign request")
public class TpmSignRequestDto {

    /**
     * Data in BASE64 encoding to sign
     */
    @ApiModelProperty(notes = "Data to sign", required = true)
    @NotBlank(message = KeymanagerConstant.INVALID_REQUEST)
    private String data;
}
