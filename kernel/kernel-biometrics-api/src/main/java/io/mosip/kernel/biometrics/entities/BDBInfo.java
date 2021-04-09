/**
 * 
 */
package io.mosip.kernel.biometrics.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.ProcessedLevelType;
import io.mosip.kernel.biometrics.constant.PurposeType;
import io.mosip.kernel.biometrics.constant.QualityType;
import io.mosip.kernel.core.cbeffutil.common.DateAdapter;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author Ramadurai Pandian
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BDBInfoType", propOrder = { "challengeResponse", "index", "format", "encryption", "creationDate",
		"notValidBefore", "notValidAfter", "type", "subtype", "level", "product", "captureDevice",
		"featureExtractionAlgorithm", "comparisonAlgorithm", "compressionAlgorithm", "purpose", "quality" })
@Data
@NoArgsConstructor
@JsonDeserialize(builder = BDBInfo.BDBInfoBuilder.class)
public class BDBInfo implements Serializable {

	@XmlElement(name = "ChallengeResponse")
	private byte[] challengeResponse;
	@XmlElement(name = "Index")
	private String index;
	@XmlElement(name = "Format")
	private RegistryIDType format;
	@XmlElement(name = "Encryption")
	private Boolean encryption;
	@XmlElement(name = "CreationDate")
	@XmlSchemaType(name = "dateTime")
	@XmlJavaTypeAdapter(DateAdapter.class)
	private LocalDateTime creationDate;
	@XmlElement(name = "NotValidBefore")
	@XmlSchemaType(name = "dateTime")
	@XmlJavaTypeAdapter(DateAdapter.class)
	private LocalDateTime notValidBefore;
	@XmlElement(name = "NotValidAfter")
	@XmlSchemaType(name = "dateTime")
	@XmlJavaTypeAdapter(DateAdapter.class)
	private LocalDateTime notValidAfter;
	@XmlList
	@XmlElement(name = "Type")
	private List<BiometricType> type;
	@XmlList
	@XmlElement(name = "Subtype")
	private List<String> subtype;
	@XmlElement(name = "Level")
	@XmlSchemaType(name = "string")
	private ProcessedLevelType level;
	@XmlElement(name = "Product")
	private RegistryIDType product;
	@XmlElement(name = "CaptureDevice")
	private RegistryIDType captureDevice;
	@XmlElement(name = "FeatureExtractionAlgorithm")
	private RegistryIDType featureExtractionAlgorithm;
	@XmlElement(name = "ComparisonAlgorithm")
	private RegistryIDType comparisonAlgorithm;
	@XmlElement(name = "CompressionAlgorithm")
	private RegistryIDType compressionAlgorithm;
	@XmlElement(name = "Purpose")
	@XmlSchemaType(name = "string")
	private PurposeType purpose;
	@XmlElement(name = "Quality")
	private QualityType quality;
	
	
	


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
}
