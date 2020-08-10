package io.mosip.commons.packet.dto;

import io.mosip.kernel.biometrics.constant.BiometricType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode
public class BiometricRequestDto {

    private String id;
    private String biometricSchemaField;
    private List<BiometricType> modalities;
    private String source;
    private String process;
    private boolean bypassCache;
}
