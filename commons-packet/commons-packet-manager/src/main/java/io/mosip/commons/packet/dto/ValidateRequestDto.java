package io.mosip.commons.packet.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ValidateRequestDto {

    @NotBlank
    private String data;

    private String signature;

    private String timestamp;
}
