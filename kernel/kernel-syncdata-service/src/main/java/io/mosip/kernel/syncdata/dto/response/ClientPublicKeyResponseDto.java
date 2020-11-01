package io.mosip.kernel.syncdata.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "Class representing a Client public key Response")
public class ClientPublicKeyResponseDto {

    private String signingPublicKey;
    private String encryptionPublicKey;
}
