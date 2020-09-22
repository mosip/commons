package io.mosip.commons.packetmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class FieldResponseDto {

    Map<String, String> fields;
}
