package io.mosip.kernel.biometrics.entities;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Entry", propOrder = { "key", "value" })
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Entry implements Serializable {

    @XmlElement(name = "Key")
    protected String key;
    @XmlElement(name = "Value")
    protected String value;
}
