package io.mosip.kernel.core.cbeffutil.entity;

import java.util.List;

import javax.xml.bind.JAXBElement;

import io.mosip.kernel.core.cbeffutil.jaxbclasses.BIRType;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CBEFF Biometric Information Record (BIR) with a fluent builder.
 * <p>
 * Contract: represents one biometric sample or template in MOSIP CBEFF XML.
 * {@code bdb} is the biometric data block (image or template bytes) and may be
 * null for metadata-only records. Use {@link BIRBuilder} to construct; call
 * {@link #toBIRType(BIR)} when marshalling to JAXB. Deprecated CBEFF path
 * since 1.1.7; kept for downstream modules.
 * </p>
 *
 * @author Ramadurai Pandian
 * @see BDBInfo
 * @see BIRInfo
 */
@Data
@NoArgsConstructor
public class BIR {

	/**
	 * BIR schema version; may be null.
	 */
	private BIRVersion version;
	/**
	 * CBEFF specification version; may be null.
	 */
	private BIRVersion cbeffversion;
	/**
	 * Record-level BIR metadata; may be null.
	 */
	private BIRInfo birInfo;
	/**
	 * Biometric data block metadata; may be null.
	 */
	private BDBInfo bdbInfo;
	/**
	 * Biometric data block bytes (image or template); may be null.
	 */
	private byte[] bdb;
	/**
	 * Signature block bytes; may be null.
	 */
	private byte[] sb;
	/**
	 * Signature-block metadata; may be null.
	 */
	private SBInfo sbInfo;
	/**
	 * Additional JAXB any-elements; may be null or empty.
	 */
	private List<JAXBElement<String>> element;

	/**
	 * Builds a BIR from the given builder state.
	 *
	 * @param birBuilder never-null builder
	 */
	public BIR(BIRBuilder birBuilder) {
		this.version = birBuilder.version;
		this.cbeffversion = birBuilder.cbeffversion;
		this.birInfo = birBuilder.birInfo;
		this.bdbInfo = birBuilder.bdbInfo;
		this.bdb = birBuilder.bdb;
		this.sb = birBuilder.sb;
		this.sbInfo = birBuilder.sbInfo;
		this.element = birBuilder.element;
	}

	/**
	 * @return the element
	 */
	public List<JAXBElement<String>> getElement() {
		return element;
	}

	/**
	 * @return the version
	 */
	public BIRVersion getVersion() {
		return version;
	}

	/**
	 * @return the cbeffversion
	 */
	public BIRVersion getCbeffversion() {
		return cbeffversion;
	}

	/**
	 * @return the birInfo
	 */
	public BIRInfo getBirInfo() {
		return birInfo;
	}

	/**
	 * @return the bdbInfo
	 */
	public BDBInfo getBdbInfo() {
		return bdbInfo;
	}

	/**
	 * @return the bdb
	 */
	public byte[] getBdb() {
		return bdb;
	}

	/**
	 * @return the sb
	 */
	public byte[] getSb() {
		return sb;
	}

	/**
	 * @return the sbInfo
	 */
	public SBInfo getSbInfo() {
		return sbInfo;
	}

	/**
	 * Fluent builder for {@link BIR}.
	 * <p>
	 * Contract: each {@code with*} method returns {@code this}. Call
	 * {@link #build()} when all desired fields are set. Fields default to null.
	 * </p>
	 */
	public static class BIRBuilder {
		private BIRVersion version;
		private BIRVersion cbeffversion;
		private BIRInfo birInfo;
		private BDBInfo bdbInfo;
		private byte[] bdb;
		private byte[] sb;
		private SBInfo sbInfo;
		private List<JAXBElement<String>> element;

		/**
		 * Sets additional JAXB any-elements.
		 *
		 * @param list elements; may be null
		 * @return this builder
		 */
		public BIRBuilder withElement(List<JAXBElement<String>> list) {
			this.element = list;
			return this;
		}

		/**
		 * Sets the BIR schema version.
		 *
		 * @param version version; may be null
		 * @return this builder
		 */
		public BIRBuilder withVersion(BIRVersion version) {
			this.version = version;
			return this;
		}

		/**
		 * Sets the CBEFF specification version.
		 *
		 * @param cbeffversion version; may be null
		 * @return this builder
		 */
		public BIRBuilder withCbeffversion(BIRVersion cbeffversion) {
			this.cbeffversion = cbeffversion;
			return this;
		}

		/**
		 * Sets record-level BIR metadata.
		 *
		 * @param birInfo metadata; may be null
		 * @return this builder
		 */
		public BIRBuilder withBirInfo(BIRInfo birInfo) {
			this.birInfo = birInfo;
			return this;
		}

		/**
		 * Sets biometric data block metadata.
		 *
		 * @param bdbInfo metadata; may be null
		 * @return this builder
		 */
		public BIRBuilder withBdbInfo(BDBInfo bdbInfo) {
			this.bdbInfo = bdbInfo;
			return this;
		}

		/**
		 * Sets biometric data block bytes.
		 *
		 * @param bdb image or template bytes; may be null
		 * @return this builder
		 */
		public BIRBuilder withBdb(byte[] bdb) {
			this.bdb = bdb;
			return this;
		}

		/**
		 * Sets signature block bytes.
		 *
		 * @param sb signature bytes; may be null
		 * @return this builder
		 */
		public BIRBuilder withSb(byte[] sb) {
			this.sb = sb;
			return this;
		}

		/**
		 * Sets signature-block metadata.
		 *
		 * @param sbInfo metadata; may be null
		 * @return this builder
		 */
		public BIRBuilder withSbInfo(SBInfo sbInfo) {
			this.sbInfo = sbInfo;
			return this;
		}

		/**
		 * Builds an immutable-style {@link BIR} from the current state.
		 *
		 * @return never-null BIR
		 */
		public BIR build() {
			return new BIR(this);
		}

	}

	/**
	 * Converts this BIR graph to the JAXB {@link BIRType} used for XML
	 * marshalling.
	 *
	 * @param bir never-null source BIR (typically {@code this})
	 * @return never-null JAXB BIR; nested fields omitted when null
	 */
	public BIRType toBIRType(BIR bir) {
		BIRType bIRType = new BIRType();
		if (bir.getVersion() != null)
			bIRType.setVersion(bir.getVersion().toVersion());
		if (bir.getCbeffversion() != null)
			bIRType.setCBEFFVersion(bir.getCbeffversion().toVersion());
		bIRType.setBDB(getBdb());
		bIRType.setSB(getSb());
		if (bir.getBirInfo() != null)
			bIRType.setBIRInfo(bir.getBirInfo().toBIRInfo());
		if (bir.getBdbInfo() != null)
			bIRType.setBDBInfo(bir.getBdbInfo().toBDBInfo());
		if (bir.getSbInfo() != null)
			bIRType.setSBInfo(bir.getSbInfo().toSBInfoType());
		if (bir.getElement() != null)
			bIRType.setAny(getElement());
		return bIRType;
	}

}
