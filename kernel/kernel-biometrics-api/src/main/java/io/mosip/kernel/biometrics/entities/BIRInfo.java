/**
 * 
 */
package io.mosip.kernel.biometrics.entities;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
/**
 * @author Ramadurai Pandian
 *
 */
@Data
@JsonDeserialize(builder = BIRInfo.BIRInfoBuilder.class)
public class BIRInfo implements Serializable {

	private String creator;
	private String index;
	private byte[] payload;
	private Boolean integrity;
	private LocalDateTime creationDate;
	private LocalDateTime notValidBefore;
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
