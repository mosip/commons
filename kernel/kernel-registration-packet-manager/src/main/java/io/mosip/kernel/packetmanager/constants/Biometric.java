package io.mosip.kernel.packetmanager.constants;

import io.mosip.kernel.core.cbeffutil.constant.CbeffConstant;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.SingleType;

import java.util.ArrayList;
import java.util.List;

public enum Biometric {
	
	LEFT_INDEX("FINGERPRINT_SLAB_LEFT", "Left Slab", "leftIndex", SingleType.FINGER, "LF_INDEX"),
	LEFT_MIDDLE("FINGERPRINT_SLAB_LEFT", "Left Slab",  "leftMiddle", SingleType.FINGER, "LF_MIDDLE"),
	LEFT_RING("FINGERPRINT_SLAB_LEFT", "Left Slab", "leftRing", SingleType.FINGER, "LF_RING"),
	LEFT_LITTLE("FINGERPRINT_SLAB_LEFT", "Left Slab", "leftLittle", SingleType.FINGER, "LF_LITTLE"),
	RIGHT_INDEX("FINGERPRINT_SLAB_RIGHT", "Right Slab", "rightIndex", SingleType.FINGER, "RF_INDEX"),
	RIGHT_MIDDLE("FINGERPRINT_SLAB_RIGHT", "Right Slab", "rightMiddle", SingleType.FINGER, "RF_MIDDLE"),
	RIGHT_RING("FINGERPRINT_SLAB_RIGHT", "Right Slab", "rightRing", SingleType.FINGER, "RF_RING"),
	RIGHT_LITTLE("FINGERPRINT_SLAB_RIGHT", "Right Slab", "rightLittle", SingleType.FINGER, "RF_LITTLE"),
	LEFT_THUMB("FINGERPRINT_SLAB_THUMBS", "Thumbs", "leftThumb", SingleType.FINGER, "LF_THUMB"),
	RIGHT_THUMB("FINGERPRINT_SLAB_THUMBS", "Thumbs", "rightThumb", SingleType.FINGER, "RF_THUMB"),
	RIGHT_IRIS("IRIS_DOUBLE", "Iris", "rightEye", SingleType.IRIS, "R_IRIS"),
	LEFT_IRIS("IRIS_DOUBLE", "Iris", "leftEye", SingleType.IRIS, "L_IRIS"),
	FACE("FACE_FULL FACE", "Face", "face", SingleType.FACE, "FACE");
	
	Biometric(String modalityName, String modalityShortName, String attributeName, SingleType singleType, String mdmConstant) {
		this.modalityName = modalityName;
		this.setModalityShortName(modalityShortName);
		this.attributeName = attributeName;
		this.singleType = singleType;
		this.mdmConstant = mdmConstant;
	}
	
	private String modalityName;
	private String modalityShortName;
	private String attributeName;
	private SingleType singleType;
	private String mdmConstant;
		
	public String getModalityName() {
		return modalityName;
	}
	public void setModalityName(String modalityName) {
		this.modalityName = modalityName;
	}
	public String getAttributeName() {
		return attributeName;
	}
	public void setAttributes(String attributeName) {
		this.attributeName = attributeName;
	}
	public SingleType getSingleType() {
		return singleType;
	}
	public void setSingleType(SingleType singleType) {
		this.singleType = singleType;
	}
	
	public String getModalityShortName() {
		return modalityShortName;
	}
	public void setModalityShortName(String modalityShortName) {
		this.modalityShortName = modalityShortName;
	}
	public static List<String> getDefaultAttributes(String modalityName) {
		List<String> list = new ArrayList<>();
		for(Biometric biometric : Biometric.values()) {
			if(biometric.getModalityName().equalsIgnoreCase(modalityName) || 
					biometric.getModalityShortName().equalsIgnoreCase(modalityName))
				list.add(biometric.getAttributeName());
		}
		return list;
	}
	
	public static String getModalityNameByAttribute(String attributeName) {
		String modalityName = null;
		for(Biometric biometric : Biometric.values()) {
			if(biometric.getAttributeName().equalsIgnoreCase(attributeName)) {
				modalityName = biometric.getModalityName();
				break;
			}
		}
		return modalityName;
	}	

	public static SingleType getSingleTypeByAttribute(String attributeName) {
		SingleType modalityName = null;
		for(Biometric biometric : Biometric.values()) {
			if(biometric.getAttributeName().equalsIgnoreCase(attributeName)) {
				modalityName = biometric.getSingleType();
				break;
			}
		}
		return modalityName;
	}
	
	public static Biometric getBiometricByAttribute(String attributeName) {
		Biometric constant = null;
		for(Biometric biometric : Biometric.values()) {
			if(biometric.getMdmConstant().equalsIgnoreCase(attributeName) || 
					biometric.getAttributeName().equalsIgnoreCase(attributeName)) {
				constant = biometric;
				break;
			}
		}
		return constant;
	}
	
	public static Biometric getBiometricByMDMConstant(String mdmConstant) {
		Biometric constant = null;
		for(Biometric biometric : Biometric.values()) {
			if(biometric.getMdmConstant().equalsIgnoreCase(mdmConstant)) {
				constant = biometric;
				break;
			}
		}
		return constant;
	}
	
	public static long getFormatType(SingleType singleType) {
		long format = 0;
		switch (singleType) {
		case FINGER:	
			format = CbeffConstant.FORMAT_TYPE_FINGER;
			break;
		case FACE:
			format = CbeffConstant.FORMAT_TYPE_FACE;
			break;
		case IRIS:
			format = CbeffConstant.FORMAT_TYPE_IRIS;
			break;
		}
		return format;
	}
	public String getMdmConstant() {
		return mdmConstant;
	}
	public void setMdsConstant(String mdmConstant) {
		this.mdmConstant = mdmConstant;
	}
	
	
}
