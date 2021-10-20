package io.mosip.commons.khazana.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ObjectDto {

    private String source;
    private String process;
    private String objectName;
    private Date lastModified;
}
