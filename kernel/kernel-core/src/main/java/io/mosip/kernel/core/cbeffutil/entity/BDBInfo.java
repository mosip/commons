/**
 * 
 */
package io.mosip.kernel.core.cbeffutil.entity;

import java.time.LocalDateTime;
import java.util.List;

import io.mosip.kernel.core.cbeffutil.jaxbclasses.BDBInfoType;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.ProcessedLevelType;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.PurposeType;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.QualityType;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.RegistryIDType;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.SingleType;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CBEFF Biometric Data Block (BDB) metadata for a {@link BIR}.
 * <p>
 * Contract: describes type, subtype, quality, purpose, validity window, and
 * algorithm registry ids. All fields may be null. Use {@link BDBInfoBuilder}
 * to construct and {@link #toBDBInfo()} to marshal to JAXB.
 * </p>
 *
 * @author Ramadurai Pandian
 * @see BIR
 */
@Data
@NoArgsConstructor
public class BDBInfo {

	/**
	 * Optional challenge-response bytes used with encrypted BDBs; may be null.
	 */
	private byte[] challengeResponse;
	/**
	 * Unique index of this BDB within the CBEFF; may be null.
	 */
	private String index;
	/**
	 * Whether the BDB payload is encrypted; may be null if unspecified.
	 */
	private Boolean encryption;
	/**
	 * UTC creation timestamp; may be null.
	 */
	private LocalDateTime creationDate;
	/**
	 * Inclusive start of validity; may be null.
	 */
	private LocalDateTime notValidBefore;
	/**
	 * Inclusive end of validity; may be null.
	 */
	private LocalDateTime notValidAfter;
	/**
	 * Biometric types such as Finger, Iris, Face; may be null or empty.
	 */
	private List<SingleType> type;
	/**
	 * Subtypes such as Left or Right Index; may be null or empty.
	 */
	private List<String> subtype;
	/**
	 * Processed level (raw, intermediate, processed); may be null.
	 */
	private ProcessedLevelType level;
	/**
	 * Product registry identifier; may be null.
	 */
	private RegistryIDType product;
	/**
	 * Capture or match purpose; may be null.
	 */
	private PurposeType purpose;
	/**
	 * Quality score and algorithm; may be null.
	 */
	private QualityType quality;
	/**
	 * BDB format registry identifier; may be null.
	 */
	private RegistryIDType format;
	/**
	 * Capture-device registry identifier; may be null.
	 */
	private RegistryIDType captureDevice;
	/**
	 * Feature-extraction algorithm registry identifier; may be null.
	 */
	private RegistryIDType featureExtractionAlgorithm;
	/**
	 * Comparison algorithm registry identifier; may be null.
	 */
	private RegistryIDType comparisonAlgorithm;
	/**
	 * Compression algorithm registry identifier; may be null.
	 */
	private RegistryIDType compressionAlgorithm;

	/**
	 * Builds BDB metadata from the given builder.
	 *
	 * @param bDBInfoBuilder never-null builder
	 */
	public BDBInfo(BDBInfoBuilder bDBInfoBuilder) {
		this.challengeResponse = bDBInfoBuilder.challengeResponse;
		this.index = bDBInfoBuilder.index;
		this.format = bDBInfoBuilder.format;
		this.encryption = bDBInfoBuilder.encryption;
		this.creationDate = bDBInfoBuilder.creationDate;
		this.notValidBefore = bDBInfoBuilder.notValidBefore;
		this.notValidAfter = bDBInfoBuilder.notValidAfter;
		this.type = bDBInfoBuilder.type;
		this.subtype = bDBInfoBuilder.subtype;
		this.level = bDBInfoBuilder.level;
		this.product = bDBInfoBuilder.product;
		this.purpose = bDBInfoBuilder.purpose;
		this.quality = bDBInfoBuilder.quality;
		this.captureDevice = bDBInfoBuilder.captureDevice;
		this.featureExtractionAlgorithm = bDBInfoBuilder.featureExtractionAlgorithm;
		this.comparisonAlgorithm = bDBInfoBuilder.comparisonAlgorithm;
		this.compressionAlgorithm = bDBInfoBuilder.compressionAlgorithm;
	}

	public byte[] getChallengeResponse() {
		return challengeResponse;
	}

	public String getIndex() {
		return index;
	}

	public RegistryIDType getFormat() {
		return format;
	}

	public Boolean getEncryption() {
		return encryption;
	}

	public LocalDateTime getCreationDate() {
		return creationDate;
	}

	public LocalDateTime getNotValidBefore() {
		return notValidBefore;
	}

	public LocalDateTime getNotValidAfter() {
		return notValidAfter;
	}

	public List<SingleType> getType() {
		return type;
	}

	public List<String> getSubtype() {
		return subtype;
	}

	public ProcessedLevelType getLevel() {
		return level;
	}

	public RegistryIDType getProduct() {
		return product;
	}

	public PurposeType getPurpose() {
		return purpose;
	}

	public QualityType getQuality() {
		return quality;
	}

	/**
	 * @return the captureDevice
	 */
	public RegistryIDType getCaptureDevice() {
		return captureDevice;
	}

	/**
	 * @return the featureExtractionAlgorithm
	 */
	public RegistryIDType getFeatureExtractionAlgorithm() {
		return featureExtractionAlgorithm;
	}

	/**
	 * @return the comparisonAlgorithm
	 */
	public RegistryIDType getComparisonAlgorithm() {
		return comparisonAlgorithm;
	}

	/**
	 * @return the compressionAlgorithm
	 */
	public RegistryIDType getCompressionAlgorithm() {
		return compressionAlgorithm;
	}

	/**
	 * Fluent builder for {@link BDBInfo}.
	 * <p>
	 * Contract: each {@code with*} method returns {@code this}. Fields default
	 * to null.
	 * </p>
	 */
	public static class BDBInfoBuilder {
		private byte[] challengeResponse;
		private String index;
		private RegistryIDType format;
		private Boolean encryption;
		private LocalDateTime creationDate;
		private LocalDateTime notValidBefore;
		private LocalDateTime notValidAfter;
		private List<SingleType> type;
		private List<String> subtype;
		private ProcessedLevelType level;
		private RegistryIDType product;
		private PurposeType purpose;
		private QualityType quality;
		private RegistryIDType captureDevice;
		private RegistryIDType featureExtractionAlgorithm;
		private RegistryIDType comparisonAlgorithm;
		private RegistryIDType compressionAlgorithm;

		/**
		 * Sets challenge-response bytes for encrypted BDBs.
		 *
		 * @param challengeResponse bytes; may be null
		 * @return this builder
		 */
		public BDBInfoBuilder withChallengeResponse(byte[] challengeResponse) {
			this.challengeResponse = challengeResponse;
			return this;
		}

		public BDBInfoBuilder withIndex(String index) {
			this.index = index;
			return this;
		}

		public BDBInfoBuilder withFormat(RegistryIDType format) {
			this.format = format;
			return this;
		}

		public BDBInfoBuilder withEncryption(Boolean encryption) {
			this.encryption = encryption;
			return this;
		}

		public BDBInfoBuilder withCreationDate(LocalDateTime creationDate) {
			this.creationDate = creationDate;
			return this;
		}

		public BDBInfoBuilder withNotValidBefore(LocalDateTime notValidBefore) {
			this.notValidBefore = notValidBefore;
			return this;
		}

		public BDBInfoBuilder withNotValidAfter(LocalDateTime notValidAfter) {
			this.notValidAfter = notValidAfter;
			return this;
		}

		public BDBInfoBuilder withType(List<SingleType> type) {
			this.type = type;
			return this;
		}

		public BDBInfoBuilder withSubtype(List<String> subtype) {
			this.subtype = subtype;
			return this;
		}

		public BDBInfoBuilder withLevel(ProcessedLevelType level) {
			this.level = level;
			return this;
		}

		public BDBInfoBuilder withProduct(RegistryIDType product) {
			this.product = product;
			return this;
		}

		public BDBInfoBuilder withPurpose(PurposeType purpose) {
			this.purpose = purpose;
			return this;
		}

		public BDBInfoBuilder withQuality(QualityType quality) {
			this.quality = quality;
			return this;
		}

		public BDBInfo build() {
			return new BDBInfo(this);
		}

		public BDBInfoBuilder withCaptureDevice(RegistryIDType captureDevice) {
			this.captureDevice = captureDevice;
			return this;
		}

		public BDBInfoBuilder withFeatureExtractionAlgorithm(RegistryIDType featureExtractionAlgorithm) {
			this.featureExtractionAlgorithm = featureExtractionAlgorithm;
			return this;
		}

		public BDBInfoBuilder withComparisonAlgorithm(RegistryIDType comparisonAlgorithm) {
			this.comparisonAlgorithm = comparisonAlgorithm;
			return this;
		}

		public BDBInfoBuilder withCompressionAlgorithm(RegistryIDType compressionAlgorithm) {
			this.compressionAlgorithm = compressionAlgorithm;
			return this;
		}

	}

	/**
	 * Converts this instance to the JAXB {@link BDBInfoType}.
	 *
	 * @return never-null JAXB type; null fields omitted
	 */
	public BDBInfoType toBDBInfo() {
		BDBInfoType bDBInfoType = new BDBInfoType();
		challengeIndexFormatPopolation(bDBInfoType);
		bdbTimePopolation(bDBInfoType);
		typeSubTypeLevelPopolation(bDBInfoType);
		featureExtractionComparissionAlgoPopolation(bDBInfoType);
		if (getEncryption() != null) {
			bDBInfoType.setEncryption(getEncryption());
		}
		if (getProduct() != null) {
			bDBInfoType.setProduct(getProduct());
		}
		if (getFormat() != null) {
			bDBInfoType.setFormat(getFormat());
		}
		if (getPurpose() != null) {
			bDBInfoType.setPurpose(getPurpose());
		}
		if (getQuality() != null) {
			bDBInfoType.setQuality(getQuality());
		}
		if (getCaptureDevice() != null) {
			bDBInfoType.setCaptureDevice(getCaptureDevice());
		}
		return bDBInfoType;
	}

	/**
	 * Copies feature-extraction and comparison algorithm ids onto the JAXB type.
	 *
	 * @param bDBInfoType never-null target JAXB type
	 */
	private void featureExtractionComparissionAlgoPopolation(BDBInfoType bDBInfoType) {
		if (getFeatureExtractionAlgorithm() != null) {
			bDBInfoType.setFeatureExtractionAlgorithm(getFeatureExtractionAlgorithm());
		}
		if (getComparisonAlgorithm() != null) {
			bDBInfoType.setComparisonAlgorithm(getComparisonAlgorithm());
		}
	}

	/**
	 * Copies type, subtype, and processed level onto the JAXB type.
	 *
	 * @param bDBInfoType never-null target JAXB type
	 */
	private void typeSubTypeLevelPopolation(BDBInfoType bDBInfoType) {
		if (getType() != null) {
			bDBInfoType.setType(getType());
		}
		if (getSubtype() != null) {
			bDBInfoType.setSubtype(getSubtype());
		}
		if (getLevel() != null) {
			bDBInfoType.setLevel(getLevel());
		}
	}

	/**
	 * Copies challenge response, index, and format onto the JAXB type.
	 *
	 * @param bDBInfoType never-null target JAXB type
	 */
	private void challengeIndexFormatPopolation(BDBInfoType bDBInfoType) {
		if (getChallengeResponse() != null && getChallengeResponse().length > 0) {
			bDBInfoType.setChallengeResponse(getChallengeResponse());
		}
		if (getIndex() != null && getIndex().length() > 0) {
			bDBInfoType.setIndex(getIndex());
		}
		if (getFormat() != null) {
			bDBInfoType.setFormat(getFormat());
		}
	}

	/**
	 * Copies creation and validity timestamps onto the JAXB type.
	 *
	 * @param bDBInfoType never-null target JAXB type
	 */
	private void bdbTimePopolation(BDBInfoType bDBInfoType) {
		if (getCreationDate() != null) {
			bDBInfoType.setCreationDate(getCreationDate());
		}
		if (getNotValidBefore() != null) {
			bDBInfoType.setNotValidBefore(getNotValidBefore());
		}
		if (getNotValidAfter() != null) {
			bDBInfoType.setNotValidAfter(getNotValidAfter());
		}
	}
}
