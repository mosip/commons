package io.mosip.commons.packet.dto.packet;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.commons.packet.dto.Document;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.BIRType;
import io.mosip.kernel.core.util.DateUtils;
import lombok.Data;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

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
	private Map<String, Document> documents;
	private Map<String, BiometricRecord> biometrics;
	private Map<String, Object> metaData;
	/*private Map<String, List<BiometricsException>> exceptionBiometrics;*/
	/*private List<FieldValue> operationsData;
	private List<DeviceMetaInfo> capturedRegisteredDevices;
	private List<FieldValue> capturedNonRegisteredDevices;*/
	/*private List<FieldValue> checkSum;*/
	/*private List<SimpleType> printingName;*/
	/*private List<BiometricsDto> officerBiometrics;
	private List<BiometricsDto> supervisorBiometrics;	*/
	private List<AuditDto> audits;	
	private byte[] acknowledgeReceipt;
	private String acknowledgeReceiptName;
		
	public PacketInfoDto() {
		this.creationDate = DateUtils.formatToISOString(LocalDateTime.now());
		this.demographics = new HashMap<String, Object>();
		this.documents = new HashMap<String, Document>();
		this.biometrics = new HashMap<String, BiometricRecord>();
		this.metaData = new HashMap<String, Object>();
		/*this.exceptionBiometrics = new HashMap<String, List<BiometricsException>>();
		this.operationsData = new ArrayList<FieldValue>();
		this.checkSum = new ArrayList<FieldValue>();*/
		/*this.printingName = new ArrayList<SimpleType>();*/
	}

	public void setField(String fieldName, String value) {
		setFields(fieldName, value, demographics);
	}
	
	public void setFields(Map<String, String> fields) {
		fields.entrySet().forEach(entry -> {
			setFields(entry.getKey(), entry.getValue(), demographics);
		});
	}
	
	public void setBiometricField(String fieldName, BiometricRecord value) {
		this.biometrics.put(fieldName, value);
	}
	
	public void setDocumentField(String fieldName, Document dto) {
		this.documents.put(fieldName, dto);		
	}

	public void setMetaData(Map<String, String> metaInfo) {
		metaInfo.entrySet().forEach(meta -> {
			setFields(meta.getKey(), meta.getValue(), this.metaData);
		});
	}

	private void setFields(String fieldName, String value, Map finalMap) {
		try {
			if (value != null) {
				Object json = new JSONTokener(value).nextValue();
				if (json instanceof JSONObject) {
					HashMap<String, Object> hashMap = new ObjectMapper().readValue(value, HashMap.class);
					finalMap.putIfAbsent(fieldName, hashMap);
				}
				else if (json instanceof JSONArray) {
					List jsonList = new ArrayList<>();
					JSONArray jsonArray = new JSONArray(value);
					for (int i = 0; i < jsonArray.length(); i++) {
						Object obj = jsonArray.get(i);
						HashMap<String, Object> hashMap = new ObjectMapper().readValue(obj.toString(), HashMap.class);
						jsonList.add(hashMap);
					}
					finalMap.putIfAbsent(fieldName, jsonList);
				} else
					finalMap.putIfAbsent(fieldName, value);
			} else
				finalMap.putIfAbsent(fieldName, value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/*public void setField(String fieldName, List<SimpleDto> value) {
		List<SimpleType> list = new ArrayList<SimpleType>();
		for(SimpleDto dto : value) {
			list.add(new SimpleType(dto.getLanguage(), dto.getValue()));
		}
		this.demographics.put(fieldName, list);
	}*/

	/*public void setExceptionBiometrics(String fieldName, List<BiometricsException> exceptionList) {
		this.exceptionBiometrics.put(fieldName, exceptionList);
	}

	public void setOperationsData(String key, String value) {
		if(!this.operationsData.contains(new FieldValue(key, value)))
			this.operationsData.add(new FieldValue(key, value));
	}
	
	public void setChecksum(String key, String value) {
		if(!this.checkSum.contains(new FieldValue(key, value)))
			this.checkSum.add(new FieldValue(key, value));
	}*/
	
	/*public void setPrintingName(String language, String value) {
		this.getPrintingName().add(new SimpleType(language, value));
	}*/
	

	
}
