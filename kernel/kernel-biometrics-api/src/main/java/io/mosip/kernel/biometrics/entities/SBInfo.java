/**
 * 
 */
package io.mosip.kernel.biometrics.entities;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Ramadurai Pandian
 *
 */
@Data
@JsonDeserialize(builder = SBInfo.SBInfoBuilder.class)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SBInfoType", propOrder = { "format" })
public class SBInfo implements Serializable {

	@XmlElement(name = "Format")
	private RegistryIDType format;

	public SBInfo(SBInfoBuilder sBInfoBuilder) {
		this.format = sBInfoBuilder.format;
	}

	public RegistryIDType getFormat() {
		return format;
	}

	public static class SBInfoBuilder {
		private RegistryIDType format;

		public SBInfoBuilder setFormatOwner(RegistryIDType format) {
			this.format = format;
			return this;
		}

		public SBInfo build() {
			return new SBInfo(this);
		}
	}
}
