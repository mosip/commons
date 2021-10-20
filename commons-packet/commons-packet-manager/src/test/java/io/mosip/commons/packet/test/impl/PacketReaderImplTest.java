package io.mosip.commons.packet.test.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import io.mosip.commons.packet.dto.Document;
import io.mosip.commons.packet.dto.Packet;
import io.mosip.commons.packet.exception.GetAllIdentityException;
import io.mosip.commons.packet.exception.GetAllMetaInfoException;
import io.mosip.commons.packet.exception.GetDocumentException;
import io.mosip.commons.packet.exception.PacketKeeperException;
import io.mosip.commons.packet.exception.PacketValidationFailureException;
import io.mosip.commons.packet.impl.PacketReaderImpl;
import io.mosip.commons.packet.keeper.PacketKeeper;
import io.mosip.commons.packet.spi.IPacketReader;
import io.mosip.commons.packet.util.IdSchemaUtils;
import io.mosip.commons.packet.util.PacketValidator;
import io.mosip.commons.packet.util.ZipUtils;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.core.cbeffutil.common.CbeffValidator;
import io.mosip.kernel.core.cbeffutil.entity.BDBInfo;
import io.mosip.kernel.core.cbeffutil.entity.BIR;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.BIRType;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.QualityType;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.RegistryIDType;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.SingleType;
import io.mosip.kernel.core.idobjectvalidator.exception.IdObjectIOException;
import io.mosip.kernel.core.idobjectvalidator.exception.IdObjectValidationFailedException;
import io.mosip.kernel.core.idobjectvalidator.exception.InvalidIdSchemaException;
import io.mosip.kernel.core.util.JsonUtils;
import io.mosip.kernel.core.util.exception.JsonProcessingException;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static io.mosip.commons.packet.constants.PacketManagerConstants.*;
import static io.mosip.commons.packet.constants.PacketManagerConstants.VALUE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ZipUtils.class, IOUtils.class, JsonUtils.class, CbeffValidator.class})
@PropertySource("classpath:application-test.properties")
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
public class PacketReaderImplTest {

    @InjectMocks
    private IPacketReader iPacketReader = new PacketReaderImpl();

    @Mock
    private PacketValidator packetValidator;

    @Mock
    private PacketKeeper packetKeeper;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private IdSchemaUtils idSchemaUtils;

    private static final String docName = "proofOfIdentity";
    private static final String biometricFieldName = "individualBiometrics";

