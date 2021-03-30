package io.mosip.commons.packet.util;

import static io.mosip.commons.packet.constants.PacketManagerConstants.IDENTITY;
import static io.mosip.commons.packet.constants.PacketManagerConstants.IDSCHEMA_VERSION;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.util.HMACUtils2;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.commons.packet.audit.AuditLogEntry;
import io.mosip.commons.packet.constants.PacketManagerConstants;
import io.mosip.commons.packet.dto.Packet;
import io.mosip.commons.packet.dto.PacketInfo;
import io.mosip.commons.packet.dto.packet.FieldValueArray;
import io.mosip.commons.packet.exception.GetAllMetaInfoException;
import io.mosip.commons.packet.exception.PacketKeeperException;
import io.mosip.commons.packet.keeper.PacketKeeper;
import io.mosip.kernel.core.idobjectvalidator.exception.IdObjectIOException;
import io.mosip.kernel.core.idobjectvalidator.exception.IdObjectValidationFailedException;
import io.mosip.kernel.core.idobjectvalidator.exception.InvalidIdSchemaException;
import io.mosip.kernel.core.idobjectvalidator.spi.IdObjectValidator;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.JsonUtils;
import io.mosip.kernel.core.util.exception.JsonProcessingException;

@Component
public class PacketValidator {

    @Value("${mosip.commons.packetnames:id}")
    private String packetNames;

    @Value("${mosip.commons.packet.manager.schema.validator.convertIdSchemaToDouble:true}")
    private boolean convertIdschemaToDouble;

    private static final Logger LOGGER = PacketManagerLogger.getLogger(PacketValidator.class);
    private static final String FIELD_LIST = "mosip.kernel.idobjectvalidator.mandatory-attributes.reg-processor.%s";

    private static final String eventId = "PACKET_MANAGER";
    private static final String eventName = "PACKET MANAGER";
    private static final String eventType = "SYSTEM";


    @Autowired
    private Environment env;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private PacketKeeper packetKeeper;

    @Autowired
    private IdObjectValidator idObjectValidator;

    @Autowired
    private IdSchemaUtils idSchemaUtils;

    @Autowired
    private AuditLogEntry auditLogEntry;


    public boolean validate(String id, String source, String process) throws IdObjectIOException, IdObjectValidationFailedException, InvalidIdSchemaException, IOException, JsonProcessingException, PacketKeeperException, NoSuchAlgorithmException {
        boolean result = validateSchema(id, source, process);
        if(result) {
            LOGGER.info(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id, "Id object validation successful for process name : " + process);
            auditLogEntry.addAudit("Id object validation successful", eventId, eventName, eventType, null, null, id);
            result = fileAndChecksumValidation(id, source, process);
        } else {
            LOGGER.error(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id, "Id object validation failed for process name : " + process);
            auditLogEntry.addAudit("Id object validation failed", eventId, eventName, eventType, null, null, id);
        }

        return result;
    }

    private boolean validateSchema(String id, String source, String process) throws IOException, InvalidIdSchemaException, IdObjectIOException {
        LinkedHashMap<String, Object> objectMap = new LinkedHashMap<>();
        try {
            for (String packetName : packetNames.split(",")) {
                Packet packet = packetKeeper.getPacket(getPacketInfo(id, packetName, source, process));
                InputStream idJsonStream = ZipUtils.unzipAndGetFile(packet.getPacket(), "ID");
                if (idJsonStream != null) {
                    byte[] bytearray = IOUtils.toByteArray(idJsonStream);
                    String jsonString = new String(bytearray);
                    LinkedHashMap<String, Object> currentIdMap = (LinkedHashMap<String, Object>) mapper
                            .readValue(jsonString, LinkedHashMap.class).get(IDENTITY);
                    if (convertIdschemaToDouble && currentIdMap.get(IDSCHEMA_VERSION) != null)
                        currentIdMap.put(IDSCHEMA_VERSION, Double.valueOf(currentIdMap.get(IDSCHEMA_VERSION).toString()));
                    currentIdMap.entrySet().forEach(e -> objectMap.putIfAbsent(e.getKey(), e.getValue()));

                }
            }
            String fields = env.getProperty(String.format(FIELD_LIST, IdObjectsSchemaValidationOperationMapper.getOperation(id, process).getOperation()));
            LinkedHashMap finalMap = new LinkedHashMap();
            finalMap.put(IDENTITY, objectMap);
            JSONObject finalIdObject = new JSONObject(finalMap);

            return idObjectValidator.validateIdObject(idSchemaUtils.getIdSchema(Double.valueOf(objectMap.get(
                    PacketManagerConstants.IDSCHEMA_VERSION).toString())), finalIdObject, Arrays.asList(fields.split(",")));
        } catch (IdObjectValidationFailedException | PacketKeeperException e) {
            LOGGER.error(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id, "Id object validation failed :  " + ExceptionUtils.getStackTrace(e));
            return false;
        }

    }

