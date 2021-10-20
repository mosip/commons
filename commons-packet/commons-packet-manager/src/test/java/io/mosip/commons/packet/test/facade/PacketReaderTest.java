package io.mosip.commons.packet.test.facade;

import com.google.common.collect.Lists;
import io.mosip.commons.khazana.dto.ObjectDto;
import io.mosip.commons.packet.dto.Document;
import io.mosip.commons.packet.dto.TagResponseDto;
import io.mosip.commons.packet.exception.NoAvailableProviderException;
import io.mosip.commons.packet.facade.PacketReader;
import io.mosip.commons.packet.impl.PacketReaderImpl;
import io.mosip.commons.packet.keeper.PacketKeeper;
import io.mosip.commons.packet.spi.IPacketReader;
import io.mosip.commons.packet.util.PacketHelper;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.QualityType;
import io.mosip.kernel.biometrics.entities.BDBInfo;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.entities.RegistryIDType;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.SingleType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PacketHelper.class})
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
public class PacketReaderTest {

    @InjectMocks
    private PacketReader packetReader = new PacketReader();

    @Mock
    private PacketReaderImpl packetReaderProvider;

    private Map<String, Object> allFields;

    private static final String source = "reg-client";
    private static final String process = "NEW";
    private static final String id = "110111101120191111121111";
    
    @Mock
	private PacketKeeper packetKeeper;

    @Before
    public void setup() {
        PowerMockito.mockStatic(PacketHelper.class);
        PowerMockito.when(PacketHelper.isSourceAndProcessPresent(anyString(),anyString(),anyString(),any())).thenReturn(true);
        List<IPacketReader> referenceReaderProviders = new ArrayList<>();
        referenceReaderProviders.add(packetReaderProvider);
        ReflectionTestUtils.setField(packetReader, "referenceReaderProviders", referenceReaderProviders);
        allFields = new HashMap<>();
        allFields.put("name", "mono");
        allFields.put("email", "mono@mono.com");
        allFields.put("phone", "1234567");

        Mockito.when(packetReaderProvider.getAll(anyString(), anyString(), anyString())).thenReturn(allFields);

    }

    @Test
    public void testGetFieldWithBypassCache() {
        String field = "name";
        Mockito.when(packetReaderProvider.getField(anyString(), anyString(), anyString(), anyString())).thenReturn(field);

        String result = packetReader.getField(id, field, source, process, true);

        assertTrue(result == field);
    }

    @Test
    public void testGetField() {
        String field = "name";

        String result = packetReader.getField(id, field, source, process, false);

        assertTrue(result.equals(allFields.get("name")));
    }

    @Test
    public void testGetFields() {
        String field = "name";
        List<String> fieldList = Lists.newArrayList(field);
        Map<String, String> fieldMap = new HashMap<>();
        fieldMap.put(field, field);
        Mockito.when(packetReaderProvider.getFields(anyString(), anyList(), anyString(), anyString())).thenReturn(fieldMap);

        Map<String, String> result = packetReader.getFields(id, fieldList, source, process, true);

        assertTrue(result.size() == 1);
    }

    @Test
    public void testGetFieldsBypassCache() {
        String field = "name";
        List<String> fieldList = Lists.newArrayList(field);
        Map<String, String> fieldMap = new HashMap<>();
        fieldMap.put(field, field);
        Mockito.when(packetReaderProvider.getFields(anyString(), anyList(), anyString(), anyString())).thenReturn(fieldMap);

        Map<String, String> result = packetReader.getFields(id, fieldList, source, process, false);

        assertTrue(result.size() == 1);
    }

    @Test
    public void testGetDocument() {
        String docName = "poa";
        Document document = new Document();
        document.setValue("document");

        Mockito.when(packetReaderProvider.getDocument(anyString(), anyString(), anyString(), anyString())).thenReturn(document);

        Document result = packetReader.getDocument(id, docName, source, process);

        assertTrue(result.equals(document));
    }

    @Test
    public void testGetBiometrics() {
        List<BIR> birTypeList = new ArrayList<>();
        BIR birType1 = new BIR.BIRBuilder().build();
        BDBInfo bdbInfoType1 = new BDBInfo.BDBInfoBuilder().build();
        RegistryIDType registryIDType = new RegistryIDType("Mosip", "257");
        QualityType quality = new QualityType();
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

        Mockito.when(packetReaderProvider.getBiometric(anyString(), anyString(), anyList(), anyString(), anyString())).thenReturn(biometricRecord);

        BiometricRecord result = packetReader.getBiometric(id, "individualBiometrics", Lists.newArrayList(), source, process, true);

        assertTrue(result.equals(biometricRecord));
    }

    @Test
    public void testGetMetaInfo() {
        Map<String, String> metaMap = new HashMap<>();
        metaMap.put("operationsData","officerid:1234");

        Mockito.when(packetReaderProvider.getMetaInfo(anyString(), anyString(), anyString())).thenReturn(metaMap);

        Map<String, String> result = packetReader.getMetaInfo(id, source, process, true);

        assertTrue(result.equals(metaMap));
    }

    @Test
    public void testGetAudits() {
        Map<String, String> auditMap = new HashMap<>();
        auditMap.put("audit","audit1");
        List<Map<String, String>> auditList = new ArrayList<>();
        auditList.add(auditMap);

        Mockito.when(packetReaderProvider.getAuditInfo(anyString(), anyString(), anyString())).thenReturn(auditList);

        List<Map<String, String>> result = packetReader.getAudits(id, source, process, true);

        assertTrue(result.equals(auditList));
    }

    @Test
    public void testValidatePacket() {
        Map<String, String> auditMap = new HashMap<>();
        auditMap.put("audit","audit1");
        List<Map<String, String>> auditList = new ArrayList<>();
        auditList.add(auditMap);

        Mockito.when(packetReaderProvider.validatePacket(anyString(), anyString(), anyString())).thenReturn(true);

        boolean result = packetReader.validatePacket(id, source, process);

        assertTrue(result);
    }

    @Test(expected = NoAvailableProviderException.class)
    public void testProviderException() {
        PowerMockito.when(PacketHelper.isSourceAndProcessPresent(anyString(),anyString(),anyString(),any())).thenReturn(false);

        packetReader.validatePacket(id, source, process);
    }
    
    @Test
    public void testGetTags() {
        Map<String, String> tags = new HashMap<>();
        tags.put("test", "testValue");
      
        Mockito.when(packetKeeper.getTags(any(), any())).thenReturn(tags);

    	TagResponseDto tagResponseDto= packetReader.getTags("id",new ArrayList());

        assertEquals(tagResponseDto.getTags(),tags); 
    }

    @Test
    public void testInfo() {
        ObjectDto objectDto = new ObjectDto("source1", "process1", "object1", new Date());
        ObjectDto objectDto2 = new ObjectDto("source2", "process2", "object2", new Date());
        ObjectDto objectDto3 = new ObjectDto("source3", "process3", "object3", new Date());
        List<ObjectDto> objectDtos = Lists.newArrayList(objectDto, objectDto2, objectDto3);


        Mockito.when(packetKeeper.getAll(any())).thenReturn(objectDtos);

        List<ObjectDto> result = packetReader.info("id");

        assertEquals(objectDtos.size(), result.size());
    }
}
