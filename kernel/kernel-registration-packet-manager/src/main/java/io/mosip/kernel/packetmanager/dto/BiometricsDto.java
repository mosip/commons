package io.mosip.kernel.packetmanager.dto;

import io.mosip.kernel.packetmanager.constants.Biometric;
import lombok.Data;

@Data
public class BiometricsDto {
	
	private byte[] attributeISO;
	private String bioAttribute;
	private String modalityName;
	private double qualityScore;
	private boolean isForceCaptured;
	private int numOfRetries;	
	private boolean isCaptured;
	private String subType;

		
	public BiometricsDto(String bioAttribute, byte[] attributeISO, double qualityScore) {
		this.bioAttribute = bioAttribute;
		this.attributeISO = attributeISO;
		this.qualityScore = qualityScore;
		this.modalityName = Biometric.getModalityNameByAttribute(bioAttribute);
	}
}
