package io.mosip.commons.packet.test.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import io.mosip.commons.packet.dto.Packet;
import io.mosip.commons.packet.exception.GetAllIdentityException;
import io.mosip.commons.packet.exception.PacketKeeperException;
import io.mosip.commons.packet.impl.PacketReaderImpl;
import io.mosip.commons.packet.keeper.PacketKeeper;
import io.mosip.commons.packet.spi.IPacketReader;
import io.mosip.commons.packet.util.PacketValidator;
import io.mosip.commons.packet.util.ZipUtils;
import io.mosip.kernel.core.idobjectvalidator.exception.IdObjectIOException;
import io.mosip.kernel.core.idobjectvalidator.exception.IdObjectValidationFailedException;
import io.mosip.kernel.core.idobjectvalidator.exception.InvalidIdSchemaException;
import io.mosip.kernel.core.util.JsonUtils;
import io.mosip.kernel.core.util.exception.JsonProcessingException;
import org.apache.commons.io.IOUtils;
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ZipUtils.class, IOUtils.class, JsonUtils.class})
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

    }

    @Test
    public void validatePacketTest() throws JsonProcessingException, PacketKeeperException, InvalidIdSchemaException, IdObjectValidationFailedException, IdObjectIOException, IOException {
        when(packetValidator.validate(anyString(), anyString(), anyMap())).thenReturn(true);
        boolean result = iPacketReader.validatePacket("id", "process");

        assertTrue("Should be true", result);
    }

    @Test
    public void getAllTest() {
        Map<String, Object> result = iPacketReader.getAll("id", "process");

        assertTrue("Should be true", result.size() == 2);
    }

    @Test(expected = GetAllIdentityException.class)
    public void getAllExceptionTest() throws IOException {
        when(objectMapper.readValue(anyString(), any(Class.class))).thenReturn(null);

        Map<String, Object> result = iPacketReader.getAll("id", "process");

        assertTrue("Should be true", result.size() == 2);
    }

    @Test(expected = GetAllIdentityException.class)
    @Ignore
    public void getAllExceptionTest2() throws JsonProcessingException {
        PowerMockito.mockStatic(IOUtils.class);

        when(JsonUtils.javaObjectToJsonString(anyObject())).thenThrow(new JsonProcessingException("errormessage"));
        Map<String, Object> result = iPacketReader.getAll("id", "process");

        assertTrue("Should be true", result.size() == 2);
    }

    @Test
    public void getFieldTest() {
        String result = iPacketReader.getField("id", "phone", "process");

        assertTrue("Should be true", result.equals("9606139887"));
    }

    @Test
    public void getFieldsTest() {
        List<String> list = Lists.newArrayList("phone", "email");

        Map<String, String> result = iPacketReader.getFields("id", list, "process");

        assertTrue("Should be true", result.size() == 2);
    }

}
