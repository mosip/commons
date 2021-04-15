package io.mosip.kernel.biometrics.entities;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for SingleAnySubtypeType.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * <p>
 * 
 * <pre>
 * &lt;simpleType name="SingleAnySubtypeType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Left"/>
 *     &lt;enumeration value="Right"/>
 *     &lt;enumeration value="Thumb"/>
 *     &lt;enumeration value="IndexFinger"/>
 *     &lt;enumeration value="MiddleFinger"/>
 *     &lt;enumeration value="RingFinger"/>
 *     &lt;enumeration value="LittleFinger"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "SingleAnySubtypeType")
@XmlEnum
public enum SingleAnySubtypeType {

	@XmlEnumValue("Left")
	LEFT("Left"), @XmlEnumValue("Right")
	RIGHT("Right"), @XmlEnumValue("Thumb")
	THUMB("Thumb"), @XmlEnumValue("IndexFinger")
	INDEX_FINGER("IndexFinger"), @XmlEnumValue("MiddleFinger")
	MIDDLE_FINGER("MiddleFinger"), @XmlEnumValue("RingFinger")
	RING_FINGER("RingFinger"), @XmlEnumValue("LittleFinger")
	LITTLE_FINGER("LittleFinger");

	private final String value;

	SingleAnySubtypeType(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static SingleAnySubtypeType fromValue(String v) {
		for (SingleAnySubtypeType c : SingleAnySubtypeType.values()) {
			if (c.value.equalsIgnoreCase(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}

}