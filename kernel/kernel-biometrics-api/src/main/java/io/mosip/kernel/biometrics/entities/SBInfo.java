/**
 * 
 */
package io.mosip.kernel.biometrics.entities;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
/**
 * @author Ramadurai Pandian
 *
 */
@Data
@JsonDeserialize(builder = SBInfo.SBInfoBuilder.class)
public class SBInfo {

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
