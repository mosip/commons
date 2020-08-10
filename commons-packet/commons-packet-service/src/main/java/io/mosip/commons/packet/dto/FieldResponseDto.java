package io.mosip.commons.packet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode
@AllArgsConstructor
public class FieldResponseDto {

    Map<String, String> fields;
}
