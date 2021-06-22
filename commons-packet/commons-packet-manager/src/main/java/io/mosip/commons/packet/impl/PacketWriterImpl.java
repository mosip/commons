package io.mosip.commons.packet.impl;

import com.google.common.collect.Lists;
import io.mosip.commons.packet.constants.ErrorCode;
import io.mosip.commons.packet.constants.LoggerFileConstant;
import io.mosip.commons.packet.constants.PacketManagerConstants;
import io.mosip.commons.packet.dto.Document;
import io.mosip.commons.packet.dto.Packet;
import io.mosip.commons.packet.dto.PacketInfo;
import io.mosip.commons.packet.dto.packet.BiometricsType;
import io.mosip.commons.packet.dto.packet.DocumentType;
import io.mosip.commons.packet.dto.packet.HashSequenceMetaInfo;
import io.mosip.commons.packet.dto.packet.RegistrationPacket;
import io.mosip.commons.packet.exception.PacketCreatorException;
import io.mosip.commons.packet.keeper.PacketKeeper;
import io.mosip.commons.packet.spi.IPacketWriter;
import io.mosip.commons.packet.util.PacketManagerHelper;
import io.mosip.commons.packet.util.PacketManagerLogger;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.JsonUtils;
import io.mosip.kernel.core.util.StringUtils;
import io.mosip.kernel.core.util.exception.JsonProcessingException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class PacketWriterImpl implements IPacketWriter {

    private static final Logger LOGGER = PacketManagerLogger.getLogger(PacketWriterImpl.class);
    private static Map<String, String> categorySubpacketMapping = new HashMap<>();
    private static final String UNDERSCORE = "_";
    private static final String HASHSEQUENCE1 = "hashSequence1";
    private static final String HASHSEQUENCE2 = "hashSequence2";

    static {
        categorySubpacketMapping.put("pvt", "id");
        categorySubpacketMapping.put("kyc", "id");
        categorySubpacketMapping.put("none", "id,evidence,optional");
        categorySubpacketMapping.put("evidence", "evidence");
        categorySubpacketMapping.put("optional", "optional");
    }

    @Autowired
    private PacketManagerHelper packetManagerHelper;

    @Autowired
    private PacketKeeper packetKeeper;

    @Value("${mosip.kernel.packet.default_subpacket_name:id}")
    private String defaultSubpacketName;

    @Value("${default.provider.version:v1.0}")
    private String defaultProviderVersion;

    @Value("${packetmanager.zip.datetime.pattern:yyyyMMddHHmmss}")
    private String zipDatetimePattern;

    private RegistrationPacket registrationPacket = null;

    public RegistrationPacket initialize(String id) {
        if (this.registrationPacket == null || !registrationPacket.getRegistrationId().equalsIgnoreCase(id)) {
            this.registrationPacket = new RegistrationPacket();
            this.registrationPacket.setRegistrationId(id);
        }
        return registrationPacket;
    }

    @Override
    public void setField(String id, String fieldName, String value) {
        this.initialize(id).setField(fieldName, value);
    }

    @Override
    public void setFields(String id, Map<String, String> fields) {
        this.initialize(id).setFields(fields);
    }

    @Override
    public void setBiometric(String id, String fieldName, BiometricRecord value) {
        this.initialize(id).setBiometricField(fieldName, value);
    }

    @Override
    public void setDocument(String id, String fieldName, Document value) {
        this.initialize(id).setDocumentField(fieldName, value);
    }


    @Override
    public void addAudits(String id, List<Map<String, String>> auditList) {
        this.initialize(id).setAudits(auditList);
    }

    @Override
    public void addAudit(String id, Map<String, String> audit) {
        this.initialize(id).setAudit(audit);
    }

    @Override
    public void addMetaInfo(String id, Map<String, String> metaInfo) {
        this.initialize(id).setMetaData(metaInfo);
    }

    @Override
    public void addMetaInfo(String id, String key, String value) {
        this.initialize(id).addMetaData(key, value);
    }

    private List<PacketInfo> createPacket(String id, String version, String schemaJson, String source, String process,
                                          String additionalInfoReqId, String refId, boolean offlineMode) throws PacketCreatorException {
        LOGGER.info(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.ID.toString(), id, "Started packet creation");
        if (this.registrationPacket == null || !registrationPacket.getRegistrationId().equalsIgnoreCase(id))
            throw new PacketCreatorException(ErrorCode.INITIALIZATION_ERROR.getErrorCode(),
                    ErrorCode.INITIALIZATION_ERROR.getErrorMessage());

        List<PacketInfo> packetInfos = new ArrayList<>();

        Map<String, List<Object>> identityProperties = loadSchemaFields(schemaJson);

        try {
            int counter = 1;
            String packetId = new StringBuilder()
                    .append(StringUtils.isNotBlank(additionalInfoReqId) ? additionalInfoReqId : id)
                    .append("-")
                    .append(refId)
                    .append("-")
                    .append(getcurrentTimeStamp()).toString();
            for (String subPacketName : identityProperties.keySet()) {
                LOGGER.info(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.ID.toString(),
                        id, "Started Subpacket: " + subPacketName);
                List<Object> schemaFields = identityProperties.get(subPacketName);
                byte[] subpacketBytes = createSubpacket(Double.valueOf(version), schemaFields, defaultSubpacketName.equalsIgnoreCase(subPacketName),
                        id, offlineMode);

                PacketInfo packetInfo = new PacketInfo();
                packetInfo.setProviderName(this.getClass().getSimpleName());
                packetInfo.setSchemaVersion(new Double(version).toString());
                if (offlineMode)
                    packetInfo.setId(packetId);
                else
                    packetInfo.setId(id);
                packetInfo.setRefId(refId);
                packetInfo.setSource(source);
                packetInfo.setProcess(process);
                packetInfo.setPacketName(id + UNDERSCORE + subPacketName);
                packetInfo.setCreationDate(DateUtils.getUTCCurrentDateTimeString());
                packetInfo.setProviderVersion(defaultProviderVersion);
                Packet packet = new Packet();
                packet.setPacketInfo(packetInfo);
                packet.setPacket(subpacketBytes);
                packetKeeper.putPacket(packet);
                packetInfos.add(packetInfo);
                LOGGER.info(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.ID.toString(),
                        id, "Completed Subpacket: " + subPacketName);

                if (counter == identityProperties.keySet().size()) {
                    boolean res = packetKeeper.pack(packetInfo.getId(), packetInfo.getSource(),
                            packetInfo.getProcess(), packetInfo.getRefId());
                    if (!res)
                        packetKeeper.deletePacket(id, source, process);
                }

                counter++;
            }

        } catch (Exception e) {
            LOGGER.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.ID.toString(),
                    id, "Exception occured. Deleting the packet.");
            packetKeeper.deletePacket(id, source, process);
            throw new PacketCreatorException(ErrorCode.PKT_ZIP_ERROR.getErrorCode(),
                    ErrorCode.PKT_ZIP_ERROR.getErrorMessage().concat(ExceptionUtils.getStackTrace(e)));
        } finally {
            this.registrationPacket = null;
        }
        LOGGER.info(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.ID.toString(),
                id, "Exiting packet creation");
        return packetInfos;
    }

    @SuppressWarnings("unchecked")
    private byte[] createSubpacket(double version, List<Object> schemaFields, boolean isDefault, String id, boolean offlineMode)
            throws PacketCreatorException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream subpacketZip = new ZipOutputStream(new BufferedOutputStream(out))) {
            LOGGER.info(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.ID.toString(),
                    id, "Identified fields >>> " + schemaFields.size());
            Map<String, Object> identity = new HashMap<String, Object>();
            Map<String, HashSequenceMetaInfo> hashSequences = new HashMap<>();

            identity.put(PacketManagerConstants.IDSCHEMA_VERSION, version);
            this.registrationPacket.getMetaData().put(PacketManagerConstants.REGISTRATIONID, id);
            this.registrationPacket.getMetaData().put(PacketManagerConstants.META_CREATION_DATE, this.registrationPacket.getCreationDate());

            for (Object obj : schemaFields) {
                Map<String, Object> field = (Map<String, Object>) obj;
                String fieldName = (String) field.get(PacketManagerConstants.SCHEMA_ID);
                LOGGER.info(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.ID.toString(),
                        id, "Adding field : " + fieldName);
                switch ((String) field.get(PacketManagerConstants.SCHEMA_TYPE)) {
                    case PacketManagerConstants.BIOMETRICS_TYPE:
                        if (this.registrationPacket.getBiometrics().get(fieldName) != null)
                            addBiometricDetailsToZip(fieldName, identity, subpacketZip, hashSequences, offlineMode);
                        break;
                    case PacketManagerConstants.DOCUMENTS_TYPE:
                        if (this.registrationPacket.getDocuments().get(fieldName) != null)
                            addDocumentDetailsToZip(fieldName, identity, subpacketZip, hashSequences, offlineMode);
                        break;
                    default:
                        if (this.registrationPacket.getDemographics().get(fieldName) != null)
                            identity.put(fieldName, this.registrationPacket.getDemographics().get(fieldName));
                        break;
                }
            }

            byte[] identityBytes = getIdentity(identity).getBytes();
            addEntryToZip(PacketManagerConstants.IDENTITY_FILENAME_WITH_EXT, identityBytes, subpacketZip);
            addHashSequenceWithSource(PacketManagerConstants.DEMOGRAPHIC_SEQ, PacketManagerConstants.IDENTITY_FILENAME, identityBytes,
                    hashSequences);
            addOtherFilesToZip(isDefault, subpacketZip, hashSequences, offlineMode);

        } catch (JsonProcessingException e) {
            throw new PacketCreatorException(ErrorCode.OBJECT_TO_JSON_ERROR.getErrorCode(),
                    ErrorCode.BIR_TO_XML_ERROR.getErrorMessage().concat(ExceptionUtils.getStackTrace(e)));
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new PacketCreatorException(ErrorCode.PKT_ZIP_ERROR.getErrorCode(),
                    ErrorCode.PKT_ZIP_ERROR.getErrorMessage().concat(ExceptionUtils.getStackTrace(e)));
        }
        return out.toByteArray();
    }

    private void addDocumentDetailsToZip(String fieldName, Map<String, Object> identity,
                                         ZipOutputStream zipOutputStream, Map<String, HashSequenceMetaInfo> hashSequences, boolean offlineMode) throws PacketCreatorException {
        Document document = this.registrationPacket.getDocuments().get(fieldName);
        //filename without extension must be set as value in ID.json
        identity.put(fieldName, new DocumentType(fieldName, document.getType(), document.getFormat()));
        String fileName = String.format("%s.%s", fieldName, document.getFormat());
        addEntryToZip(fileName, document.getDocument(), zipOutputStream);
        this.registrationPacket.getMetaData().put(fieldName, document.getType());

        addHashSequenceWithSource(PacketManagerConstants.DEMOGRAPHIC_SEQ, fieldName, document.getDocument(),
                hashSequences);
    }

    private void addBiometricDetailsToZip(String fieldName, Map<String, Object> identity,
                                          ZipOutputStream zipOutputStream, Map<String, HashSequenceMetaInfo> hashSequences, boolean offlineMode) throws PacketCreatorException {
        BiometricRecord birType = this.registrationPacket.getBiometrics().get(fieldName);
        if (birType != null && birType.getSegments() != null && !birType.getSegments().isEmpty()) {

            byte[] xmlBytes;
            try {
                xmlBytes = packetManagerHelper.getXMLData(birType, offlineMode);
            } catch (Exception e) {
                throw new PacketCreatorException(ErrorCode.BIR_TO_XML_ERROR.getErrorCode(),
                        ErrorCode.BIR_TO_XML_ERROR.getErrorMessage().concat(ExceptionUtils.getStackTrace(e)));
            }

            addEntryToZip(String.format(PacketManagerConstants.CBEFF_FILENAME_WITH_EXT, fieldName), xmlBytes, zipOutputStream);
            identity.put(fieldName, new BiometricsType(PacketManagerConstants.CBEFF_FILE_FORMAT,
                    PacketManagerConstants.CBEFF_VERSION, String.format(PacketManagerConstants.CBEFF_FILENAME, fieldName)));
            addHashSequenceWithSource(PacketManagerConstants.BIOMETRIC_SEQ, String.format(PacketManagerConstants.CBEFF_FILENAME,
                    fieldName), xmlBytes, hashSequences);
        }
    }

    private void addHashSequenceWithSource(String sequenceType, String name, byte[] bytes,
                                           Map<String, HashSequenceMetaInfo> hashSequences) {
        if (!hashSequences.containsKey(sequenceType))
            hashSequences.put(sequenceType, new HashSequenceMetaInfo(sequenceType));

        hashSequences.get(sequenceType).addHashSource(name, bytes);
    }

    private void addOtherFilesToZip(boolean isDefault, ZipOutputStream zipOutputStream,
                                    Map<String, HashSequenceMetaInfo> hashSequences, boolean offlineMode) throws JsonProcessingException, PacketCreatorException, IOException, NoSuchAlgorithmException {

        if (isDefault) {
            addOperationsBiometricsToZip(PacketManagerConstants.OFFICER,
                    zipOutputStream, hashSequences, offlineMode);
            addOperationsBiometricsToZip(PacketManagerConstants.SUPERVISOR,
                    zipOutputStream, hashSequences, offlineMode);

            if (this.registrationPacket.getAudits() == null || this.registrationPacket.getAudits().isEmpty())
                throw new PacketCreatorException(ErrorCode.AUDITS_REQUIRED.getErrorCode(), ErrorCode.AUDITS_REQUIRED.getErrorMessage());

            byte[] auditBytes = JsonUtils.javaObjectToJsonString(this.registrationPacket.getAudits()).getBytes();
            addEntryToZip(PacketManagerConstants.AUDIT_FILENAME_WITH_EXT, auditBytes, zipOutputStream);
            addHashSequenceWithSource(PacketManagerConstants.OPERATIONS_SEQ, PacketManagerConstants.AUDIT_FILENAME, auditBytes,
                    hashSequences);

            HashSequenceMetaInfo hashSequenceMetaInfo = hashSequences.get(PacketManagerConstants.OPERATIONS_SEQ);
            addEntryToZip(PacketManagerConstants.PACKET_OPER_HASH_FILENAME,
                    PacketManagerHelper.generateHash(hashSequenceMetaInfo.getValue(), hashSequenceMetaInfo.getHashSource()),
                    zipOutputStream);

            this.registrationPacket.getMetaData().put(HASHSEQUENCE2, Lists.newArrayList(hashSequenceMetaInfo));
        }

        addPacketDataHash(hashSequences, zipOutputStream);
        addEntryToZip(PacketManagerConstants.PACKET_META_FILENAME, getIdentity(this.registrationPacket.getMetaData()).getBytes(), zipOutputStream);
    }

    private void addPacketDataHash(Map<String, HashSequenceMetaInfo> hashSequences,
                                   ZipOutputStream zipOutputStream) throws PacketCreatorException, IOException, NoSuchAlgorithmException {

        LinkedList<String> sequence = new LinkedList<String>();
        List<HashSequenceMetaInfo> hashSequenceMetaInfos = new ArrayList<>();
        Map<String, byte[]> data = new HashMap<>();
        if (hashSequences.containsKey(PacketManagerConstants.BIOMETRIC_SEQ)) {
            sequence.addAll(hashSequences.get(PacketManagerConstants.BIOMETRIC_SEQ).getValue());
            data.putAll(hashSequences.get(PacketManagerConstants.BIOMETRIC_SEQ).getHashSource());
            hashSequenceMetaInfos.add(hashSequences.get(PacketManagerConstants.BIOMETRIC_SEQ));
        }
        if (hashSequences.containsKey(PacketManagerConstants.DEMOGRAPHIC_SEQ)) {
            sequence.addAll(hashSequences.get(PacketManagerConstants.DEMOGRAPHIC_SEQ).getValue());
            data.putAll(hashSequences.get(PacketManagerConstants.DEMOGRAPHIC_SEQ).getHashSource());
            hashSequenceMetaInfos.add(hashSequences.get(PacketManagerConstants.DEMOGRAPHIC_SEQ));
        }
        if (hashSequenceMetaInfos.size() > 0)
            this.registrationPacket.getMetaData().put(HASHSEQUENCE1, hashSequenceMetaInfos);

        addEntryToZip(PacketManagerConstants.PACKET_DATA_HASH_FILENAME, PacketManagerHelper.generateHash(sequence, data),
                zipOutputStream);
    }

    private Map<String, List<Object>> loadSchemaFields(String schemaJson) throws PacketCreatorException {
        Map<String, List<Object>> packetBasedMap = new HashMap<String, List<Object>>();

        try {
            JSONObject schema = new JSONObject(schemaJson);
            schema = schema.getJSONObject(PacketManagerConstants.PROPERTIES);
            schema = schema.getJSONObject(PacketManagerConstants.IDENTITY);
            schema = schema.getJSONObject(PacketManagerConstants.PROPERTIES);

            JSONArray fieldNames = schema.names();
            for (int i = 0; i < fieldNames.length(); i++) {
                String fieldName = fieldNames.getString(i);
                JSONObject fieldDetail = schema.getJSONObject(fieldName);
                String fieldCategory = fieldDetail.has(PacketManagerConstants.SCHEMA_CATEGORY) ?
                        fieldDetail.getString(PacketManagerConstants.SCHEMA_CATEGORY) : "none";
                String packets = categorySubpacketMapping.get(fieldCategory.toLowerCase());

                String[] packetNames = packets.split(",");
                for (String packetName : packetNames) {
                    if (!packetBasedMap.containsKey(packetName)) {
                        packetBasedMap.put(packetName, new ArrayList<Object>());
                    }

                    Map<String, String> attributes = new HashMap<>();
                    attributes.put(PacketManagerConstants.SCHEMA_ID, fieldName);
                    attributes.put(PacketManagerConstants.SCHEMA_TYPE, fieldDetail.has(PacketManagerConstants.SCHEMA_REF) ?
                            fieldDetail.getString(PacketManagerConstants.SCHEMA_REF) : fieldDetail.getString(PacketManagerConstants.SCHEMA_TYPE));
                    packetBasedMap.get(packetName).add(attributes);
                }
            }
        } catch (JSONException e) {
            throw new PacketCreatorException(ErrorCode.JSON_PARSE_ERROR.getErrorCode(),
                    ErrorCode.JSON_PARSE_ERROR.getErrorMessage().concat(ExceptionUtils.getStackTrace(e)));
        }
        return packetBasedMap;
    }


    private void addEntryToZip(String fileName, byte[] data, ZipOutputStream zipOutputStream)
            throws PacketCreatorException {
        LOGGER.info(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.ID.toString(),
                this.registrationPacket.getRegistrationId(), "Adding file : " + fileName);
        try {
            if (data != null) {
                ZipEntry zipEntry = new ZipEntry(fileName);
                zipOutputStream.putNextEntry(zipEntry);
                zipOutputStream.write(data);
            }
        } catch (IOException e) {
            throw new PacketCreatorException(ErrorCode.ADD_ZIP_ENTRY_ERROR.getErrorCode(),
                    ErrorCode.ADD_ZIP_ENTRY_ERROR.getErrorMessage().concat(ExceptionUtils.getStackTrace(e)));
        }
    }

    private String getIdentity(Object object) throws JsonProcessingException {
        return "{ \"identity\" : " + JsonUtils.javaObjectToJsonString(object) + " } ";
    }

    private void addOperationsBiometricsToZip(String operationType,
                                              ZipOutputStream zipOutputStream, Map<String, HashSequenceMetaInfo> hashSequences, boolean offlineMode) throws PacketCreatorException {

        BiometricRecord biometrics = this.registrationPacket.getBiometrics().get(operationType);

        if (biometrics != null && biometrics.getSegments() != null && !biometrics.getSegments().isEmpty()) {
            byte[] xmlBytes;
            try {
                xmlBytes = packetManagerHelper.getXMLData(biometrics, offlineMode);
            } catch (Exception e) {
                throw new PacketCreatorException(ErrorCode.BIR_TO_XML_ERROR.getErrorCode(),
                        ErrorCode.BIR_TO_XML_ERROR.getErrorMessage().concat(ExceptionUtils.getStackTrace(e)));
            }

            if (xmlBytes != null) {
                String fileName = operationType + PacketManagerConstants.CBEFF_EXT;
                addEntryToZip(fileName, xmlBytes, zipOutputStream);
                this.registrationPacket.getMetaData().put(String.format("%sBiometricFileName", operationType), fileName);
                addHashSequenceWithSource(PacketManagerConstants.OPERATIONS_SEQ, operationType, xmlBytes, hashSequences);
            }
        }
    }

    @Override
    public List<PacketInfo> persistPacket(String id, String version, String schemaJson, String source,
                                          String process, String additionalInfoReqId, String refId, boolean offlineMode) {
        try {
            return createPacket(id, version, schemaJson, source, process, additionalInfoReqId, refId, offlineMode);
        } catch (PacketCreatorException e) {
            LOGGER.error(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id, ExceptionUtils.getStackTrace(e));
            throw e;
        }
    }

    private String getcurrentTimeStamp() {
        DateTimeFormatter format = DateTimeFormatter
                .ofPattern(zipDatetimePattern);
        return LocalDateTime.now(ZoneId.of("UTC")).format(format);
    }

}
