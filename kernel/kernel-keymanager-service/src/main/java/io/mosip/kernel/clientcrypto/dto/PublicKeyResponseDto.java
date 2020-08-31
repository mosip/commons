package io.mosip.kernel.clientcrypto.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Class representing a Public Key Response")
public class PublicKeyResponseDto {

    /**
     * Field for public key
     */
    @ApiModelProperty(notes = "Public key in BASE64 encoding format", required = true)
    private String publicKey;
}
