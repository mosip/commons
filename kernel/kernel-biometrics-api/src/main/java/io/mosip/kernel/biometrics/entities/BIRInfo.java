/**
 * 
 */
package io.mosip.kernel.biometrics.entities;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.mosip.kernel.core.cbeffutil.common.DateAdapter;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * @author Ramadurai Pandian
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BIRInfoType", propOrder = { "creator", "index", "payload", "integrity", "creationDate",
		"notValidBefore", "notValidAfter" })
@Data
@NoArgsConstructor
@JsonDeserialize(builder = BIRInfo.BIRInfoBuilder.class)
public class BIRInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2466414332099574792L;
	@XmlElement(name = "Creator")
	private String creator;
	@XmlElement(name = "Index")
	private String index;
	@XmlElement(name = "Payload")
	private byte[] payload;
	@XmlElement(name = "Integrity")
	private Boolean integrity;
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
	
	
	public BIRInfo(BIRInfoBuilder bIRInfoBuilder) {
		this.creator = bIRInfoBuilder.creator;
		this.index = bIRInfoBuilder.index;
		this.payload = bIRInfoBuilder.payload;
		this.integrity = bIRInfoBuilder.integrity;
		this.creationDate = bIRInfoBuilder.creationDate;
		this.notValidBefore = bIRInfoBuilder.notValidBefore;
		this.notValidAfter = bIRInfoBuilder.notValidAfter;
	}

	public static class BIRInfoBuilder {
		private String creator;
		private String index;
		private byte[] payload;
		private Boolean integrity;
		private LocalDateTime creationDate;
		private LocalDateTime notValidBefore;
		private LocalDateTime notValidAfter;

		public BIRInfoBuilder withCreator(String creator) {
			this.creator = creator;
			return this;
		}

		public BIRInfoBuilder withIndex(String index) {
			this.index = index;
			return this;
		}

		public BIRInfoBuilder withPayload(byte[] payload) {
			this.payload = payload;
			return this;
		}

		public BIRInfoBuilder withIntegrity(Boolean integrity) {
			this.integrity = integrity;
			return this;
		}

		public BIRInfoBuilder withCreationDate(LocalDateTime creationDate) {
			this.creationDate = creationDate;
			return this;
		}

		public BIRInfoBuilder withNotValidBefore(LocalDateTime notValidBefore) {
			this.notValidBefore = notValidBefore;
			return this;
		}

		public BIRInfoBuilder withNotValidAfter(LocalDateTime notValidAfter) {
			this.notValidAfter = notValidAfter;
			return this;
		}

		public BIRInfo build() {
			return new BIRInfo(this);
		}

	}
}
