package io.mosip.kernel.packetmanager.spi;

import io.mosip.kernel.packetmanager.dto.AuditDto;
import io.mosip.kernel.packetmanager.dto.BiometricsDto;
import io.mosip.kernel.packetmanager.dto.DocumentDto;
import io.mosip.kernel.packetmanager.dto.SimpleDto;
import io.mosip.kernel.packetmanager.dto.metadata.BiometricsException;
import io.mosip.kernel.packetmanager.dto.metadata.DeviceMetaInfo;
import io.mosip.kernel.packetmanager.exception.PacketCreatorException;

import java.util.List;
import java.util.Map;


public interface PacketCreator {
			
	public void initialize();
	
	public void setField(String fieldName, Object value);
	
	public void setField(String fieldName, List<SimpleDto> value);
	
	public void setBiometric(String fieldName, List<BiometricsDto> value);
	
	public void setDocument(String fieldName, DocumentDto value);
	
	public void setMetaInfo(String key, String value);
	
	public void setOperationsInfo(String key, String value);
	
	public void setBiometricException(String fieldName, List<BiometricsException> modalityExceptions);
	
	public void setAudits(List<AuditDto> auditList);
	
	public void setAcknowledgement(String acknowledgeReceiptName, byte[] acknowledgeReceipt);
	
	public void setChecksum(String key, String value);
	
	public void setRegisteredDeviceDetails(List<DeviceMetaInfo> deviceDetails);
	
	public void setPrintingName(String langauge, String printingName);
	
	public byte[] createPacket(String registrationId, double version, String schemaJson,
                               Map<String, String> categoryPacketMapping, byte[] publicKey, PacketSigner signer) throws PacketCreatorException;
	
	public Map<String, Object> getIdentityObject();
	
	public void setOfficerBiometric(String userId, String officerRole, List<BiometricsDto> value);

}
