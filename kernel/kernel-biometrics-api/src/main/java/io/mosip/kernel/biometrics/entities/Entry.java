package io.mosip.kernel.biometrics.entities;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

public class Entry implements Serializable {

    @XmlAttribute
	public String key;
    @XmlValue
	public String value;


	public Entry(){}
	public Entry(String tKey, String tValue){
		key = tKey;
		value = tValue;
	}

}
