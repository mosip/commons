package io.mosip.kernel.packetmanager.dto;

import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.packetmanager.constants.PacketManagerConstants;
import io.mosip.kernel.packetmanager.datatype.BiometricsType;
import io.mosip.kernel.packetmanager.datatype.DocumentType;
import io.mosip.kernel.packetmanager.datatype.SimpleType;
import io.mosip.kernel.packetmanager.dto.metadata.BiometricsException;
import io.mosip.kernel.packetmanager.dto.metadata.DeviceMetaInfo;
import io.mosip.kernel.packetmanager.dto.metadata.FieldValue;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Data
public class PacketInfoDto {	
	
	private String registrationId;
	private double idSchemaVersion;
	private String creationDate;
	private Map<String, Object> demographics;
	private Map<String, DocumentDto> documents;
	private Map<String, List<BiometricsDto>> biometrics;
	private Map<String, List<BiometricsException>> exceptionBiometrics;
	private List<FieldValue> metaData;
	private List<FieldValue> operationsData;
	private List<DeviceMetaInfo> capturedRegisteredDevices;
	private List<FieldValue> capturedNonRegisteredDevices;
	private List<FieldValue> checkSum;
	private List<SimpleType> printingName;	
	private List<BiometricsDto> officerBiometrics;
	private List<BiometricsDto> supervisorBiometrics;	
	private List<AuditDto> audits;	
	private byte[] acknowledgeReceipt;
	private String acknowledgeReceiptName;
		
	public PacketInfoDto() {
		this.creationDate = DateUtils.formatToISOString(LocalDateTime.now());
		this.demographics = new HashMap<String, Object>();
		this.documents = new HashMap<String, DocumentDto>();
		this.biometrics = new HashMap<String, List<BiometricsDto>>();
		this.exceptionBiometrics = new HashMap<String, List<BiometricsException>>();
		this.metaData = new ArrayList<FieldValue>();
		this.operationsData = new ArrayList<FieldValue>();
		this.checkSum = new ArrayList<FieldValue>();
		this.printingName = new ArrayList<SimpleType>();
	}
	
	public void setField(String fieldName, Object value) {
		this.demographics.put(fieldName, value);
	}
		
	public void setField(String fieldName, List<SimpleDto> value) {
		List<SimpleType> list = new ArrayList<SimpleType>();
		for(SimpleDto dto : value) {
			list.add(new SimpleType(dto.getLanguage(), dto.getValue()));
		}
		this.demographics.put(fieldName, list);
	}	
	
	public void setBiometricField(String fieldName, List<BiometricsDto> list) {		
		this.biometrics.put(fieldName, list);	
	}	
	
	public void setDocumentField(String fieldName, DocumentDto dto) {		
		this.documents.put(fieldName, dto);		
	}
	
	public void setExceptionBiometrics(String fieldName, List<BiometricsException> exceptionList) {
		this.exceptionBiometrics.put(fieldName, exceptionList);
	}
	
	public void setMetaData(String label, String value) {
		if(!this.metaData.contains(new FieldValue(label, value)))
			this.metaData.add(new FieldValue(label, value));
	}
	
	public void setMetaData(FieldValue fieldValue) {
		if(!this.metaData.contains(fieldValue))
			this.metaData.add(fieldValue);
	}
	
	public void setOperationsData(String key, String value) {
		if(!this.operationsData.contains(new FieldValue(key, value)))
			this.operationsData.add(new FieldValue(key, value));
	}
	
	public void setChecksum(String key, String value) {
		if(!this.checkSum.contains(new FieldValue(key, value)))
			this.checkSum.add(new FieldValue(key, value));
	}
	
	public void setPrintingName(String language, String value) {
		this.getPrintingName().add(new SimpleType(language, value));
	}
	
	public Map<String, Object> getIdentityObject() {
        Map<String, Object> identityData = new HashMap<String, Object>();
        identityData.put(PacketManagerConstants.IDSCHEMA_VERSION, idSchemaVersion);
        identityData.putAll(this.demographics);
        this.documents.forEach((k, v) ->{
        	Map<String, String> data =  new HashMap<>();
        	data.put("value", k);
        	data.put("type", v.getType());
        	data.put("format", v.getFormat());
            identityData.put(k, data);
        });             
        this.biometrics.forEach((k, v) -> {
        	Map<String, Object> data =  new HashMap<>();
        	data.put("value", String.format(PacketManagerConstants.CBEFF_FILENAME, k));
        	data.put("version", PacketManagerConstants.CBEFF_VERSION);
        	data.put("format", PacketManagerConstants.CBEFF_FILE_FORMAT);
            identityData.put(k, data);
        });
        Map<String, Object> idObject = new HashMap<String, Object>();
        idObject.put(PacketManagerConstants.IDENTITY, identityData);
        return idObject;
}
	
}
