package io.mosip.commons.packet.impl;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import io.mosip.commons.packet.constants.ErrorCode;
import io.mosip.commons.packet.dto.Document;
import io.mosip.commons.packet.dto.ProviderInfo;
import io.mosip.commons.packet.exception.PacketReaderException;
import io.mosip.commons.packet.spi.IPacketReader;
import io.mosip.commons.packet.spi.PacketSigner;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.IOException;
import io.mosip.kernel.core.util.JsonUtils;
import io.mosip.kernel.core.util.exception.JsonMappingException;
import io.mosip.kernel.core.util.exception.JsonParseException;

public class PacketReaderImpl implements IPacketReader {
    
    @Override
    public ProviderInfo init(String schemaUrl, byte[] publicKey, PacketSigner signer) {
        return null;
    }

    /**
     * idobject validation, cbeff validation(ex - biometric exception validation)
     *
     * @param id
     * @param process
     * @return
     */
    @Override
    public boolean validatePacket(String id, String process) {
        return false;
    }

    /**
     * return data from idobject of all 3 subpackets
     *
     * @param id
     * @param process
     * @return
     */
    @Override
    public Map<String, Object> getAll(String id, String process) {
        return null;
    }

    @Override
    public String getField(String id, String field, String process, boolean bypassCache) {
    	Map<String, Object> allFields=getAll(id,process);
    	
        return (String) allFields.get(field);
    }

    @Override
    public Map<String, String> getFields(String id, List<String> fields, String process, boolean bypassCache) {
    	Map<String, String> fieldsValue= new HashMap<String,String>();
    	Map<String, Object> allFields=getAll(id,process);
    
    	for(String key:fields) {
    		String value=(String) allFields.get(key);
    		fieldsValue.put(key, value);
    	}
        return fieldsValue;
    }

    @Override
    public Document getDocument(String id, String documentName, String process) throws PacketReaderException {
    	try {
    	String doc=getField( id,documentName,process,false);
    	  if(doc!=null) {
    	
				JSONObject documentJson = (JSONObject) JsonUtils.jsonStringToJavaObject(JSONObject.class, doc);
				Document document = new Document();
				document.setType((String) documentJson.get("type"));
				// To do to get inpustream for the documentJson.get("value")
			
    	  }
		} catch (JsonParseException | JsonMappingException e) {
			throw new PacketReaderException(ErrorCode.JSON_PARSE_ERROR.getErrorCode(),
					ErrorCode.JSON_PARSE_ERROR.getErrorMessage().concat(ExceptionUtils.getStackTrace(e)));
		} catch (IOException e) {
			throw new PacketReaderException(ErrorCode.SYSTEM_IO_ERROR.getErrorCode(),
					ErrorCode.SYSTEM_IO_ERROR.getErrorMessage().concat(ExceptionUtils.getStackTrace(e)));
		}
        return null;
    }

    @Override
    public BiometricRecord getBiometric(String id, String person, List<BiometricType> modalities, String process) {
        return null;
    }

    @Override
    public Map<String, String> getMetaInfo(String id, String source, String process, boolean bypassCache) {
        return null;
    }
}
