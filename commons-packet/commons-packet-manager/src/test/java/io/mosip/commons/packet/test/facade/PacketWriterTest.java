package io.mosip.commons.packet.test.facade;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.commons.packet.dto.Document;
import io.mosip.commons.packet.dto.PacketInfo;
import io.mosip.commons.packet.dto.TagDto;
import io.mosip.commons.packet.dto.TagRequestDto;
import io.mosip.commons.packet.dto.packet.PacketDto;
import io.mosip.commons.packet.exception.NoAvailableProviderException;
import io.mosip.commons.packet.exception.PacketCreatorException;
import io.mosip.commons.packet.facade.PacketWriter;
import io.mosip.commons.packet.impl.PacketWriterImpl;
import io.mosip.commons.packet.keeper.PacketKeeper;
import io.mosip.commons.packet.spi.IPacketWriter;
import io.mosip.commons.packet.util.PacketHelper;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.QualityType;
import io.mosip.kernel.biometrics.entities.BDBInfo;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.entities.RegistryIDType;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PacketHelper.class})
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
public class PacketWriterTest {

    @InjectMocks
    private PacketWriter packetWriter = new PacketWriter();

    @Mock
    private PacketWriterImpl packetWriterProvider;

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
        List<IPacketWriter> referenceWriterProviders = new ArrayList<>();
        referenceWriterProviders.add(packetWriterProvider);
        ReflectionTestUtils.setField(packetWriter, "referenceWriterProviders", referenceWriterProviders);
    }

    @Test
    public void testSetField() {
        Mockito.doNothing().when(packetWriterProvider).setField(anyString(),anyString(),anyString());

        packetWriter.setField(id, "name", "mono", source, process);
    }

    @Test
    public void testSetFields() {
        Map<String, String> fields = new HashMap<>();
        Mockito.doNothing().when(packetWriterProvider).setFields(anyString(),anyMap());

        packetWriter.setFields(id, fields, source, process);
    }

    @Test
    public void testSetDocument() {
        Document document = new Document();
        document.setValue("document");
        Mockito.doNothing().when(packetWriterProvider).setDocument(anyString(), anyString(), any());

        packetWriter.setDocument(id, "poa", document, source, process);
    }

    @Test
    public void testSetBiometrics() {
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
        String source = "reg-client";
        String process = "NEW";
        String id = "110111101120191111121111";
        BiometricRecord biometricRecord = new BiometricRecord();
        biometricRecord.setSegments(birTypeList);
        Mockito.doNothing().when(packetWriterProvider).setBiometric(anyString(), anyString(), any());

        packetWriter.setBiometric(id, "individualBiometrics", biometricRecord, source, process);
    }

    @Test
    public void testAddMetaInfo() {
        Map<String, String> fields = new HashMap<>();
        Mockito.doNothing().when(packetWriterProvider).addMetaInfo(anyString(),anyMap());

        packetWriter.addMetaInfo(id, fields, source, process);
    }

    @Test
    public void testAddMetaInfoKeyValue() {
        Mockito.doNothing().when(packetWriterProvider).addMetaInfo(anyString(),anyString(),anyString());

        packetWriter.addMetaInfo(id, "rid", "regid", source, process);
    }

    @Test
    public void testAddAudit() {
        Map<String, String> fields = new HashMap<>();
        Mockito.doNothing().when(packetWriterProvider).addAudit(anyString(),anyMap());

        packetWriter.addAudit(id, fields, source, process);
    }

    @Test
    public void testAddAudits() {
        Map<String, String> auditMap = new HashMap<>();
        auditMap.put("audit","audit1");
        List<Map<String, String>> auditList = new ArrayList<>();
        auditList.add(auditMap);
        Mockito.doNothing().when(packetWriterProvider).addAudits(anyString(),anyList());

        packetWriter.addAudits(id, auditList, source, process);
    }


    @Test
    public void testPersistPacket() {
        List<PacketInfo> packetInfos = new ArrayList<>();
        Mockito.when(packetWriterProvider.persistPacket(id, "0.2", "schema", source, process, null, null, true)).thenReturn(packetInfos);

        List<PacketInfo> result = packetWriter.persistPacket(id, "0.2", "schema", source, process, null, null, true);

        assertTrue(result.equals(packetInfos));
    }

    @Test
    public void testCreatePacket() {
        PacketDto packetDto = new PacketDto();
        packetDto.setId(id);
        packetDto.setProcess(process);
        packetDto.setSource(source);
        packetDto.setAudits(new ArrayList<>());
        packetDto.setBiometrics(new HashMap<>());
        packetDto.setDocuments(new HashMap<>());
        packetDto.setFields(new HashMap<>());
        packetDto.setMetaInfo(new HashMap<>());
        packetDto.setSchemaJson("schemajson");
        packetDto.setSchemaVersion("0.2");

        PacketInfo packetInfo = new PacketInfo();
        packetInfo.setId(id);
        packetInfo.setSource(source);

        List<PacketInfo> packetInfos = new ArrayList<>();
        packetInfos.add(packetInfo);

        Mockito.doNothing().when(packetWriterProvider).setField(anyString(),anyString(),anyString());
        Mockito.doNothing().when(packetWriterProvider).setFields(anyString(),anyMap());
        Mockito.doNothing().when(packetWriterProvider).setDocument(anyString(), anyString(), any());
        Mockito.doNothing().when(packetWriterProvider).setBiometric(anyString(), anyString(), any());
        Mockito.doNothing().when(packetWriterProvider).addMetaInfo(anyString(),anyString(),anyString());
        Mockito.doNothing().when(packetWriterProvider).addMetaInfo(anyString(),anyMap());
        Mockito.doNothing().when(packetWriterProvider).addAudit(anyString(),anyMap());
        Mockito.doNothing().when(packetWriterProvider).addAudits(anyString(),anyList());
        Mockito.when(packetWriterProvider.persistPacket(anyString(), anyString(),
                anyString(), anyString(), anyString(), any(), any(), anyBoolean())).thenReturn(packetInfos);

        List<PacketInfo> result = packetWriter.createPacket(packetDto);

        assertTrue(result.equals(packetInfos));
    }

    @Test
    public void testException() {
        PacketDto packetDto = new PacketDto();
        packetDto.setId(id);
        packetDto.setProcess(process);
        packetDto.setSource(source);
        packetDto.setAudits(new ArrayList<>());
        packetDto.setBiometrics(new HashMap<>());
        packetDto.setDocuments(new HashMap<>());
        packetDto.setFields(new HashMap<>());
        packetDto.setMetaInfo(new HashMap<>());
        packetDto.setSchemaJson("schemajson");
        packetDto.setSchemaVersion("0.2");

        PacketInfo packetInfo = new PacketInfo();
        packetInfo.setId(id);
        packetInfo.setSource(source);

        List<PacketInfo> packetInfos = new ArrayList<>();
        packetInfos.add(packetInfo);

        Mockito.doNothing().when(packetWriterProvider).setField(anyString(),anyString(),anyString());
        Mockito.doNothing().when(packetWriterProvider).setFields(anyString(),anyMap());
        Mockito.doNothing().when(packetWriterProvider).setDocument(anyString(), anyString(), any());
        Mockito.doNothing().when(packetWriterProvider).setBiometric(anyString(), anyString(), any());
        Mockito.doNothing().when(packetWriterProvider).addMetaInfo(anyString(),anyString(),anyString());
        Mockito.doNothing().when(packetWriterProvider).addMetaInfo(anyString(),anyMap());
        Mockito.doNothing().when(packetWriterProvider).addAudit(anyString(),anyMap());
        Mockito.doNothing().when(packetWriterProvider).addAudits(anyString(),anyList());
        Mockito.when(packetWriterProvider.persistPacket(anyString(), anyString(),
                anyString(), anyString(), anyString(), any(), any(), anyBoolean())).thenThrow(new PacketCreatorException("",""));

        List<PacketInfo> result = packetWriter.createPacket(packetDto);

        assertTrue(result == null);
    }

    @Test(expected = NoAvailableProviderException.class)
    public void testProviderException() {
        PowerMockito.when(PacketHelper.isSourceAndProcessPresent(anyString(),anyString(),anyString(),any())).thenReturn(false);

        packetWriter.setField(id, "name", "mono", source, process);
    }
    
    @Test
    public void testAddTags() {
    	TagDto tagDto=new TagDto();
    	tagDto.setId(id);
    	Map<String, String> tags = new HashMap<>();
        tags.put("test", "testValue");
    	tagDto.setTags(tags);
    	Mockito.when(packetKeeper.addTags(any())).thenReturn(tags);

       	Map<String, String> expectedTags= packetWriter.addTags(tagDto);

        assertEquals(expectedTags,tags); 
    }
    @Test
    public void testUpdateTags() {
    	TagDto tagDto=new TagDto();
    	tagDto.setId(id);
    	Map<String, String> tags = new HashMap<>();
        tags.put("test", "testValue");
    	tagDto.setTags(tags);
    	Mockito.when(packetKeeper.addorUpdate(any())).thenReturn(tags);


       	Map<String, String> expectedTags= packetWriter.addorUpdate(tagDto);

        assertEquals(expectedTags,tags); 
    }
    @Test
    public void testDeleteTags() {
    	TagRequestDto tagDto=new TagRequestDto();
    	tagDto.setId(id);
    	List<String> tags = new ArrayList<>();
    	tags.add("test");
    	tagDto.setTagNames(tags);
		packetWriter.deleteTags(tagDto);

    }
    
}
