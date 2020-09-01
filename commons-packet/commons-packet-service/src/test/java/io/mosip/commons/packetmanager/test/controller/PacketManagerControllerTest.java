package io.mosip.commons.packetmanager.test.controller;

import com.google.common.collect.Lists;
import io.mosip.commons.packet.dto.Document;
import io.mosip.commons.packet.dto.packet.PacketDto;
import io.mosip.commons.packet.facade.PacketReader;
import io.mosip.commons.packet.facade.PacketWriter;
import io.mosip.commons.packetmanager.PacketServiceApp;
import io.mosip.commons.packetmanager.dto.BiometricRequestDto;
import io.mosip.commons.packetmanager.dto.DocumentDto;
import io.mosip.commons.packetmanager.dto.FieldDto;
import io.mosip.commons.packetmanager.dto.FieldDtos;
import io.mosip.commons.packetmanager.dto.InfoDto;
import io.mosip.commons.packetmanager.test.TestBootApplication;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.cbeffutil.impl.CbeffImpl;
import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.util.JsonUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestBootApplication.class)
@AutoConfigureMockMvc
public class PacketManagerControllerTest {

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PacketReader packetReader;

    @MockBean
    private PacketWriter packetWriter;

    @MockBean
    private CbeffImpl cbeff;


    private RequestWrapper<Object> request = new RequestWrapper<>();



    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    @WithUserDetails("reg-processor")
    public void testSearchField() throws Exception {
        String value = "value";
        FieldDto fieldDto = new FieldDto();
        fieldDto.setField("name");
        fieldDto.setBypassCache(false);
        fieldDto.setId("id");
        fieldDto.setProcess("NEW");
        fieldDto.setSource("REGISTRATION");

        Mockito.when(
                packetReader.getField(anyString(), anyString(), anyString(), anyString(), anyBoolean())).thenReturn(value);

        request.setRequest(fieldDto);

        this.mockMvc.perform(post("/searchField").contentType(MediaType.APPLICATION_JSON).content(JsonUtils.javaObjectToJsonString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails("reg-processor")
    public void testSearchFields() throws Exception {
        String value = "value";
        FieldDtos fieldDto = new FieldDtos();
        fieldDto.setFields(Lists.newArrayList("fullname", "email"));
        fieldDto.setBypassCache(false);
        fieldDto.setId("id");
        fieldDto.setProcess("NEW");
        fieldDto.setSource("REGISTRATION");

        Mockito.when(
                packetReader.getFields(anyString(), any(), anyString(), anyString(), anyBoolean())).thenReturn(new HashMap<>());

        request.setRequest(fieldDto);

        this.mockMvc.perform(post("/searchFields").contentType(MediaType.APPLICATION_JSON).content(JsonUtils.javaObjectToJsonString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails("reg-processor")
    public void testDocument() throws Exception {
        String value = "value";
        DocumentDto documentDto = new DocumentDto();
        documentDto.setDocumentName("document");
        documentDto.setId("id");
        documentDto.setProcess("NEW");
        documentDto.setSource("REGISTRATION");

        Mockito.when(
                packetReader.getDocument(anyString(), anyString(), anyString(), anyString())).thenReturn(new Document());

        request.setRequest(documentDto);

        this.mockMvc.perform(post("/document").contentType(MediaType.APPLICATION_JSON).content(JsonUtils.javaObjectToJsonString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails("reg-processor")
    public void testBiometrics() throws Exception {
        BiometricRequestDto biometricRequestDto = new BiometricRequestDto();
        biometricRequestDto.setPerson("applicant");
        biometricRequestDto.setId("id");
        biometricRequestDto.setProcess("NEW");
        biometricRequestDto.setSource("REGISTRATION");

        Mockito.when(
                packetReader.getBiometric(anyString(), anyString(), any(), anyString(), anyString(), anyBoolean())).thenReturn(new BiometricRecord());

        request.setRequest(biometricRequestDto);

        this.mockMvc.perform(post("/biometrics").contentType(MediaType.APPLICATION_JSON).content(JsonUtils.javaObjectToJsonString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails("reg-processor")
    public void testMetaInfo() throws Exception {
        InfoDto infoDto = new InfoDto();
        infoDto.setBypassCache(false);
        infoDto.setId("id");
        infoDto.setProcess("NEW");
        infoDto.setSource("REGISTRATION");

        Mockito.when(
                packetReader.getMetaInfo(anyString(), anyString(), anyString(), anyBoolean())).thenReturn(new HashMap<>());

        request.setRequest(infoDto);

        this.mockMvc.perform(post("/metaInfo").contentType(MediaType.APPLICATION_JSON).content(JsonUtils.javaObjectToJsonString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails("reg-processor")
    public void testAudits() throws Exception {
        InfoDto infoDto = new InfoDto();
        infoDto.setBypassCache(false);
        infoDto.setId("id");
        infoDto.setProcess("NEW");
        infoDto.setSource("REGISTRATION");

        Mockito.when(
                packetReader.getAudits(anyString(), anyString(), anyString(), anyBoolean())).thenReturn(new ArrayList<>());

        request.setRequest(infoDto);

        this.mockMvc.perform(post("/audits").contentType(MediaType.APPLICATION_JSON).content(JsonUtils.javaObjectToJsonString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails("reg-processor")
    public void testValidatePacket() throws Exception {
        InfoDto infoDto = new InfoDto();
        infoDto.setBypassCache(false);
        infoDto.setId("id");
        infoDto.setProcess("NEW");
        infoDto.setSource("REGISTRATION");

        Mockito.when(
                packetReader.validatePacket(anyString(), anyString(), anyString())).thenReturn(true);

        request.setRequest(infoDto);

        this.mockMvc.perform(post("/validatePacket").contentType(MediaType.APPLICATION_JSON).content(JsonUtils.javaObjectToJsonString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails("reg-processor")
    public void testCreatePacket() throws Exception {
        PacketDto packetDto = new PacketDto();
        packetDto.setId("id");
        packetDto.setProcess("NEW");
        packetDto.setSource("REGISTRATION");

        Mockito.when(
                packetWriter.createPacket(any(), anyBoolean())).thenReturn(new ArrayList<>());

        request.setRequest(packetDto);

        this.mockMvc.perform(put("/createPacket").contentType(MediaType.APPLICATION_JSON).content(JsonUtils.javaObjectToJsonString(request)))
                .andExpect(status().isOk());
    }


    @Test
    @WithUserDetails("reg-processor")
    public void testBaseUncheckedException() throws Exception {
        PacketDto packetDto = new PacketDto();
        packetDto.setId("id");
        packetDto.setProcess("NEW");
        packetDto.setSource("REGISTRATION");

        Mockito.when(
                packetWriter.createPacket(any(), anyBoolean())).thenThrow(new BaseUncheckedException("errorCode", "errorMessage"));

        request.setRequest(packetDto);

        this.mockMvc.perform(put("/createPacket").contentType(MediaType.APPLICATION_JSON).content(JsonUtils.javaObjectToJsonString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails("reg-processor")
    @Ignore
    public void testBaseCheckedException() throws Exception {
        InfoDto infoDto = new InfoDto();
        infoDto.setBypassCache(false);
        infoDto.setId("id");
        infoDto.setProcess("NEW");
        infoDto.setSource("REGISTRATION");

        Mockito.when(
                packetReader.getAudits(anyString(), anyString(), anyString(), anyBoolean())).thenThrow(new BaseCheckedException("errorCode", "errorMessage"));

        request.setRequest(infoDto);

        this.mockMvc.perform(post("/audits").contentType(MediaType.APPLICATION_JSON).content(JsonUtils.javaObjectToJsonString(request)))
                .andExpect(status().isOk());
    }
}
