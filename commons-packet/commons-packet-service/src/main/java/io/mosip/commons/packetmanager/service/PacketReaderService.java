package io.mosip.commons.packetmanager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import io.mosip.commons.khazana.dto.ObjectDto;
import io.mosip.commons.packet.dto.TagResponseDto;
import io.mosip.commons.packet.facade.PacketReader;
import io.mosip.commons.packet.util.PacketManagerLogger;
import io.mosip.commons.packetmanager.dto.BiometricsDto;
import io.mosip.commons.packetmanager.dto.ContainerInfoDto;
import io.mosip.commons.packetmanager.dto.InfoResponseDto;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.StringUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PacketReaderService {

    private static Logger LOGGER = PacketManagerLogger.getLogger(PacketReaderService.class);
    private static final String VALUE = "value";
    private static final String INDIVIDUAL_BIOMETRICS = "individualBiometrics";
    private static final String IDENTITY = "identity";
    private String key = null;

    @Value("${config.server.file.storage.uri}")
    private String configServerUrl;

    @Value("${registration.processor.identityjson}")
    private String mappingjsonFileName;

    @Autowired
    private PacketReader packetReader;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public InfoResponseDto info(String id) {
        try {
            List<ObjectDto> allObjects = packetReader.info(id);
            List<ContainerInfoDto> containerInfoDtos = new ArrayList<>();
            for (ObjectDto o : allObjects) {
                if (!containerInfoDtos.stream().anyMatch(info -> info.getSource().equalsIgnoreCase(o.getSource()) && info.getProcess().equalsIgnoreCase(o.getProcess()))) {
                    ContainerInfoDto containerInfo = new ContainerInfoDto();
                    containerInfo.setSource(o.getSource());
                    containerInfo.setProcess(o.getProcess());
                    containerInfo.setLastModified(o.getLastModified());

                    //get demographic fields
                    Set<String> demographics = packetReader.getAllKeys(id, containerInfo.getSource(), containerInfo.getProcess());
                    // get biometrics
                    List<BiometricsDto> biometrics = null;
                    BiometricRecord br = packetReader.getBiometric(id, getKey(), Lists.newArrayList(), o.getSource(), o.getProcess(), false);
                    if (br != null && !CollectionUtils.isEmpty(br.getSegments())) {
                        Map<String, List<String>> biomap = new HashMap<>();
                        for (BIR b : br.getSegments()) {
                            String key = b.getBdbInfo().getType().iterator().next().value();
                            String subtype = b.getBdbInfo().getSubtype().stream().collect(Collectors.joining(" ")).strip();
                            if (biomap.get(key) == null)
                                biomap.put(key, StringUtils.isNotEmpty(subtype) ? Lists.newArrayList(subtype) : null);
                            else {
                                List<String> finalVal =  biomap.get(key);
                                finalVal.add(subtype);
                                biomap.put(key, finalVal);
                            }
                        }
                        biometrics = new ArrayList<>();
                        for (Map.Entry<String, List<String>> b : biomap.entrySet()) {
                            BiometricsDto bioDto = new BiometricsDto();
                            bioDto.setType(b.getKey());
                            bioDto.setSubtypes(b.getValue());
                            biometrics.add(bioDto);
                        }
                    }

                    // get tags
                    TagResponseDto tagResponse = packetReader.getTags(id, null);

                    containerInfo.setDemographics(demographics);
                    containerInfo.setBiometrics(biometrics);
                    containerInfo.setTags(tagResponse != null && tagResponse.getTags() != null ? tagResponse.getTags() : null);
                    containerInfoDtos.add(containerInfo);
                }
            }
            InfoResponseDto infoResponseDto = new InfoResponseDto();
            infoResponseDto.setApplicationId(id);
            infoResponseDto.setPacketId(id);
            infoResponseDto.setInfo(containerInfoDtos);
            return infoResponseDto;
        } catch (Exception e) {
            LOGGER.error(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id, ExceptionUtils.getStackTrace(e));
            throw new BaseUncheckedException(e.getMessage());
        }
    }

    private String getKey() throws IOException {
        if (key != null)
            return key;
        String mappingJsonString = restTemplate.getForObject(configServerUrl + "/" + mappingjsonFileName, String.class);
        JSONObject jsonObject = objectMapper.readValue(mappingJsonString, JSONObject.class);
        if(jsonObject == null)
            return null;
        LinkedHashMap<String, Object> identity = (LinkedHashMap) jsonObject.get(IDENTITY);
        LinkedHashMap<String, String> individualBio = (LinkedHashMap) identity.get(INDIVIDUAL_BIOMETRICS);
        key = individualBio.get(VALUE);
        return key;
    }

}
