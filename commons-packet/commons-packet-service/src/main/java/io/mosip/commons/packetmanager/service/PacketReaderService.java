package io.mosip.commons.packetmanager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import io.mosip.commons.khazana.dto.ObjectDto;
import io.mosip.commons.packet.dto.TagResponseDto;
import io.mosip.commons.packet.facade.PacketReader;
import io.mosip.commons.packet.util.PacketManagerLogger;
import io.mosip.commons.packetmanager.constant.DefaultStrategy;
import io.mosip.commons.packetmanager.dto.BiometricsDto;
import io.mosip.commons.packetmanager.dto.ContainerInfoDto;
import io.mosip.commons.packetmanager.dto.InfoResponseDto;
import io.mosip.commons.packetmanager.dto.SourceProcessDto;
import io.mosip.commons.packetmanager.exception.SourceNotPresentException;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PacketReaderService {

    private static Logger LOGGER = PacketManagerLogger.getLogger(PacketReaderService.class);
    private static final String VALUE = "value";
    private static final String INDIVIDUAL_BIOMETRICS = "individualBiometrics";
    private static final String IDENTITY = "identity";
    private static final String DOCUMENTS = "documents";
    public static final String META_INFO = "metaInfo";
    public static final String AUDITS = "audits";
    private static final String SOURCE = "source";
    private static final String PROCESS = "process";
    private static final String PROVIDER = "provider";
    private String key = null;
    private static final String sourceInitial = "source:";
    private static final String processInitial = "process:";
    private JSONObject mappingJson = null;

    @Value("${config.server.file.storage.uri}")
    private String configServerUrl;

    @Value("${registration.processor.identityjson}")
    private String mappingjsonFileName;

    @Value("${packetmanager.default.read.strategy}")
    private String defaultStrategy;

    @Value("${packetmanager.default.priority}")
    private String defaultPriority;

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

                    containerInfo.setDemographics(demographics);
                    containerInfo.setBiometrics(biometrics);
                    containerInfoDtos.add(containerInfo);
                }
            }
            // get tags
            TagResponseDto tagResponse = packetReader.getTags(id, null);

            InfoResponseDto infoResponseDto = new InfoResponseDto();
            infoResponseDto.setApplicationId(id);
            infoResponseDto.setPacketId(id);
            infoResponseDto.setInfo(containerInfoDtos);
            infoResponseDto.setTags(tagResponse != null && tagResponse.getTags() != null ? tagResponse.getTags() : null);
            return infoResponseDto;
        } catch (Exception e) {
            LOGGER.error(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id, ExceptionUtils.getStackTrace(e));
            throw new BaseUncheckedException(e.getMessage());
        }
    }

    private String getKey() throws IOException {
        if (key != null)
            return key;
        JSONObject jsonObject = getMappingJsonFile();
        if(jsonObject == null)
            return null;
        LinkedHashMap<String, String> individualBio = (LinkedHashMap) jsonObject.get(INDIVIDUAL_BIOMETRICS);
        key = individualBio.get(VALUE);
        return key;
    }


    public String getSource(String id, String source, String process) {
        if (StringUtils.isEmpty(source)) {
            try {
                if (defaultStrategy.equalsIgnoreCase(DefaultStrategy.DEFAULT_PRIORITY.getValue())) {
                    source = searchInMappingJson(id, process);
                    if (source == null)
                        source = getDefaultSource(process);
                } else {
                    throw new SourceNotPresentException();
                }
            } catch (Exception e) {
                throw new SourceNotPresentException(e);
            }

        }
        return source;
    }

    public SourceProcessDto getSourceAndProcess(String id, String field, String source, String process) {
        SourceProcessDto sourceProcessDto = null;
        if (StringUtils.isEmpty(source)) {
            try {
                if (defaultStrategy.equalsIgnoreCase(DefaultStrategy.DEFAULT_PRIORITY.getValue())) {
                    InfoResponseDto infoResponseDto = info(id);
                    ContainerInfoDto containerInfoDto = findPriority(field, process, infoResponseDto);
                    if (containerInfoDto == null)
                        return null;
                    sourceProcessDto = new SourceProcessDto(containerInfoDto.getSource(), containerInfoDto.getProcess());
                }
            } catch (Exception e) {
                throw new SourceNotPresentException(e);
            }

        } else {
            sourceProcessDto = new SourceProcessDto(source, process);
        }
        return sourceProcessDto;
    }

    public ContainerInfoDto findPriority(String field, String process, InfoResponseDto infoResponseDto) throws IOException {
        if (infoResponseDto.getInfo().size() == 1)
            return infoResponseDto.getInfo().iterator().next();
        else
            return getContainerInfo(infoResponseDto, field);
    }

    private ContainerInfoDto getContainerInfo(InfoResponseDto infoResponseDto, String field) {
        if (org.apache.commons.lang.StringUtils.isNotEmpty(defaultPriority)) {
            String[] val = defaultPriority.split(",");
            if (val != null && val.length > 0) {
                for (String value : val) {
                    String[] str = value.split("/");
                    if (str != null && str.length > 0 && str[0].startsWith(sourceInitial)) {
                        String sourceStr = str[0].substring(sourceInitial.length());
                        String processStr = str[1].substring(processInitial.length());
                        for (String process : processStr.split("\\|")) {
                            Optional<ContainerInfoDto> containerDto = infoResponseDto.getInfo().stream().filter(info ->
                                    info.getDemographics().contains(field) && info.getSource().equalsIgnoreCase(sourceStr)
                            && info.getProcess().equalsIgnoreCase(process)).findAny();
                            // if source is present then get from that source or else continue searching
                            if (containerDto.isPresent()) {
                                return containerDto.get();
                            } else
                                continue;
                        }
                    }
                }
            }
        }
        return null;
    }

    private String getDefaultSource(String process) {
        if (org.apache.commons.lang.StringUtils.isNotEmpty(defaultPriority)) {
            String[] val = defaultPriority.split(",");
            if (val != null && val.length > 0) {
                for (String value : val) {
                    String[] str = value.split("/");
                    if (str != null && str.length > 0 && str[0].startsWith(sourceInitial)) {
                        String sourceStr = str[0].substring(sourceInitial.length());
                        String processStr = str[1].substring(processInitial.length());
                        String[] processes = processStr.split("\\|");
                        if (Arrays.stream(processes).filter(p -> p.equalsIgnoreCase(process)).findAny().isPresent())
                            return sourceStr;
                    }
                }
            }
        } else
            throw new SourceNotPresentException();
        return null;
    }

    public String getSourceFromIdField(String process, String idField) throws IOException {
        JSONObject jsonObject = getMappingJsonFile();
        for (Object key : jsonObject.keySet()) {
            LinkedHashMap hMap = (LinkedHashMap) jsonObject.get(key);
            String value = (String) hMap.get(VALUE);
            if (value != null && value.contains(idField)) {
                return getSource(jsonObject, process, key.toString());
            }
        }
        return null;
    }

    public String searchInMappingJson(String idField, String process) throws IOException {
        if (idField != null) {
            JSONObject jsonObject = getMappingJsonFile();
            for (Object key : jsonObject.keySet()) {
                LinkedHashMap hMap = (LinkedHashMap) jsonObject.get(key);
                String value = (String) hMap.get(VALUE);
                if (value != null && value.contains(idField)) {
                    return getSource(jsonObject, process, key.toString());
                }
            }
        }
        return null;
    }

    private String getSource(JSONObject jsonObject, String process, String field) {
        String source = null;
        Object obj = field == null ? jsonObject.get(PROVIDER) : getField(jsonObject, field);
        if (obj != null && obj instanceof ArrayList) {
            List<String> providerList = (List) obj;
            for (String value : providerList) {
                String[] values = value.split(",");
                for (String provider : values) {
                    if (provider != null) {
                        if (provider.startsWith(PROCESS) && provider.contains(process)) {
                            for (String val : values) {
                                if (val.startsWith(SOURCE)) {
                                    return val.replace(SOURCE + ":", "").trim();
                                }
                            }
                        }
                    }
                }
            }
        }

        return source;
    }

    private Object getField(JSONObject jsonObject, String field) {
        LinkedHashMap lm = (LinkedHashMap) jsonObject.get(field);
        return lm.get(PROVIDER);
    }

    private static JSONObject getJSONObject(JSONObject jsonObject, Object key) {
        if(jsonObject == null)
            return null;
        LinkedHashMap identity = (LinkedHashMap) jsonObject.get(key);
        return identity != null ? new JSONObject(identity) : null;
    }

    private JSONObject getMappingJsonFile() throws IOException {
        if (mappingJson != null)
            return mappingJson;

        String mappingJsonString = restTemplate.getForObject(configServerUrl + "/" + mappingjsonFileName, String.class);
        JSONObject jsonObject = objectMapper.readValue(mappingJsonString, JSONObject.class);
        LinkedHashMap combinedMap = new LinkedHashMap();
        combinedMap.putAll((Map) jsonObject.get(IDENTITY));
        combinedMap.putAll((Map) jsonObject.get(DOCUMENTS));
        combinedMap.put(META_INFO, jsonObject.get(META_INFO));
        combinedMap.put(AUDITS, jsonObject.get(AUDITS));
        mappingJson = new JSONObject(combinedMap);
        return mappingJson;
    }

}
