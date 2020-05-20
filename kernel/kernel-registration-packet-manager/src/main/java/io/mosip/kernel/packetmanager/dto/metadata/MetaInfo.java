package io.mosip.kernel.packetmanager.dto.metadata;

import io.mosip.kernel.packetmanager.datatype.SimpleType;
import io.mosip.kernel.packetmanager.dto.SimpleDto;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Data
public class MetaInfo {	
	
	public MetaInfo() {
		this.metaData = new ArrayList<FieldValue>();
		this.operationsData = new ArrayList<FieldValue>();
		this.biometrics = new HashMap<>();
		this.exceptionBiometrics = new ArrayList<BiometricsException>();
		this.documents = new ArrayList<DocumentMetaInfo>();		
		this.hashSequence1 = new LinkedList<HashSequenceMetaInfo>();
		this.hashSequence2 = new LinkedList<HashSequenceMetaInfo>();	
		this.operationsData = new ArrayList<FieldValue>();
	}
	
	private Map<String, Map<String, ModalityInfo>> biometrics;
	private List<BiometricsException> exceptionBiometrics;
	private List<DocumentMetaInfo> documents;
	private List<FieldValue> metaData;
	private List<FieldValue> operationsData;
	private List<HashSequenceMetaInfo> hashSequence1;
	private List<HashSequenceMetaInfo> hashSequence2;
	private List<DeviceMetaInfo> capturedRegisteredDevices;
	private List<FieldValue> capturedNonRegisteredDevices;
	private List<FieldValue> checkSum;
	private List<SimpleType> printingName;
	
	public void setBiometrics(String subType, String bioAttribute, ModalityInfo modalityInfo) {
		if(this.biometrics.containsKey(subType) && this.biometrics.get(subType) != null) {
			this.biometrics.get(subType).put(bioAttribute, modalityInfo);
		}
		else {
			Map<String, ModalityInfo> map = new HashMap<>();
			map.put(bioAttribute, modalityInfo);
			this.biometrics.put(subType, map);
		}
	}
	
	public void setBiometricException(List<BiometricsException> modalityExceptions) {
		this.exceptionBiometrics.addAll(modalityExceptions);
	}
	
	public void addDocumentMetaInfo(DocumentMetaInfo documentMetaInfo) {
		this.documents.add(documentMetaInfo);
	}
	
	public void addMetaData(FieldValue fieldValue) {
		if(!this.metaData.contains(fieldValue))
			this.metaData.add(fieldValue);	
	}
	
	public void addOperationsData(FieldValue fieldValue) {
		if(!this.operationsData.contains(fieldValue))
			this.operationsData.add(fieldValue);	
	}
	
	public void addHashSequence1(HashSequenceMetaInfo hashSequenceMetaInfo) {
		this.hashSequence1.add(hashSequenceMetaInfo);
	}
	
	public void addHashSequence2(HashSequenceMetaInfo hashSequenceMetaInfo) {
		this.hashSequence2.add(hashSequenceMetaInfo);
	}
}