    @Before
    public void setup() throws PacketKeeperException, IOException {
        Packet packet = new Packet();
        packet.setPacket("hello".getBytes());

        String str = "{ \"identity\" : {\n" +
                "  \"proofOfAddress\" : {\n" +
                "    \"value\" : \"proofOfAddress\",\n" +
                "    \"type\" : \"DOC004\",\n" +
                "    \"format\" : \"jpg\"\n" +
                "  },\n" +
                "  \"gender\" : [ {\n" +
                "    \"language\" : \"eng\",\n" +
                "    \"value\" : \"Male\"\n" +
                "  }, {\n" +
                "    \"language\" : \"ara\",\n" +
                "    \"value\" : \"الذكر\"\n" +
                "  } ],\n" +
                "  \"city\" : [ {\n" +
                "    \"language\" : \"eng\",\n" +
                "    \"value\" : \"Kenitra\"\n" +
                "  }, {\n" +
                "    \"language\" : \"ara\",\n" +
                "    \"value\" : \"القنيطرة\"\n" +
                "  } ],\n" +
                "  \"postalCode\" : \"14000\",\n" +
                "  \"fullName\" : [ {\n" +
                "    \"language\" : \"eng\",\n" +
                "    \"value\" : \"Test after fix\"\n" +
                "  }, {\n" +
                "    \"language\" : \"ara\",\n" +
                "    \"value\" : \"Test after fix\"\n" +
                "  } ],\n" +
                "  \"dateOfBirth\" : \"1976/01/01\",\n" +
                "  \"referenceIdentityNumber\" : \"2345235252352353523\",\n" +
                "  \"individualBiometrics\" : {\n" +
                "    \"format\" : \"cbeff\",\n" +
                "    \"version\" : 1.0,\n" +
                "    \"value\" : \"individualBiometrics_bio_CBEFF\"\n" +
                "  },\n" +
                "  \"IDSchemaVersion\" : \"0.1\",\n" +
                "  \"province\" : [ {\n" +
                "    \"language\" : \"eng\",\n" +
                "    \"value\" : \"Kenitra\"\n" +
                "  }, {\n" +
                "    \"language\" : \"ara\",\n" +
                "    \"value\" : \"القنيطرة\"\n" +
                "  } ],\n" +
                "  \"zone\" : [ {\n" +
                "    \"language\" : \"eng\",\n" +
                "    \"value\" : \"Assam\"\n" +
                "  }, {\n" +
                "    \"language\" : \"ara\",\n" +
                "    \"value\" : \"العصام\"\n" +
                "  } ],\n" +
                "  \"phone\" : \"9606139887\",\n" +
                "  \"addressLine1\" : [ {\n" +
                "    \"language\" : \"eng\",\n" +
                "    \"value\" : \"asdadsfas\"\n" +
                "  }, {\n" +
                "    \"language\" : \"ara\",\n" +
                "    \"value\" : \"asdadsfas\"\n" +
                "  } ],\n" +
                "  \"addressLine2\" : [ {\n" +
                "    \"language\" : \"eng\",\n" +
                "    \"value\" : \"qqwqrqwrw\"\n" +
                "  }, {\n" +
                "    \"language\" : \"ara\",\n" +
                "    \"value\" : \"qqwqrqwrw\"\n" +
                "  } ],\n" +
                "  \"residenceStatus\" : [ {\n" +
                "    \"language\" : \"eng\",\n" +
                "    \"value\" : \"Non-Foreigner\"\n" +
                "  }, {\n" +
                "    \"language\" : \"ara\",\n" +
                "    \"value\" : \"غير أجنبي\"\n" +
                "  } ],\n" +
                "  \"addressLine3\" : [ {\n" +
                "    \"language\" : \"eng\",\n" +
                "    \"value\" : \"wfwfwef\"\n" +
                "  }, {\n" +
                "    \"language\" : \"ara\",\n" +
                "    \"value\" : \"wfwfwef\"\n" +
                "  } ],\n" +
                "  \"region\" : [ {\n" +
                "    \"language\" : \"eng\",\n" +
                "    \"value\" : \"Rabat Sale Kenitra\"\n" +
                "  }, {\n" +
                "    \"language\" : \"ara\",\n" +
                "    \"value\" : \"جهة الرباط سلا القنيطرة\"\n" +
                "  } ],\n" +
                "  \"email\" : \"niyati.swami@technoforte.co.in\"\n" +
                "} } ";

        Map<String, Object> keyValueMap = new LinkedHashMap<>();
        keyValueMap.put("email", "niyati.swami@technoforte.co.in");
        keyValueMap.put("phone", "9606139887");
        keyValueMap.put("fullName", "[ {\r\n  \"language\" : \"eng\",\r\n  \"value\" : \"Test after fix\"\r\n}, {\r\n  \"language\" : \"ara\",\r\n  \"value\" : \"Test after fix\"\r\n} ]");
        keyValueMap.put("IDSchemaVersion", "0.1");
        keyValueMap.put(biometricFieldName, "{\r\n  \"format\" : \"cbeff\",\r\n  \"version\" : 1.0,\r\n  \"value\" : \"individualBiometrics_bio_CBEFF\"\r\n}");
        keyValueMap.put(docName, "{\r\n  \"value\" : \"proofOfIdentity\",\r\n  \"type\" : \"DOC003\",\r\n  \"format\" : \"jpg\"\r\n}");


        Map<String, Object> finalMap = new LinkedHashMap<>();
        finalMap.put("identity", keyValueMap);

        ByteArrayInputStream bis = new ByteArrayInputStream(str.getBytes());

        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(iPacketReader, "packetNames", "id,evidence,optional");
        when(packetKeeper.getPacket(any())).thenReturn(packet);

        PowerMockito.mockStatic(ZipUtils.class);
        when(ZipUtils.unzipAndGetFile(any(), anyString())).thenReturn(bis);
        PowerMockito.mockStatic(IOUtils.class);
        when(IOUtils.toByteArray(any(InputStream.class))).thenReturn(str.getBytes());

        when(objectMapper.readValue(anyString(), any(Class.class))).thenReturn(finalMap);
        when(idSchemaUtils.getSource(any(), any())).thenReturn("id");

    }

    @Test
    public void validatePacketTest() throws JsonProcessingException, PacketKeeperException, InvalidIdSchemaException, IdObjectValidationFailedException, IdObjectIOException, IOException, NoSuchAlgorithmException {
        when(packetValidator.validate(anyString(), anyString(), anyString())).thenReturn(true);
        boolean result = iPacketReader.validatePacket("id", "source", "process");

        assertTrue("Should be true", result);
    }

