package io.mosip.commons.packet.test.impl;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.commons.packet.dto.Document;
import io.mosip.commons.packet.dto.PacketInfo;
import io.mosip.commons.packet.impl.PacketWriterImpl;
import io.mosip.commons.packet.keeper.PacketKeeper;
import io.mosip.commons.packet.spi.IPacketWriter;
import io.mosip.commons.packet.util.PacketManagerHelper;
import io.mosip.commons.packet.util.ZipUtils;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.QualityType;
import io.mosip.kernel.biometrics.entities.BDBInfo;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.entities.RegistryIDType;
import io.mosip.kernel.core.util.JsonUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ZipUtils.class, IOUtils.class, JsonUtils.class})
@PropertySource("classpath:application-test.properties")
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
public class PacketWriterImplTest {

    private static final String id = "10001100770000320200720092256";

    private static final String schemaJson = "{\"$schema\":\"http:\\/\\/json-schema.org\\/draft-07\\/schema#\",\"description\":\"MOSIP Sample identity\",\"additionalProperties\":false,\"title\":\"MOSIP identity\",\"type\":\"object\",\"definitions\":{\"simpleType\":{\"uniqueItems\":true,\"additionalItems\":false,\"type\":\"array\",\"items\":{\"additionalProperties\":false,\"type\":\"object\",\"required\":[\"language\",\"value\"],\"properties\":{\"language\":{\"type\":\"string\"},\"value\":{\"type\":\"string\"}}}},\"documentType\":{\"additionalProperties\":false,\"type\":\"object\",\"properties\":{\"format\":{\"type\":\"string\"},\"type\":{\"type\":\"string\"},\"value\":{\"type\":\"string\"}}},\"biometricsType\":{\"additionalProperties\":false,\"type\":\"object\",\"properties\":{\"format\":{\"type\":\"string\"},\"version\":{\"type\":\"number\",\"minimum\":0},\"value\":{\"type\":\"string\"}}}},\"properties\":{\"identity\":{\"additionalProperties\":false,\"type\":\"object\",\"required\":[\"IDSchemaVersion\",\"fullName\",\"dateOfBirth\",\"gender\",\"addressLine1\",\"addressLine2\",\"addressLine3\",\"region\",\"province\",\"city\",\"zone\",\"postalCode\",\"phone\",\"email\",\"proofOfIdentity\",\"individualBiometrics\"],\"properties\":{\"proofOfAddress\":{\"bioAttributes\":[],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/documentType\"},\"gender\":{\"bioAttributes\":[],\"fieldCategory\":\"pvt\",\"format\":\"\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"city\":{\"bioAttributes\":[],\"validators\":[{\"validator\":\"^(?=.{0,50}$).*\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"postalCode\":{\"bioAttributes\":[],\"validators\":[{\"validator\":\"^[(?i)A-Z0-9]{5}$|^NA$\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"type\":\"string\",\"fieldType\":\"default\"},\"proofOfException-1\":{\"bioAttributes\":[],\"fieldCategory\":\"evidence\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/documentType\"},\"referenceIdentityNumber\":{\"bioAttributes\":[],\"validators\":[{\"validator\":\"^([0-9]{10,30})$\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"kyc\",\"type\":\"string\",\"fieldType\":\"default\"},\"individualBiometrics\":{\"bioAttributes\":[\"leftEye\",\"rightEye\",\"rightIndex\",\"rightLittle\",\"rightRing\",\"rightMiddle\",\"leftIndex\",\"leftLittle\",\"leftRing\",\"leftMiddle\",\"leftThumb\",\"rightThumb\",\"face\"],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/biometricsType\"},\"province\":{\"bioAttributes\":[],\"validators\":[{\"validator\":\"^(?=.{0,50}$).*\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"zone\":{\"bioAttributes\":[],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"proofOfDateOfBirth\":{\"bioAttributes\":[],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/documentType\"},\"addressLine1\":{\"bioAttributes\":[],\"validators\":[{\"validator\":\"^(?=.{0,50}$).*\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"addressLine2\":{\"bioAttributes\":[],\"validators\":[{\"validator\":\"^(?=.{3,50}$).*\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"residenceStatus\":{\"bioAttributes\":[],\"fieldCategory\":\"kyc\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"addressLine3\":{\"bioAttributes\":[],\"validators\":[{\"validator\":\"^(?=.{3,50}$).*\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"email\":{\"bioAttributes\":[],\"validators\":[{\"validator\":\"^[A-Za-z0-9_\\\\-]+(\\\\.[A-Za-z0-9_]+)*@[A-Za-z0-9_-]+(\\\\.[A-Za-z0-9_]+)*(\\\\.[a-zA-Z]{2,})$\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"type\":\"string\",\"fieldType\":\"default\"},\"parentOrGuardianRID\":{\"bioAttributes\":[],\"fieldCategory\":\"evidence\",\"format\":\"none\",\"type\":\"string\",\"fieldType\":\"default\"},\"parentOrGuardianBiometrics\":{\"bioAttributes\":[\"leftEye\",\"rightEye\",\"rightIndex\",\"rightLittle\",\"rightRing\",\"rightMiddle\",\"leftIndex\",\"leftLittle\",\"leftRing\",\"leftMiddle\",\"leftThumb\",\"rightThumb\",\"face\"],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/biometricsType\"},\"fullName\":{\"bioAttributes\":[],\"validators\":[{\"validator\":\"^(?=.{3,50}$).*\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"dateOfBirth\":{\"bioAttributes\":[],\"validators\":[{\"validator\":\"^(1869|18[7-9][0-9]|19[0-9][0-9]|20[0-9][0-9])\\/([0][1-9]|1[0-2])\\/([0][1-9]|[1-2][0-9]|3[01])$\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"type\":\"string\",\"fieldType\":\"default\"},\"individualAuthBiometrics\":{\"bioAttributes\":[\"leftEye\",\"rightEye\",\"rightIndex\",\"rightLittle\",\"rightRing\",\"rightMiddle\",\"leftIndex\",\"leftLittle\",\"leftRing\",\"leftMiddle\",\"leftThumb\",\"rightThumb\",\"face\"],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/biometricsType\"},\"parentOrGuardianUIN\":{\"bioAttributes\":[],\"fieldCategory\":\"evidence\",\"format\":\"none\",\"type\":\"string\",\"fieldType\":\"default\"},\"proofOfIdentity\":{\"bioAttributes\":[],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/documentType\"},\"IDSchemaVersion\":{\"bioAttributes\":[],\"fieldCategory\":\"none\",\"format\":\"none\",\"type\":\"number\",\"fieldType\":\"default\",\"minimum\":0},\"proofOfException\":{\"bioAttributes\":[],\"fieldCategory\":\"evidence\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/documentType\"},\"phone\":{\"bioAttributes\":[],\"validators\":[{\"validator\":\"^[+]*([0-9]{1})([0-9]{9})$\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"type\":\"string\",\"fieldType\":\"default\"},\"parentOrGuardianName\":{\"bioAttributes\":[],\"fieldCategory\":\"evidence\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"proofOfRelationship\":{\"bioAttributes\":[],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/documentType\"},\"UIN\":{\"bioAttributes\":[],\"fieldCategory\":\"none\",\"format\":\"none\",\"type\":\"string\",\"fieldType\":\"default\"},\"region\":{\"bioAttributes\":[],\"validators\":[{\"validator\":\"^(?=.{0,50}$).*\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"}}}}}";

    private static final String source = "reg-client";
    private static final String process = "NEW";

    @InjectMocks
    private IPacketWriter packetWriter = new PacketWriterImpl();

    @Mock
    private PacketManagerHelper packetManagerHelper;

    @Mock
    private PacketKeeper packetKeeper;

    @Before
    public void setup() throws Exception {
        ReflectionTestUtils.setField(packetWriter, "defaultSubpacketName", "id");
        ReflectionTestUtils.setField(packetWriter, "defaultProviderVersion", "v1.0");
        ReflectionTestUtils.setField(packetWriter, "defaultSubpacketName", "id");
        ReflectionTestUtils.setField(packetWriter, "defaultSubpacketName", "id");

        PacketInfo packetInfo = new PacketInfo();
        packetInfo.setSource(source);
        packetInfo.setId(id);
        packetInfo.setProcess(process);


        when(packetManagerHelper.getXMLData(any(), anyBoolean())).thenReturn("abc".getBytes());
        when(packetKeeper.putPacket(any())).thenReturn(packetInfo);
    }

    @Test
    public void testCreatePacket() {
        Map<String, String> fieldMap = new HashMap<>();
        fieldMap.put("email", "mono@mono.com");
        fieldMap.put("phone", "123456789");
        fieldMap.put("proofOfAddress", "{\r\n  \"value\" : \"proofOfAddress\",\r\n  \"type\" : \"DOC004\",\r\n  \"format\" : \"jpg\"\r\n}");
        fieldMap.put("gender", "[ {\r\n  \"language\" : \"eng\",\r\n  \"value\" : \"Male\"\r\n}, {\r\n  \"language\" : \"ara\",\r\n  \"value\" : \"الذكر\"\r\n} ]");

        Map<String, String> metaMap = new HashMap<>();
        metaMap.put("metaData", "[ {\r\n  \"label\" : \"registrationId\",\r\n  \"value\" : \"10001100770000320200720092256\"\r\n}, {\r\n  \"label\" : \"creationDate\",\r\n  \"value\" : \"2020-07-20T14:54:49.823Z\"\r\n}, {\r\n  \"label\" : \"Registration Client Version Number\",\r\n  \"value\" : \"1.0.10\"\r\n}, {\r\n  \"label\" : \"registrationType\",\r\n  \"value\" : \"New\"\r\n}, {\r\n  \"label\" : \"preRegistrationId\",\r\n  \"value\" : null\r\n}, {\r\n  \"label\" : \"machineId\",\r\n  \"value\" : \"10077\"\r\n}, {\r\n  \"label\" : \"centerId\",\r\n  \"value\" : \"10001\"\r\n}, {\r\n  \"label\" : \"dongleId\",\r\n  \"value\" : null\r\n}, {\r\n  \"label\" : \"keyIndex\",\r\n  \"value\" : \"4F:38:72:D9:F8:DB:94:E7:22:48:96:D0:91:01:6D:3C:64:90:2E:14:DC:D2:F8:14:1F:2A:A4:19:1A:BC:91:41\"\r\n}, {\r\n  \"label\" : \"consentOfApplicant\",\r\n  \"value\" : \"Yes\"\r\n} ]");
        metaMap.put("operationsData", "[ {\r\n  \"label\" : \"officerId\",\r\n  \"value\" : \"110122\"\r\n}, {\r\n  \"label\" : \"officerBiometricFileName\",\r\n  \"value\" : null\r\n}, {\r\n  \"label\" : \"supervisorId\",\r\n  \"value\" : null\r\n}, {\r\n  \"label\" : \"supervisorBiometricFileName\",\r\n  \"value\" : null\r\n}, {\r\n  \"label\" : \"supervisorPassword\",\r\n  \"value\" : \"false\"\r\n}, {\r\n  \"label\" : \"officerPassword\",\r\n  \"value\" : \"true\"\r\n}, {\r\n  \"label\" : \"supervisorPIN\",\r\n  \"value\" : null\r\n}, {\r\n  \"label\" : \"officerPIN\",\r\n  \"value\" : null\r\n}, {\r\n  \"label\" : \"supervisorOTPAuthentication\",\r\n  \"value\" : \"false\"\r\n}, {\r\n  \"label\" : \"officerOTPAuthentication\",\r\n  \"value\" : \"false\"\r\n} ]");

        Map<String, String> audit = new HashMap<>();
        audit.put("actionTimeStamp", "2020-07-23T06:47:28.845Z");
        audit.put("applicationId", "REGISTRATION");
        audit.put("eventId", "REGISTRATION");

        List<Map<String, String>> audits = new ArrayList<>();
        audits.add(audit);

        Document document = new Document();
        document.setDocument("documentproofpdf".getBytes());
        document.setValue("proofOfAddress");
        document.setFormat("jpg");
        document.setType("DOC004");

        List<io.mosip.kernel.biometrics.entities.BIR> birTypeList = new ArrayList<>();
        io.mosip.kernel.biometrics.entities.BIR birType1 = new BIR.BIRBuilder().build();
        io.mosip.kernel.biometrics.entities.BDBInfo bdbInfoType1 = new BDBInfo.BDBInfoBuilder().build();
        io.mosip.kernel.biometrics.entities.RegistryIDType registryIDType = new RegistryIDType("Mosip", "257");
        io.mosip.kernel.biometrics.constant.QualityType quality = new QualityType();
        quality.setAlgorithm(registryIDType);
        quality.setScore(90l);
        bdbInfoType1.setQuality(quality);
        BiometricType singleType1 = BiometricType.FINGER;
        List<BiometricType> singleTypeList1 = new ArrayList<>();
        singleTypeList1.add(singleType1);
        List<String> subtype1 = new ArrayList<>(Arrays.asList("Left", "RingFinger"));
        bdbInfoType1.setSubtype(subtype1);
        bdbInfoType1.setType(singleTypeList1);
        birType1.setBdbInfo(bdbInfoType1);
        birTypeList.add(birType1);

        BiometricRecord biometricRecord = new BiometricRecord();
        biometricRecord.setSegments(birTypeList);

        packetWriter.setField(id, "name", "mono");
        packetWriter.setFields(id, fieldMap);
        packetWriter.addMetaInfo(id, metaMap);
        packetWriter.addMetaInfo(id, "capturedNonRegisteredDevices", null);
        packetWriter.addAudit(id, audit);
        packetWriter.addAudits(id, audits);
        packetWriter.setDocument(id, "proofOfAddress", document);
        packetWriter.setBiometric(id, "individualBiometrics", biometricRecord);

        List<PacketInfo> result = packetWriter.persistPacket(id, "0.1", schemaJson, source, process, null, null, true);

        assertTrue(result != null && result.size() == 3);
    }
}
