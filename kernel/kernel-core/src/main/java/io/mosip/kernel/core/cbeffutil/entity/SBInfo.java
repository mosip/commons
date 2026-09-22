/**
 * 
 */
package io.mosip.kernel.core.cbeffutil.entity;

import io.mosip.kernel.core.cbeffutil.jaxbclasses.RegistryIDType;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.SBInfoType;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CBEFF signature-block metadata (format registry id).
 * <p>
 * Contract: optional companion to {@link BIR#getSb()}. {@code format} may be
 * null. Use {@link SBInfoBuilder} to construct and {@link #toSBInfoType()} to
 * marshal.
 * </p>
 *
 * @author Ramadurai Pandian
 */
@Data
@NoArgsConstructor
public class SBInfo {

	/**
	 * Format registry identifier for the signature block; may be null.
	 */
	private RegistryIDType format;

	/**
	 * Builds signature-block info from the given builder.
	 *
	 * @param sBInfoBuilder never-null builder
	 */
	public SBInfo(SBInfoBuilder sBInfoBuilder) {
		this.format = sBInfoBuilder.format;
	}

	/**
	 * Returns the signature-block format registry id.
	 *
	 * @return format; may be null
	 */
	public RegistryIDType getFormat() {
		return format;
	}

	/**
	 * Fluent builder for {@link SBInfo}.
	 */
	public static class SBInfoBuilder {
		private RegistryIDType format;

		/**
		 * Sets the format registry identifier.
		 *
		 * @param format format; may be null
		 * @return this builder
		 */
		public SBInfoBuilder setFormatOwner(RegistryIDType format) {
			this.format = format;
			return this;
		}

		/**
		 * Builds an {@link SBInfo} from the current state.
		 *
		 * @return never-null SBInfo
		 */
		public SBInfo build() {
			return new SBInfo(this);
		}
	}

	/**
	 * Converts this instance to the JAXB {@link SBInfoType}.
	 *
	 * @return never-null JAXB type; format omitted when null
	 */
	public SBInfoType toSBInfoType() {
		SBInfoType sBInfoType = new SBInfoType();
		if (getFormat() != null) {
			sBInfoType.setFormat(getFormat());
		}
		return sBInfoType;
	}

}
