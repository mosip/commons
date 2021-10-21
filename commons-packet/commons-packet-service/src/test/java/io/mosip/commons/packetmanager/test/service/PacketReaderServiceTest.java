package io.mosip.commons.packetmanager.test.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import io.mosip.commons.khazana.dto.ObjectDto;
import io.mosip.commons.packet.facade.PacketReader;
import io.mosip.commons.packetmanager.dto.InfoResponseDto;
import io.mosip.commons.packetmanager.service.PacketReaderService;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.QualityType;
import io.mosip.kernel.biometrics.entities.BDBInfo;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.entities.RegistryIDType;
import io.mosip.kernel.core.exception.BaseUncheckedException;
import org.assertj.core.util.Lists;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;

@RunWith(SpringRunner.class)
public class PacketReaderServiceTest {

    private static final String id = "10001100770000320200720092256";

    @InjectMocks
    private PacketReaderService packetReaderService;

    @Mock
    private PacketReader packetReader;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private RestTemplate restTemplate;

    @Before
    public void setup() throws IOException {
        ReflectionTestUtils.setField(packetReaderService, "configServerUrl", "localhost");
        ReflectionTestUtils.setField(packetReaderService, "mappingjsonFileName", "reg-proc.json");

        List<BIR> birTypeList = new ArrayList<>();
        BIR birType1 = new BIR.BIRBuilder().build();
        BDBInfo bdbInfoType1 = new BDBInfo.BDBInfoBuilder().build();
        io.mosip.kernel.biometrics.entities.RegistryIDType registryIDType = new RegistryIDType();
        registryIDType.setOrganization("Mosip");
        registryIDType.setType("257");
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
        Mockito.when(packetReader.getBiometric(any(),any(),any(),any(),any(), anyBoolean())).thenReturn(biometricRecord);

        Mockito.when(restTemplate.getForObject(anyString(), any(Class.class))).thenReturn("jsonobject");
        LinkedHashMap tempMap = new LinkedHashMap();
        JSONObject jsonObject = new JSONObject();
        LinkedHashMap<String, String> val = new LinkedHashMap<>();
        val.put("value", "individualBiometrics");
        tempMap.put("individualBiometrics", val);
        jsonObject.put("identity", tempMap);
        jsonObject.put("documents", tempMap);
        jsonObject.put("metaInfo", tempMap);
        jsonObject.put("audits", tempMap);
        Mockito.when(objectMapper.readValue(anyString(), any(Class.class))).thenReturn(jsonObject);

        ObjectDto objectDto = new ObjectDto("REGISTRATION_CLIENT", "NEW", id + "_id", new Date());
        List<ObjectDto> allObjects = Lists.newArrayList(objectDto);
        Mockito.when(packetReader.info(id)).thenReturn(allObjects);

        Set<String> demographics = Sets.newHashSet("name", "email", "phone", "individualBiometrics");
        Mockito.when(packetReader.getAllKeys(id, objectDto.getSource(), objectDto.getProcess())).thenReturn(demographics);

    }

    @Test
    public void testInfoSuccess() {
        InfoResponseDto infoResponseDto = packetReaderService.info(id);

        assertTrue("Id should be equal.", infoResponseDto.getApplicationId().equals(id));
        assertTrue("Id should be equal.", infoResponseDto.getPacketId().equals(id));
        assertTrue("Size should be equal.", infoResponseDto.getInfo().size() == 1);
    }

    @Test(expected = BaseUncheckedException.class)
    public void testException() throws IOException {
        Mockito.when(objectMapper.readValue(anyString(), any(Class.class))).thenThrow(new IOException("IO Exception"));

        packetReaderService.info(id);
    }
}
