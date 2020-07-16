package io.mosip.commons.packet.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.commons.packet.constants.PacketManagerConstants;
import io.mosip.commons.packet.dto.Document;
import io.mosip.commons.packet.dto.Packet;
import io.mosip.commons.packet.dto.PacketInfo;
import io.mosip.commons.packet.dto.ProviderInfo;
import io.mosip.commons.packet.exception.ApiNotAccessibleException;
import io.mosip.commons.packet.exception.GetAllIdentityException;
import io.mosip.commons.packet.exception.PacketDecryptionFailureException;
import io.mosip.commons.packet.keeper.PacketKeeper;
import io.mosip.commons.packet.spi.IPacketReader;
import io.mosip.commons.packet.spi.PacketSigner;
import io.mosip.commons.packet.util.IdSchemaUtils;
import io.mosip.commons.packet.util.ZipUtils;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.mosip.commons.packet.constants.PacketManagerConstants.FORMAT;
import static io.mosip.commons.packet.constants.PacketManagerConstants.IDENTITY;
import static io.mosip.commons.packet.constants.PacketManagerConstants.TYPE;
import static io.mosip.commons.packet.constants.PacketManagerConstants.VALUE;

@Component
public class PacketReaderImpl implements IPacketReader {

    @Value("${mosip.commons.packetNames}")
    private String packetNames;

    @Autowired
    private PacketKeeper packetKeeper;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private IdSchemaUtils idSchemaUtils;

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
        PacketInfo packetInfo = new PacketInfo();
        packetInfo.setId(id);
        packetInfo.setProcess(process);
        Map<String, Object> finalMap = new LinkedHashMap<>();

        String[] sourcePacketNames = packetNames.split(",");

        try {
            for (String source : sourcePacketNames) {
                packetInfo.setPacketName(source);
                Packet packet = packetKeeper.getPacket(packetInfo);
                InputStream idJsonStream = ZipUtils.unzipAndGetFile(packet.getPacket(), "ID");
                if(idJsonStream != null) {
                    byte[] bytearray = IOUtils.toByteArray(idJsonStream);
                    String jsonString = new String(bytearray);
                    LinkedHashMap<String, Object> currentIdMap = (LinkedHashMap<String, Object>) mapper.readValue(jsonString, LinkedHashMap.class).get(IDENTITY);


                    currentIdMap.keySet().stream().forEach(key -> {
                        finalMap.putIfAbsent(key, currentIdMap.get(key));
                    });
                }
            }
        } catch (Exception e) {
            throw new GetAllIdentityException(e.getMessage());
        }
        return finalMap;
    }

    @Override
    public String getField(String id, String field, String process) {
        Map<String, Object> allFields = getAll(id, process);
        if (allFields != null) {
            Object fieldObj = (Object) allFields.get(field);
            return fieldObj != null ? fieldObj.toString() : null;
        }
        return null;
    }

    @Override
    public Map<String, String> getFields(String id, List<String> fields, String process) {
        Map<String, String> result = new HashMap<>();
        Map<String, Object> allFields = getAll(id, process);
        fields.stream().forEach(field -> result.put(field, allFields.get(field) != null ? allFields.get(field).toString() : null));

        return result;
    }

    @Override
    public Document getDocument(String id, String documentName, String process) {
        Map<String, Object> idobjectMap = getAll(id, process);
        Double schemaVersion = (Double) idobjectMap.get(PacketManagerConstants.IDSCHEMA_VERSION);
        LinkedHashMap documentMap = (LinkedHashMap) idobjectMap.get(documentName);
        try {
            if (documentMap != null && !documentMap.isEmpty() && schemaVersion != null) {
                String packetName = idSchemaUtils.getSource(documentName, schemaVersion);
                PacketInfo packetInfo = new PacketInfo();
                packetInfo.setId(id);
                packetInfo.setPacketName(packetName);
                packetInfo.setProcess(process);
                Packet packet = packetKeeper.getPacket(packetInfo);
                String value = documentMap.get(VALUE) != null ? documentMap.get(VALUE).toString() : null;
                InputStream documentStream = ZipUtils.unzipAndGetFile(packet.getPacket(), value);
                Document document = new Document();
                document.setDocument(IOUtils.toByteArray(documentStream));
                document.setValue(value);
                document.setType(documentMap.get(TYPE) != null ? documentMap.get(TYPE).toString() : null);
                document.setFormat(documentMap.get(FORMAT) != null ? documentMap.get(FORMAT).toString() : null);
                return document;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ApiNotAccessibleException | PacketDecryptionFailureException e) {
            throw e;
        }
        return null;
    }

    @Override
    public BiometricRecord getBiometric(String id, String person, List<BiometricType> modalities, String process) {
        return null;
    }

    @Override
    public Map<String, String> getMetaInfo(String id, String source, String process) {
        return null;
    }
}
