package io.mosip.commons.packet.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.commons.packet.constants.PacketManagerConstants;
import io.mosip.commons.packet.dto.Document;
import io.mosip.commons.packet.dto.Packet;
import io.mosip.commons.packet.dto.PacketInfo;
import io.mosip.commons.packet.dto.packet.AuditDto;
import io.mosip.commons.packet.dto.packet.BiometricsException;
import io.mosip.commons.packet.exception.ApiNotAccessibleException;
import io.mosip.commons.packet.exception.FieldNameNotFoundException;
import io.mosip.commons.packet.exception.GetAllIdentityException;
import io.mosip.commons.packet.exception.GetAllMetaInfoException;
import io.mosip.commons.packet.exception.GetBiometricException;
import io.mosip.commons.packet.exception.GetDocumentException;
import io.mosip.commons.packet.exception.PacketDecryptionFailureException;
import io.mosip.commons.packet.facade.PacketWriter;
import io.mosip.commons.packet.keeper.PacketKeeper;
import io.mosip.commons.packet.spi.IPacketReader;
import io.mosip.commons.packet.util.IdSchemaUtils;
import io.mosip.commons.packet.util.PacketManagerLogger;
import io.mosip.commons.packet.util.ZipUtils;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.core.cbeffutil.common.CbeffValidator;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.BIRType;
import io.mosip.kernel.core.cbeffutil.spi.CbeffUtil;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.JsonUtils;
import io.mosip.kernel.core.util.exception.JsonProcessingException;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.mosip.commons.packet.constants.PacketManagerConstants.FORMAT;
import static io.mosip.commons.packet.constants.PacketManagerConstants.ID;
import static io.mosip.commons.packet.constants.PacketManagerConstants.IDENTITY;
import static io.mosip.commons.packet.constants.PacketManagerConstants.LABEL;
import static io.mosip.commons.packet.constants.PacketManagerConstants.META_INFO_OPERATIONS_DATA;
import static io.mosip.commons.packet.constants.PacketManagerConstants.TYPE;
import static io.mosip.commons.packet.constants.PacketManagerConstants.VALUE;

@RefreshScope
@Component
public class PacketReaderImpl implements IPacketReader {

    Logger LOGGER = PacketManagerLogger.getLogger(PacketReaderImpl.class);

    @Autowired
    private PacketWriter packetWriter;

    @Value("${mosip.commons.packetNames}")
    private String packetNames;

    @Autowired
    private PacketKeeper packetKeeper;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private IdSchemaUtils idSchemaUtils;

    @Autowired
    private CbeffUtil cbeffUtil;

