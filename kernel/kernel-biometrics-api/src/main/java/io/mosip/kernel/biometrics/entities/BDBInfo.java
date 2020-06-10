/**
 * 
 */
package io.mosip.kernel.biometrics.entities;

import java.time.LocalDateTime;
import java.util.List;

import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.ProcessedLevelType;
import io.mosip.kernel.biometrics.constant.PurposeType;
import io.mosip.kernel.biometrics.constant.QualityType;
import lombok.Data;


/**
 * @author Ramadurai Pandian
 *
 */
@Data
public class BDBInfo {

	private byte[] challengeResponse;
	private String index;
	private Boolean encryption;
	private LocalDateTime creationDate;
	private LocalDateTime notValidBefore;
	private LocalDateTime notValidAfter;
	private List<BiometricType> type;
	private List<String> subtype;
	private ProcessedLevelType level;
	private RegistryIDType product;
	private PurposeType purpose;
	private QualityType quality;
	private RegistryIDType format;
	private RegistryIDType captureDevice;
	private RegistryIDType featureExtractionAlgorithm;
	private RegistryIDType comparisonAlgorithm;
	private RegistryIDType compressionAlgorithm;

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
	

	public static class BDBInfoBuilder {
		private byte[] challengeResponse;
		private String index;
		private RegistryIDType format;
		private Boolean encryption;
		private LocalDateTime creationDate;
		private LocalDateTime notValidBefore;
		private LocalDateTime notValidAfter;
		private List<BiometricType> type;
		private List<String> subtype;
		private ProcessedLevelType level;
		private RegistryIDType product;
		private PurposeType purpose;
		private QualityType quality;
		private RegistryIDType captureDevice;
		private RegistryIDType featureExtractionAlgorithm;
		private RegistryIDType comparisonAlgorithm;
		private RegistryIDType compressionAlgorithm;

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

		public BDBInfoBuilder withType(List<BiometricType> type) {
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
			//TODO
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

	private void featureExtractionComparissionAlgoPopolation(BDBInfoType bDBInfoType) {
		if (getFeatureExtractionAlgorithm() != null) {
			bDBInfoType.setFeatureExtractionAlgorithm(getFeatureExtractionAlgorithm());
		}
		if (getComparisonAlgorithm() != null) {
			bDBInfoType.setComparisonAlgorithm(getComparisonAlgorithm());
		}
	}

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
