package io.mosip.commons.packet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TpmSignVerifyRequestDto {

    
    private String data;

    /**
     * Signature in BASE64 encoding
     */
    private String signature;

    /**
     * public key in BASE64 encoding
     */
    private String publicKey;

    /**
     * Flag to identify TPM or Non-TPM validations
     */
    private boolean isTpm;
}