    @Test(expected = PacketValidationFailureException.class)
    public void validatePacketExceptionTest() throws JsonProcessingException, PacketKeeperException, InvalidIdSchemaException, IdObjectValidationFailedException, IdObjectIOException, IOException, NoSuchAlgorithmException {
        when(packetValidator.validate(anyString(), anyString(), anyString())).thenThrow(new IOException("exception"));
        boolean result = iPacketReader.validatePacket("id",  "source","process");

    }

    @Test
    public void getAllTest() {
        Map<String, Object> result = iPacketReader.getAll("id", "source", "process");

        assertTrue("Should be true", result.size() == 6);
    }

    @Test(expected = GetAllIdentityException.class)
    public void getAllExceptionTest() throws IOException {
        when(objectMapper.readValue(anyString(), any(Class.class))).thenReturn(null);

        Map<String, Object> result = iPacketReader.getAll("id", "source", "process");
    }

    @Test(expected = GetAllIdentityException.class)
    public void getAllExceptionTest2() throws JsonProcessingException, IOException {
        PowerMockito.mockStatic(JsonUtils.class);
        Map<String, Object> keyValueMap = new LinkedHashMap<>();
        keyValueMap.put("email", new JSONObject(new LinkedHashMap()));
        Map<String, Object> finalMap = new LinkedHashMap<>();
        finalMap.put("identity", keyValueMap);
        when(objectMapper.readValue(anyString(), any(Class.class))).thenReturn(finalMap);

        when(JsonUtils.javaObjectToJsonString(anyObject())).thenThrow(new JsonProcessingException("errormessage"));
        Map<String, Object> result = iPacketReader.getAll("id", "source", "process");
    }

    @Test
    public void getFieldTest() {
        String result = iPacketReader.getField("id", "phone",  "source","process");

        assertTrue("Should be true", result.equals("9606139887"));
    }

    @Test
    public void getFieldsTest() {
        List<String> list = Lists.newArrayList("phone", "email");

        Map<String, String> result = iPacketReader.getFields("id", list, "source", "process");

        assertTrue("Should be true", result.size() == 2);
    }

    @Test
    public void getDocumentTest() {
        List<String> list = Lists.newArrayList("phone", "email");

        Document result = iPacketReader.getDocument("id", docName, "source", "process");

        assertTrue("Should be true", result.getDocument() != null);
    }

    @Test(expected = GetDocumentException.class)
    public void getDocumentExceptionTest() throws IOException {
        List<String> list = Lists.newArrayList("phone", "email");

        when(idSchemaUtils.getSource(any(), any())).thenThrow(new IOException("exception"));

        Document result = iPacketReader.getDocument("id", docName, "source", "process");

    }

    @Test
    public void getBiometricsTest() throws Exception {
        BIRType birType = new BIRType();
        BIRType birType1 = new BIRType();
        BIRType birType2 = new BIRType();
        birType.setBir(Lists.newArrayList(birType1, birType2));
        BIR bir1 = new BIR();
        BDBInfo bdbInfoType1 = new BDBInfo();
        RegistryIDType registryIDType = new RegistryIDType();
        registryIDType.setOrganization("Mosip");
        registryIDType.setType("257");
        QualityType quality = new QualityType();
        quality.setAlgorithm(registryIDType);
        quality.setScore(90l);
        bdbInfoType1.setQuality(quality);
        SingleType singleType1 = SingleType.FINGER;
        List<SingleType> singleTypeList1 = new ArrayList<>();
        singleTypeList1.add(singleType1);
        List<String> subtype1 = new ArrayList<>(Arrays.asList("Left", "RingFinger"));
        bdbInfoType1.setSubtype(subtype1);
        bdbInfoType1.setType(singleTypeList1);
        bdbInfoType1.setFormat(registryIDType);
        bir1.setBdbInfo(bdbInfoType1);
        BIR bir2 = new BIR();
        bir2.setBdbInfo(bdbInfoType1);

        PowerMockito.mockStatic(CbeffValidator.class);
        when(CbeffValidator.getBIRFromXML(any())).thenReturn(birType);
        when(CbeffValidator.convertBIRTypeToBIR(any())).thenReturn(Lists.newArrayList(bir1, bir2));

        BiometricRecord result = iPacketReader.getBiometric("id", biometricFieldName, null, "source", "process");

        assertTrue("Should be true", result.getSegments().size() == 2);
    }

