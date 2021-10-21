package io.mosip.commons.packet.test.keeper;

import com.google.common.collect.Lists;
import io.mosip.commons.khazana.dto.ObjectDto;
import io.mosip.commons.khazana.spi.ObjectStoreAdapter;
import io.mosip.commons.packet.constants.PacketManagerConstants;
import io.mosip.commons.packet.dto.Packet;
import io.mosip.commons.packet.dto.PacketInfo;
import io.mosip.commons.packet.dto.TagDto;
import io.mosip.commons.packet.exception.GetTagException;
import io.mosip.commons.packet.exception.PacketKeeperException;
import io.mosip.commons.packet.exception.TagCreationException;
import io.mosip.commons.packet.keeper.PacketKeeper;
import io.mosip.commons.packet.spi.IPacketCryptoService;
import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.util.DateUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;



import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@RunWith(SpringRunner.class)
public class PacketKeeperTest {

    @InjectMocks
    private PacketKeeper packetKeeper;

    @Mock
    @Qualifier("SwiftAdapter")
    private ObjectStoreAdapter swiftAdapter;

    @Mock
    @Qualifier("S3Adapter")
    private ObjectStoreAdapter s3Adapter;

    @Mock
    @Qualifier("PosixAdapter")
    private ObjectStoreAdapter posixAdapter;

    @Mock
    @Qualifier("OnlinePacketCryptoServiceImpl")
    private IPacketCryptoService onlineCrypto;

    /*@Mock
    private OfflinePacketCryptoServiceImpl offlineCrypto;*/

    private Packet packet;
    private PacketInfo packetInfo;

    private static final String id = "123457890";
    private static final String source = "source";
    private static final String process = "process";

    @Before
    public void setup() {
        ReflectionTestUtils.setField(packetKeeper, "cryptoName", onlineCrypto.getClass().getSimpleName());
        ReflectionTestUtils.setField(packetKeeper, "adapterName", swiftAdapter.getClass().getSimpleName());
        ReflectionTestUtils.setField(packetKeeper, "PACKET_MANAGER_ACCOUNT", "PACKET_MANAGER_ACCOUNT");

        packetInfo = new PacketInfo();
        packetInfo.setCreationDate(DateUtils.getCurrentDateTimeString());
        packetInfo.setEncryptedHash("yWxtW-jQihLntc3Bsgf6ayQwl0yGgD2IkWdedv2ZLCA");
        packetInfo.setId(id);
        packetInfo.setProcess(process);
        packetInfo.setSource(source);
        packetInfo.setSignature("sign");
        packetInfo.setSchemaVersion("0.1");
        packetInfo.setProviderVersion("1.0");
        packet = new Packet();
        packet.setPacket("packet".getBytes());
        packet.setPacketInfo(packetInfo);

        Map<String, Object> metaMap = new HashMap<>();
        metaMap.put(PacketManagerConstants.ID, id);
        metaMap.put(PacketManagerConstants.SOURCE, source);
        metaMap.put(PacketManagerConstants.PROCESS, process);
        metaMap.put(PacketManagerConstants.SIGNATURE, "signature");
        metaMap.put(PacketManagerConstants.ENCRYPTED_HASH, "yWxtW-jQihLntc3Bsgf6ayQwl0yGgD2IkWdedv2ZLCA");

        Mockito.when(onlineCrypto.encrypt(any(), any())).thenReturn("encryptedpacket".getBytes());
        Mockito.when(onlineCrypto.sign(any())).thenReturn("signed data".getBytes());
        Mockito.when(swiftAdapter.putObject(any(), any(), any(),any(), any(), any())).thenReturn(true);
        Mockito.when(swiftAdapter.addObjectMetaData(any(), any(), any(),any(), any(), any())).thenReturn(metaMap);

        InputStream is = new ByteArrayInputStream("input".getBytes());

        Mockito.when(swiftAdapter.getObject(any(), any(),any(), any(), any())).thenReturn(is);
        Mockito.when(onlineCrypto.decrypt(any(), any())).thenReturn("decryptedpacket".getBytes());
        Mockito.when(swiftAdapter.getMetaData(any(), any(),any(), any(), any())).thenReturn(metaMap);
        Mockito.when(onlineCrypto.verify(any(), any())).thenReturn(true);
        Map<String, String> tagsMap = new HashMap<>();
        tagsMap.put("osivalidation", "pass");
        Mockito.when(swiftAdapter.getTags(any(), any())).thenReturn(tagsMap);
     
    }

    @Test
    public void testPutPacketSuccess() throws PacketKeeperException {
        PacketInfo packetInfo = packetKeeper.putPacket(packet);

        assertTrue(packetInfo.getId().equals(id));
        assertTrue(packetInfo.getSource().equals(source));
        assertTrue(packetInfo.getProcess().equals(process));
    }

    @Test(expected = PacketKeeperException.class)
    public void testPutPacketException() throws PacketKeeperException {
        Mockito.when(onlineCrypto.encrypt(any(), any())).thenThrow(new BaseUncheckedException("code","message"));

        packetKeeper.putPacket(packet);
    }

    @Test(expected = PacketKeeperException.class)
    public void testObjectStoreAdapterException() throws PacketKeeperException {
        ReflectionTestUtils.setField(packetKeeper, "adapterName", "wrongAdapterName");

        packetKeeper.putPacket(packet);
    }

