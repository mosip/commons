package io.mosip.commons.packet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientPublicKeyResponseDto {

    private String signingPublicKey;
    private String encryptionPublicKey;
}