    @Test
    @Ignore
    public void getBiometricsExceptionTest() throws Exception {
        List<String> list = Lists.newArrayList("phone", "email");
        BIRType birType = new BIRType();
        BIRType birType1 = new BIRType();
        BIRType birType2 = new BIRType();
        birType.setBir(Lists.newArrayList(birType1, birType2));
        BIR bir1 = new BIR();
        BDBInfo bdbInfoType1 = new BDBInfo();
        RegistryIDType registryIDType = new RegistryIDType();
        registryIDType.setOrganization("Mosip");
        registryIDType.setType("257");
        QualityType quality = new QualityType();
        quality.setAlgorithm(registryIDType);
        quality.setScore(90l);
        bdbInfoType1.setQuality(quality);
        SingleType singleType1 = SingleType.FINGER;
        List<SingleType> singleTypeList1 = new ArrayList<>();
        singleTypeList1.add(singleType1);
        List<String> subtype1 = new ArrayList<>(Arrays.asList("Left", "RingFinger"));
        bdbInfoType1.setSubtype(subtype1);
        bdbInfoType1.setType(singleTypeList1);
        bdbInfoType1.setFormat(registryIDType);
        bir1.setBdbInfo(bdbInfoType1);
        BIR bir2 = new BIR();
        bir2.setBdbInfo(bdbInfoType1);

        Map<String, Object> keyValueMap = new LinkedHashMap<>();
        keyValueMap.put("operationsData", "[{\n  \"label\\\" : \\\"officerId\\\",\n  \\\"value\\\" : \\\"110012\\\"\n}, {\n  \\\"label\\\" : \\\"officerBiometricFileName\\\",\n  \\\"value\\\" : \\\"officer_bio_cbeff\\\"\n}, {\n  \\\"label\\\" : \\\"supervisorId\\\",\n  \\\"value\\\" : null\n}, {\n  \\\"label\\\" : \\\"supervisorBiometricFileName\\\",\n  \\\"value\\\" : null\n}, {\n  \\\"label\\\" : \\\"supervisorPassword\\\",\n  \\\"value\\\" : \\\"false\\\"\n}, {\n  \\\"label\\\" : \\\"officerPassword\\\",\n  \\\"value\\\" : \\\"true\\\"\n}, {\n  \\\"label\\\" : \\\"supervisorPIN\\\",\n  \\\"value\\\" : null\n}, {\n  \\\"label\\\" : \\\"officerPIN\\\",\n  \\\"value\\\" : null\n}]");
        keyValueMap.put("metaData", "[{\r\n  \"label\" : \"registrationId\",\r\n  \"value\" : \"10001100770000320200720092256\"\r\n}, {\r\n  \"label\" : \"creationDate\",\r\n  \"value\" : \"2020-07-20T14:54:49.823Z\"\r\n}, {\r\n  \"label\" : \"Registration Client Version Number\",\r\n  \"value\" : \"1.0.10\"\r\n}, {\r\n  \"label\" : \"registrationType\",\r\n  \"value\" : \"New\"\r\n}, {\r\n  \"label\" : \"preRegistrationId\",\r\n  \"value\" : null\r\n}, {\r\n  \"label\" : \"machineId\",\r\n  \"value\" : \"10077\"\r\n}, {\r\n  \"label\" : \"centerId\",\r\n  \"value\" : \"10001\"\r\n}, {\r\n  \"label\" : \"dongleId\",\r\n  \"value\" : null\r\n}, {\r\n  \"label\" : \"keyIndex\",\r\n  \"value\" : \"4F:38:72:D9:F8:DB:94:E7:22:48:96:D0:91:01:6D:3C:64:90:2E:14:DC:D2:F8:14:1F:2A:A4:19:1A:BC:91:41\"\r\n}, {\r\n  \"label\" : \"consentOfApplicant\",\r\n  \"value\" : \"Yes} ]");
        Map<String, Object> finalMap = new LinkedHashMap<>();
        finalMap.put("identity", keyValueMap);

        when(objectMapper.readValue(anyString(), any(Class.class))).thenReturn(finalMap);

        PowerMockito.mockStatic(CbeffValidator.class);
        when(CbeffValidator.getBIRFromXML(any())).thenReturn(birType);
        when(CbeffValidator.convertBIRTypeToBIR(any())).thenReturn(Lists.newArrayList(bir1, bir2));

        BiometricRecord result = iPacketReader.getBiometric("id", "officerBiometric", null, "source", "process");

        assertTrue("Should be true", result.getSegments().size() == 2);
    }

