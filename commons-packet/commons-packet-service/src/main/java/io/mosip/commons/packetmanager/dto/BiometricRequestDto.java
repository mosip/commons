package io.mosip.commons.packetmanager.dto;

import io.mosip.kernel.biometrics.constant.BiometricType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode
public class BiometricRequestDto {

    private String id;
    private String person;
    private List<BiometricType> modalities;
    private String source;
    private String process;
    private boolean bypassCache;
}
