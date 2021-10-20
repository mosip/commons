package io.mosip.commons.packet.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class ValidateRequestDto implements Serializable {

    @NotBlank
    private String data;

    private String signature;

    private String timestamp;
}
