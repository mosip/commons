package io.mosip.kernel.clientcrypto.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TpmSignVerifyResponseDto {

    private boolean verified;
}
