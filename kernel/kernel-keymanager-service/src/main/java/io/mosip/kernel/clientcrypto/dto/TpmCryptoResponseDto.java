package io.mosip.kernel.clientcrypto.dto;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Model representing response for encrypt/decrypt request")
public class TpmCryptoResponseDto {

    /**
     * Encrypted / decrypted data
     */
    private String value;

}
