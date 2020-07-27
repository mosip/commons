package io.mosip.commons.packet.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.mosip.commons.packet.constants.PacketManagerConstants.PROCESS;
import static io.mosip.commons.packet.constants.PacketManagerConstants.SOURCE;

@Component
public class PacketHelper {

    /** The providerConfig. */
    @Autowired
    private Map<String, String> configurations;

    @Value("${mosip.commons.packet.provider.registration.source}")
    private String sources;

    @Value("${mosip.commons.packet.provider.registration.process}")
    private String processes;

    public boolean isSourcePresent(String providerName, String providerSource) {
        List<String> sourceKeys = configurations.keySet().stream().filter(key -> key.contains(SOURCE)).collect(Collectors.toList());
        Optional<String> provider = sourceKeys.stream().filter(key -> key.contains(providerName)).findAny();
        Optional<Map.Entry<String, String>> source = configurations.entrySet().stream().filter(key -> key.getValue().contains(providerSource)).findAny();
        return (provider.isPresent() && provider.get() != null) && (source.isPresent() && source.get().getValue().equalsIgnoreCase(providerSource));

    }

    public boolean isProcessPresent(String providerName, String providerProcess) {
        List<String> providerKeys = configurations.keySet().stream().filter(key -> key.contains(PROCESS)).collect(Collectors.toList());
        Optional<String> provider = providerKeys.stream().filter(key -> key.contains(providerName)).findAny();
        Optional<Map.Entry<String, String>> process = configurations.entrySet().stream().filter(key -> key.getValue().contains(providerProcess)).findAny();
        return provider.isPresent() && provider.get() != null && (process.isPresent() && process.get().getValue().equalsIgnoreCase(providerProcess));
    }
}