    /**
     * idobject providerConfig, cbeff providerConfig(ex - biometric exception providerConfig)
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
        LOGGER.info(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id, "Getting all fields :: enrtry");
        Map<String, Object> finalMap = new LinkedHashMap<>();
        String[] sourcePacketNames = packetNames.split(",");

        try {
            for (String source : sourcePacketNames) {
                Packet packet = packetKeeper.getPacket(getPacketInfo(id, source, process));
                InputStream idJsonStream = ZipUtils.unzipAndGetFile(packet.getPacket(), "ID");
                if (idJsonStream != null) {
                    byte[] bytearray = IOUtils.toByteArray(idJsonStream);
                    String jsonString = new String(bytearray);
                    LinkedHashMap<String, Object> currentIdMap = (LinkedHashMap<String, Object>) mapper.readValue(jsonString, LinkedHashMap.class).get(IDENTITY);

                    currentIdMap.keySet().stream().forEach(key -> {
                        Object value = currentIdMap.get(key);
                        if (value != null && value instanceof Number)
                            finalMap.putIfAbsent(key, value);
                        else {
                            try {
                                finalMap.putIfAbsent(key, value != null ? JsonUtils.javaObjectToJsonString(currentIdMap.get(key)) : null);
                            } catch (io.mosip.kernel.core.util.exception.JsonProcessingException e) {
                                e.printStackTrace();
                                throw new GetAllIdentityException(e.getMessage());
                            }
                        }
                    });
                }
            }
        } catch (Exception e) {
            LOGGER.error(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id, e.getStackTrace().toString());
            throw new GetAllIdentityException(e.getMessage());
        }

        return finalMap;
    }

    @Override
    public String getField(String id, String field, String process) {
        LOGGER.info(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id, "getField :: for - " + field);
        Map<String, Object> allFields = getAll(id, process);
        if (allFields != null) {
            Object fieldObj = allFields.get(field);
            return fieldObj != null ? fieldObj.toString() : null;
        }
        return null;
    }

    @Override
    public Map<String, String> getFields(String id, List<String> fields, String process) {
        LOGGER.info(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id, "getFields :: for - " + fields.toString());
        Map<String, String> result = new HashMap<>();
        Map<String, Object> allFields = getAll(id, process);
        fields.stream().forEach(field -> result.put(field, allFields.get(field) != null ? allFields.get(field).toString() : null));

        return result;
    }

    @Override
    public Document getDocument(String id, String documentName, String process) {
        LOGGER.info(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id, "getDocument :: for - " + documentName);
        Map<String, Object> idobjectMap = getAll(id, process);
        Double schemaVersion = (Double) idobjectMap.get(PacketManagerConstants.IDSCHEMA_VERSION);
        String documentString = (String) idobjectMap.get(documentName);
        try {
            JSONObject documentMap = new JSONObject(documentString);
            if (documentMap != null && schemaVersion != null) {
                String packetName = idSchemaUtils.getSource(documentName, schemaVersion);
                Packet packet = packetKeeper.getPacket(getPacketInfo(id, packetName, process));
                String value = documentMap.has(VALUE) ? documentMap.get(VALUE).toString() : null;
                InputStream documentStream = ZipUtils.unzipAndGetFile(packet.getPacket(), value);
                Document document = new Document();
                document.setDocument(IOUtils.toByteArray(documentStream));
                document.setValue(value);
                document.setType(documentMap.get(TYPE) != null ? documentMap.get(TYPE).toString() : null);
                document.setFormat(documentMap.get(FORMAT) != null ? documentMap.get(FORMAT).toString() : null);
                return document;
            }
        } catch (IOException | ApiNotAccessibleException | PacketDecryptionFailureException | JSONException e) {
            LOGGER.error(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id, e.getStackTrace().toString());
            throw new GetDocumentException(e.getMessage());
        }
        return null;
    }

    @Override
    public BiometricRecord getBiometric(String id, String biometricFieldName, List<BiometricType> modalities, String process) {
        LOGGER.info(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id, "getBiometric :: for - " + biometricFieldName);
        BiometricRecord biometricRecord = null;
        String packetName = null;
        String fileName = null;
        try {
            Map<String, Object> idobjectMap = getAll(id, process);
            String bioString = (String) idobjectMap.get(biometricFieldName);
            JSONObject biometricMap = new JSONObject(bioString);
            if (biometricMap == null || biometricMap.isNull(VALUE)) {
                // biometric file not present in idobject. Search in meta data.
                Map<String, String> metadataMap = getMetaInfo(id, process);
                String operationsData = metadataMap.get(META_INFO_OPERATIONS_DATA);
                JSONArray jsonArray = new JSONArray(operationsData);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                    if (jsonObject.has(LABEL) && jsonObject.get(LABEL).toString().equalsIgnoreCase(biometricFieldName)) {
                        packetName = ID;
                        fileName = jsonObject.isNull(VALUE) ? null : jsonObject.get(VALUE).toString();
                        break;
                    }
                }
            } else {
                Double schemaVersion = (Double) idobjectMap.get(PacketManagerConstants.IDSCHEMA_VERSION);
                packetName = idSchemaUtils.getSource(biometricFieldName, schemaVersion);
                fileName = biometricMap.get(VALUE).toString();
            }

            if (packetName == null || fileName == null)
                throw new FieldNameNotFoundException();

            Packet packet = packetKeeper.getPacket(getPacketInfo(id, packetName, process));
            InputStream biometrics = ZipUtils.unzipAndGetFile(packet.getPacket(), fileName);
            BIRType birType = CbeffValidator.getBIRFromXML(IOUtils.toByteArray(biometrics));
            biometricRecord = new BiometricRecord();
            biometricRecord.setSegments(CbeffValidator.convertBIRTypeToBIR(birType.getBIR()));
        } catch (Exception e) {
            LOGGER.error(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id, e.getStackTrace().toString());
            throw new GetBiometricException(e.getMessage());
        }

        return biometricRecord;
    }

    @Override
    public Map<String, String> getMetaInfo(String id, String process) {
        Map<String, String> finalMap = new LinkedHashMap<>();
        String[] sourcePacketNames = packetNames.split(",");

        try {
            for (String packetName : sourcePacketNames) {
                Packet packet = packetKeeper.getPacket(getPacketInfo(id, packetName, process));
                InputStream idJsonStream = ZipUtils.unzipAndGetFile(packet.getPacket(), "PACKET_META_INFO");
                if (idJsonStream != null) {
                    byte[] bytearray = IOUtils.toByteArray(idJsonStream);
                    String jsonString = new String(bytearray);
                    LinkedHashMap<String, Object> currentIdMap = (LinkedHashMap<String, Object>) mapper.readValue(jsonString, LinkedHashMap.class).get(IDENTITY);

                    currentIdMap.keySet().stream().forEach(key -> {
                        try {
                            finalMap.putIfAbsent(key, currentIdMap.get(key) != null ? JsonUtils.javaObjectToJsonString(currentIdMap.get(key)) : null);
                        } catch (io.mosip.kernel.core.util.exception.JsonProcessingException e) {
                            throw new GetAllMetaInfoException(e.getMessage());
                        }
                    });
                }
            }
        } catch (Exception e) {
            throw new GetAllMetaInfoException(e.getMessage());
        }
        return finalMap;
    }


    private PacketInfo getPacketInfo(String id, String packetName, String process) {
        PacketInfo packetInfo = new PacketInfo();
        packetInfo.setId(id);
        packetInfo.setPacketName(packetName);
        packetInfo.setProcess(process);
        return packetInfo;
    }
}