    @Test(expected = PacketKeeperException.class)
    public void testCryptoException() throws PacketKeeperException {
        ReflectionTestUtils.setField(packetKeeper, "cryptoName", "wrongname");

        packetKeeper.putPacket(packet);
    }

    @Test
    public void testGetPacketSuccess() throws PacketKeeperException {
        Packet result = packetKeeper.getPacket(packetInfo);

        assertTrue(result.getPacketInfo().getId().equals(id));
        assertTrue(result.getPacketInfo().getSource().equals(source));
        assertTrue(result.getPacketInfo().getProcess().equals(process));
    }

    @Test(expected = PacketKeeperException.class)
    public void testGetPacketFailure() throws PacketKeeperException {
        Mockito.when(swiftAdapter.getObject(any(), any(), any(), any(), any())).thenThrow(new BaseUncheckedException("code","message"));

        packetKeeper.getPacket(packetInfo);
    }

    @Test(expected = PacketKeeperException.class)
    @Ignore
    public void testPacketIntegrityFailure() throws PacketKeeperException {
        Mockito.when(onlineCrypto.verify(any(), any())).thenReturn(false);

        packetKeeper.getPacket(packetInfo);
    }
    @Test
    public void testAddTags() {
    	TagDto tagDto=new TagDto();
    	tagDto.setId(id);
    	Map<String, String> tags = new HashMap<>();
    	tags.put("test", "testValue");
    	tagDto.setTags(tags);
    	Mockito.when(swiftAdapter.addTags(any(), any(),any())).thenReturn(tags);
        Map<String,String> map = packetKeeper.addTags(tagDto);
        assertEquals(tags, map);

    }
    @Test(expected = TagCreationException.class)
    public void testTagAlreadyExists() {
    	TagDto tagDto=new TagDto();
    	tagDto.setId(id);
    	Map<String, String> tags = new HashMap<>();
        tags.put("osivalidation", "pass");
    	tagDto.setTags(tags);
    	Mockito.when(swiftAdapter.addTags(any(), any(),any())).thenReturn(tags);
        packetKeeper.addTags(tagDto);
      

    }
    @Test(expected = TagCreationException.class)
    public void testAddTagsException() {
    	TagDto tagDto=new TagDto();
    	tagDto.setId(id);
    	Map<String, String> tags = new HashMap<>();
    	tags.put("test", "testValue");
    	tagDto.setTags(tags);
    	Mockito.when(swiftAdapter.addTags(any(), any(),any())).thenThrow(new BaseUncheckedException("code","message"));
        Map<String,String> map = packetKeeper.addTags(tagDto);
        assertEquals(tags, map);

    }
    @Test
    public void testUpdateTags() {
    	TagDto tagDto=new TagDto();
    	tagDto.setId(id);
    	Map<String, String> tags = new HashMap<>();
    	tags.put("test", "testValue");
    	tagDto.setTags(tags);
    	Mockito.when(swiftAdapter.addTags(any(), any(),any())).thenReturn(tags);
        Map<String,String> map = packetKeeper.updateTags(tagDto);
        assertEquals(tags, map);

    }
    @Test(expected = TagCreationException.class)
    public void testUpdateTagsException() {
    	TagDto tagDto=new TagDto();
    	tagDto.setId(id);
    	Map<String, String> tags = new HashMap<>();
    	tags.put("test", "testValue");
    	tagDto.setTags(tags);
    	Mockito.when(swiftAdapter.addTags(any(), any(),any())).thenThrow(new BaseUncheckedException("code","message"));
        Map<String,String> map = packetKeeper.updateTags(tagDto);
        assertEquals(tags, map);

    }
    @Test
    public void testGetTags() {
        List<String> tagNames=new ArrayList<>();
        tagNames.add("osivalidation");
    	
        Map<String,String> map = packetKeeper.getTags(id,tagNames);
        assertEquals(map.get("osivalidation"), "pass");

    }
    @Test(expected = GetTagException.class)
    public void testGetTagNotFound() {
        List<String> tagNames=new ArrayList<>();
        tagNames.add("test");
        packetKeeper.getTags(id,tagNames);

    }
    @Test(expected = GetTagException.class)
    public void testGetTagsException() {
        List<String> tagNames=new ArrayList<>();
        tagNames.add("osivalidation");
        Mockito.when(swiftAdapter.getTags(any(), any())).thenThrow(new BaseUncheckedException("code","message"));
        packetKeeper.getTags(id,tagNames);


    }

    @Test
    public void testgetAll() {
        ObjectDto objectDto = new ObjectDto("source1", "process1", "object1", new Date());
        ObjectDto objectDto2 = new ObjectDto("source2", "process2", "object2", new Date());
        ObjectDto objectDto3 = new ObjectDto("source3", "process3", "object3", new Date());
        List<ObjectDto> objectDtos = Lists.newArrayList(objectDto, objectDto2, objectDto3);

        Mockito.when(swiftAdapter.getAllObjects(anyString(), anyString())).thenReturn(objectDtos);

        List<ObjectDto> result = packetKeeper.getAll(id);
        assertEquals(objectDtos.size(), result.size());

    }
}