    @Test
    public void getMetaInfoTest() throws IOException {
        Map<String, Object> keyValueMap = new LinkedHashMap<>();
        keyValueMap.put("operationsData", "[{\n" +
                "\t\"label\": \"officerId\",\n" +
                "\t\"value\": \"110122\"\n" +
                "}, {\n" +
                "\t\"label\": \"officerBiometricFileName\",\n" +
                "\t\"value\": \"officerBiometric\"\n" +
                "}, {\n" +
                "\t\"label\": \"supervisorId\",\n" +
                "\t\"value\": null\n" +
                "}, {\n" +
                "\t\"label\": \"supervisorBiometricFileName\",\n" +
                "\t\"value\": null\n" +
                "}, {\n" +
                "\t\"label\": \"supervisorPassword\",\n" +
                "\t\"value\": \"false\"\n" +
                "}, {\n" +
                "\t\"label\": \"officerPassword\",\n" +
                "\t\"value\": \"true\"\n" +
                "}, {\n" +
                "\t\"label\": \"supervisorPIN\",\n" +
                "\t\"value\": null\n" +
                "}, {\n" +
                "\t\"label\": \"officerPIN\",\n" +
                "\t\"value\": null\n" +
                "}, {\n" +
                "\t\"label\": \"supervisorOTPAuthentication\",\n" +
                "\t\"value\": \"false\"\n" +
                "}, {\n" +
                "\t\"label\": \"officerOTPAuthentication\",\n" +
                "\t\"value\": \"false\"\n" +
                "}]");
        keyValueMap.put("metaData", "\"[ {\r\n  \"label\" : \"registrationId\",\r\n  \"value\" : \"10001100770000320200720092256\"\r\n}, {\r\n  \"label\" : \"creationDate\",\r\n  \"value\" : \"2020-07-20T14:54:49.823Z\"\r\n}, {\r\n  \"label\" : \"Registration Client Version Number\",\r\n  \"value\" : \"1.0.10\"\r\n}, {\r\n  \"label\" : \"registrationType\",\r\n  \"value\" : \"New\"\r\n}, {\r\n  \"label\" : \"preRegistrationId\",\r\n  \"value\" : null\r\n}, {\r\n  \"label\" : \"machineId\",\r\n  \"value\" : \"10077\"\r\n}, {\r\n  \"label\" : \"centerId\",\r\n  \"value\" : \"10001\"\r\n}, {\r\n  \"label\" : \"dongleId\",\r\n  \"value\" : null\r\n}, {\r\n  \"label\" : \"keyIndex\",\r\n  \"value\" : \"4F:38:72:D9:F8:DB:94:E7:22:48:96:D0:91:01:6D:3C:64:90:2E:14:DC:D2:F8:14:1F:2A:A4:19:1A:BC:91:41\"\r\n}, {\r\n  \"label\" : \"consentOfApplicant\",\r\n  \"value\" : \"Yes\"\r\n} ]\"");
        Map<String, Object> finalMap = new LinkedHashMap<>();
        finalMap.put("identity", keyValueMap);

        when(objectMapper.readValue(anyString(), any(Class.class))).thenReturn(finalMap);

        Map<String, String> result = iPacketReader.getMetaInfo("id", "source", "process");

        assertTrue("Should be true", result.size() == 2);
    }

    @Test
    public void getAuditTest() throws IOException {
        ReflectionTestUtils.setField(iPacketReader, "packetNames", "id");
        Map<String, String> keyValueMap = new LinkedHashMap<>();
        keyValueMap.put("audit1", "audit1");
        keyValueMap.put("audit2", "audit2");
        List<Map<String, String>> finalMap = new ArrayList<>();
        finalMap.add(keyValueMap);

        when(objectMapper.readValue(anyString(), any(Class.class))).thenReturn(finalMap);

        List<Map<String, String>> result = iPacketReader.getAuditInfo("id", "source", "process");

        assertTrue("Should be true", result.size() == 1);
    }

    @Test(expected = GetAllMetaInfoException.class)
    public void metaInfoExceptionTest() throws IOException {
        when(objectMapper.readValue(anyString(), any(Class.class))).thenThrow(new IOException("exception"));

        iPacketReader.getMetaInfo("id", "source", "process");
    }

    @Test(expected = GetAllIdentityException.class)
    public void getAuditExceptionTest() throws IOException {
        when(objectMapper.readValue(anyString(), any(Class.class))).thenThrow(new IOException("exception"));

        iPacketReader.getAuditInfo("id", "source", "process");
    }

}
