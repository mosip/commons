package io.mosip.commons.packetmanager.test.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;

import io.mosip.commons.packet.dto.TagDto;
import io.mosip.commons.packet.dto.TagRequestDto;
import io.mosip.commons.packet.dto.TagResponseDto;
import io.mosip.commons.packet.exception.TagCreationException;
import io.mosip.commons.packet.exception.TagDeletionException;
import io.mosip.commons.packet.facade.PacketReader;
import io.mosip.commons.packet.facade.PacketWriter;
import io.mosip.commons.packetmanager.service.PacketWriterService;
import io.mosip.kernel.core.exception.BaseUncheckedException;

@RunWith(SpringRunner.class)
public class PacketWriterServiceTest {
	@Mock
    private PacketReader packetReader;
    
	@Mock
    private PacketWriter packetWriter;
	
    @InjectMocks
    private  PacketWriterService  packetWriterService;
	
	@Before
    public void setup() {
		 Map<String, String> tags = new HashMap<>();
	        tags.put("test", "testValue");
	    	 Mockito.when(packetReader.getTags(anyString())).thenReturn(tags);
	    	
	}
	
	 @Test
	 public void testAddTagsSuccess() {
		 TagDto tagDto=new TagDto();
		 tagDto.setId("id");
		 Map<String, String> tags = new HashMap<>();
	     tags.put("testtag", "testValue");
	     tagDto.setTags(tags);
	     Mockito.when(packetWriter.addTags(any())).thenReturn(tags);
	     TagResponseDto tagResponseDto= packetWriterService.addTags(tagDto);
	     assertEquals(tagResponseDto.getTags(), tags);
	 }
	 @Test(expected = TagCreationException.class)
	 public void testTagAlreadyExists() {
		TagDto tagDto = new TagDto();
		tagDto.setId("id");
		Map<String, String> tags = new HashMap<>();
		tags.put("test", "testValue");
		tagDto.setTags(tags);
		Mockito.when(packetWriter.addTags(any())).thenReturn(tags);
		packetWriterService.addTags(tagDto);
	}
		
	@Test(expected = TagCreationException.class)
	public void testAddTagsException() {
		TagDto tagDto = new TagDto();
		tagDto.setId("id");
		Map<String, String> tags = new HashMap<>();
		tags.put("testtag", "testValue");
		tagDto.setTags(tags);
		Mockito.when(packetWriter.addTags(any())).thenThrow(new BaseUncheckedException("code", "message"));
		packetWriterService.addTags(tagDto);
	    }

	@Test
	public void testUpdateTagsSuccess() {
		TagDto tagDto = new TagDto();
		tagDto.setId("id");
		Map<String, String> tags = new HashMap<>();
		tags.put("test", "testValueChanges");
		tagDto.setTags(tags);
		Mockito.when(packetWriter.addTags(any())).thenReturn(tags);
		TagResponseDto tagResponseDto = packetWriterService.updateTags(tagDto);
		assertEquals(tagResponseDto.getTags(), tags);
	}

	@Test(expected = TagCreationException.class)
	public void testUpdateTagsException() {
		TagDto tagDto = new TagDto();
		tagDto.setId("id");
		Map<String, String> tags = new HashMap<>();
		tags.put("testtag", "testValue");
		tagDto.setTags(tags);
		Mockito.when(packetWriter.addTags(any())).thenThrow(new BaseUncheckedException("code", "message"));
		packetWriterService.updateTags(tagDto);
	}

	@Test
	public void testDeleteTagsSuccess() {
		TagRequestDto tagRequestDto = new TagRequestDto();
		tagRequestDto.setId("id");
		List<String> tagNames = new ArrayList<String>();
		tagNames.add("test");
		tagRequestDto.setTagNames(tagNames);
		packetWriterService.deleteTags(tagRequestDto);

	}

	@Test(expected = TagDeletionException.class)
	public void testDeleteTagsException() {
		TagRequestDto tagRequestDto = new TagRequestDto();
		tagRequestDto.setId("id");
		List<String> tagNames = new ArrayList<String>();
		tagNames.add("test");
		tagRequestDto.setTagNames(tagNames);
		Mockito.doThrow(new BaseUncheckedException("code", "message")).when(packetWriter).deleteTags(any());

		packetWriterService.deleteTags(tagRequestDto);

	}
}