    /**
     * Files validation.
     *
     * @param id
     *            the registration id
     * @return true, if successful
     * @throws IOException
     */
    public boolean fileAndChecksumValidation(String id, String source, String process) throws IOException, JsonProcessingException, PacketKeeperException, NoSuchAlgorithmException {
        boolean isValid = false;
        // perform file and checksum validation for each source
        for (String packetName : packetNames.split(",")) {
            Packet packet = packetKeeper.getPacket(getPacketInfo(id, packetName, source, process));
            Map<String, String> finalMap = getMetaInfoJson(packet);
            if (!finalMap.isEmpty()) {

                List hashseq1List = finalMap.get("hashSequence1") != null ? mapper.readValue(finalMap.get("hashSequence1"), ArrayList.class) : null;
                List hashseq2List = finalMap.get("hashSequence2") != null ? (ArrayList) mapper.readValue(finalMap.get("hashSequence2"), ArrayList.class) : null;
                Map<String, InputStream> checksumMap = new HashMap<>();

                boolean fileValidation = validateFiles(hashseq1List, hashseq2List, checksumMap, packet);

                if (fileValidation) {
                    LOGGER.info(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id, "File validation successful for packet name : " + packetName);
                    auditLogEntry.addAudit("File validation successful", eventId, eventName, eventType, null, null, id);
                } else {
                    LOGGER.error(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id, "File validation failed for packet name : " + packetName);
                    auditLogEntry.addAudit("File validation failed", eventId, eventName, eventType, null, null, id);
                    return false;
                }

                boolean checksumValidation = checksumValidation(hashseq1List, hashseq2List, checksumMap, packet);

                if (checksumValidation) {
                    LOGGER.info(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id, "Checksum validation successful for packet name : " + packetName);
                    auditLogEntry.addAudit("Checksum validation successful", eventId, eventName, eventType, null, null, id);
                } else {
                    LOGGER.error(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id, "Checksum validation failed for packet name : " + packetName);
                    auditLogEntry.addAudit("Checksum validation failed", eventId, eventName, eventType, null, null, id);
                    return false;
                }

                isValid = true;
            }

        }
        return isValid;
    }

    private PacketInfo getPacketInfo(String id, String packetName, String source, String process) {
        PacketInfo packetInfo = new PacketInfo();
        packetInfo.setId(id);
        packetInfo.setPacketName(packetName);
        packetInfo.setProcess(process);
        packetInfo.setSource(source);
        return packetInfo;
    }

    private byte[] generateHash(List<FieldValueArray> hashSequence, Map<String, InputStream> checksumMap) throws NoSuchAlgorithmException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (FieldValueArray fieldValueArray : hashSequence) {
            List<String> hashValues = fieldValueArray.getValue();
            hashValues.forEach(value -> {
                byte[] valuebyte = null;
                try {
                    InputStream fileStream = checksumMap.get(value);
                    valuebyte = IOUtils.toByteArray(fileStream);
                    outputStream.write(valuebyte);
                } catch (IOException e) {
                   e.printStackTrace();
                }
            });
        }

