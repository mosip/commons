/**
 * 
 */
package io.mosip.kernel.core.cbeffutil.entity;

import io.mosip.kernel.core.cbeffutil.jaxbclasses.VersionType;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Major/minor version pair for a CBEFF BIR or CBEFF specification.
 * <p>
 * Contract: values are non-negative integers; {@code 0} means unset when
 * converting to JAXB. Use {@link BIRVersionBuilder} to construct.
 * </p>
 *
 * @author Ramadurai Pandian
 */
@Data
@NoArgsConstructor
public class BIRVersion {

	/**
	 * Minor version component; {@code 0} if unset.
	 */
	private int minor;
	/**
	 * Major version component; {@code 0} if unset.
	 */
	private int major;

	/**
	 * Builds a version from the given builder.
	 *
	 * @param birBuilder never-null builder
	 */
	public BIRVersion(BIRVersionBuilder birBuilder) {
		this.major = birBuilder.major;
		this.minor = birBuilder.minor;
	}

	/**
	 * @return the minor
	 */
	public int getMinor() {
		return minor;
	}

	/**
	 * @return the major
	 */
	public int getMajor() {
		return major;
	}

	/**
	 * Fluent builder for {@link BIRVersion}.
	 */
	public static class BIRVersionBuilder {
		private int minor;
		private int major;

		/**
		 * Sets the minor version component.
		 *
		 * @param minor non-negative minor version
		 * @return this builder
		 */
		public BIRVersionBuilder withMinor(int minor) {
			this.minor = minor;
			return this;
		}

		/**
		 * Sets the major version component.
		 *
		 * @param major non-negative major version
		 * @return this builder
		 */
		public BIRVersionBuilder withMajor(int major) {
			this.major = major;
			return this;
		}

		/**
		 * Builds a {@link BIRVersion} from the current state.
		 *
		 * @return never-null version
		 */
		public BIRVersion build() {
			return new BIRVersion(this);
		}
	}

	/**
	 * Converts this instance to the JAXB {@link VersionType}.
	 *
	 * @return never-null JAXB version; components omitted when {@code <= 0}
	 */
	public VersionType toVersion() {
		VersionType version = new VersionType();
		if (getMinor() > 0)
			version.setMinor(getMinor());
		if (getMajor() > 0)
			version.setMajor(getMajor());
		return version;
	}
}
