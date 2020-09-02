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
@ApiModel(description = "Model representing a data for encrypt/decrypt")
public class TpmCryptoRequestDto {

    /**
     * data to encrypt/decrypt.
     */
    @ApiModelProperty(notes = "Data to Encrypt/Decrypt", example = "Any String", required = true)
    @NotBlank(message = KeymanagerConstant.INVALID_REQUEST)
    private String value;
}