        return HMACUtils2.digestAsPlainText(outputStream.toByteArray()).getBytes();

    }

    private Map<String, String> getMetaInfoJson(Packet packet) throws PacketKeeperException, IOException {
        Map<String, String> finalMap = new HashMap<>();
        InputStream metaInfoJson = ZipUtils.unzipAndGetFile(packet.getPacket(), "PACKET_META_INFO");
        if (metaInfoJson != null) {
            byte[] bytearray = IOUtils.toByteArray(metaInfoJson);
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
        return finalMap;
    }

    private boolean validateFiles(List hashseq1List, List hashseq2List, Map<String, InputStream> checksumMap, Packet packet) throws JsonProcessingException, IOException {
        List<String> allFileNames = new ArrayList<>();
        if (hashseq1List != null && !hashseq1List.isEmpty()) {
            for (Object o : hashseq1List) {
                FieldValueArray fieldValueArray = mapper.readValue(JsonUtils.javaObjectToJsonString(o), FieldValueArray.class);
                allFileNames.addAll(fieldValueArray.getValue());
            }
        }

        if (hashseq2List != null && !hashseq2List.isEmpty()) {
            for (Object o : hashseq2List) {
                FieldValueArray fieldValueArray = mapper.readValue(JsonUtils.javaObjectToJsonString(o), FieldValueArray.class);
                allFileNames.addAll(fieldValueArray.getValue());
            }
        }

        List<String> notFoundFiles = new ArrayList<>();
        allFileNames.forEach(v -> notFoundFiles.add(v));
        for (String fileName : allFileNames) {
            InputStream inputStream = ZipUtils.unzipAndGetFile(packet.getPacket(), fileName);
            if (inputStream != null && inputStream.available() > 0)
                checksumMap.put(fileName, inputStream);
            notFoundFiles.remove(fileName);
        }

        return (notFoundFiles.size() == 0);
    }

    private boolean checksumValidation(List hashseq1List, List hashseq2List, Map<String, InputStream> checksumMap, Packet packet) throws JsonProcessingException, IOException, NoSuchAlgorithmException {
        List<FieldValueArray> hashSequence1 = new ArrayList<>();
        List<FieldValueArray> hashSequence2 = new ArrayList<>();
        boolean isdataCheckSumEqual = false;
        boolean isoperationsCheckSumEqual = false;

        if (hashseq1List != null && !hashseq1List.isEmpty()) {
            for (Object o : hashseq1List) {
                FieldValueArray fieldValueArray = mapper.readValue(JsonUtils.javaObjectToJsonString(o), FieldValueArray.class);
                hashSequence1.add(fieldValueArray);
            }
        }

        if (hashseq2List != null && !hashseq2List.isEmpty()) {
            for (Object o : hashseq2List) {
                FieldValueArray fieldValueArray = mapper.readValue(JsonUtils.javaObjectToJsonString(o), FieldValueArray.class);
                hashSequence2.add(fieldValueArray);
            }
        }

        // Getting hash bytes from packet
        InputStream dataHashStream = ZipUtils.unzipAndGetFile(packet.getPacket(), "PACKET_DATA_HASH");
        InputStream operationsHashStream = ZipUtils.unzipAndGetFile(packet.getPacket(), "PACKET_OPERATIONS_HASH");

        if (dataHashStream != null) {
            byte[] dataHashByte = IOUtils.toByteArray(dataHashStream);
            byte[] dataHash = generateHash(hashSequence1, checksumMap);
            isdataCheckSumEqual = MessageDigest.isEqual(dataHash, dataHashByte);
        } else
            isdataCheckSumEqual = true;

        if (operationsHashStream != null) {
            byte[] operationsHashByte = IOUtils.toByteArray(operationsHashStream);
            byte[] operationsHash = generateHash(hashSequence2, checksumMap);
            isoperationsCheckSumEqual = MessageDigest.isEqual(operationsHash, operationsHashByte);
        } else
            isoperationsCheckSumEqual = true;

        return (isdataCheckSumEqual && isoperationsCheckSumEqual);

    }


}
