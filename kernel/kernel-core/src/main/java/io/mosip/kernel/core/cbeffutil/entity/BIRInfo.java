/**
 * 
 */
package io.mosip.kernel.core.cbeffutil.entity;

import java.time.LocalDateTime;

import io.mosip.kernel.core.cbeffutil.jaxbclasses.BIRInfoType;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Record-level CBEFF BIR metadata (creator, integrity, validity).
 * <p>
 * Contract: all fields may be null. Use {@link BIRInfoBuilder} to construct
 * and {@link #toBIRInfo()} to marshal to JAXB.
 * </p>
 *
 * @author Ramadurai Pandian
 * @see BIR
 */
@Data
@NoArgsConstructor
public class BIRInfo {

	/**
	 * Creator of the BIR; may be null.
	 */
	private String creator;
	/**
	 * Unique index of this BIR; may be null.
	 */
	private String index;
	/**
	 * Optional integrity payload bytes; may be null.
	 */
	private byte[] payload;
	/**
	 * Whether integrity of the BIR is asserted; may be null.
	 */
	private Boolean integrity;
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

	public String getCreator() {
		return creator;
	}

	public String getIndex() {
		return index;
	}

	public byte[] getPayload() {
		return payload;
	}

	public Boolean isIntegrity() {
		return integrity;
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

	/**
	 * Builds BIR metadata from the given builder.
	 *
	 * @param bIRInfoBuilder never-null builder
	 */
	public BIRInfo(BIRInfoBuilder bIRInfoBuilder) {
		this.creator = bIRInfoBuilder.creator;
		this.index = bIRInfoBuilder.index;
		this.payload = bIRInfoBuilder.payload;
		this.integrity = bIRInfoBuilder.integrity;
		this.creationDate = bIRInfoBuilder.creationDate;
		this.notValidBefore = bIRInfoBuilder.notValidBefore;
		this.notValidAfter = bIRInfoBuilder.notValidAfter;
	}

	/**
	 * Fluent builder for {@link BIRInfo}.
	 */
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

	/**
	 * Converts this instance to the JAXB {@link BIRInfoType}.
	 *
	 * @return never-null JAXB type; null fields omitted
	 */
	public BIRInfoType toBIRInfo() {
		BIRInfoType bIRInfoType = new BIRInfoType();
		createrPopolation(bIRInfoType);
		if (isIntegrity() != null) {
			bIRInfoType.setIntegrity(isIntegrity());
		}

		if (getPayload() != null && getPayload().length > 0) {
			bIRInfoType.setPayload(getPayload());
		}
		if (getCreationDate() != null) {
			bIRInfoType.setCreationDate(getCreationDate());
		}
		if (getNotValidAfter() != null) {
			bIRInfoType.setNotValidAfter(getNotValidAfter());
		}
		if (getNotValidBefore() != null) {
			bIRInfoType.setNotValidBefore(getNotValidBefore());
		}

		return bIRInfoType;
	}

	/**
	 * Copies a non-empty creator onto the JAXB type.
	 *
	 * @param bIRInfoType never-null target JAXB type
	 */
	private void createrPopolation(BIRInfoType bIRInfoType) {
		if (getCreator() != null && getCreator().length() > 0) {
			bIRInfoType.setCreator(getCreator());
		}
	}

}
