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

    public boolean isSourcePresent(String providerName) {
        List<String> sourceKeys = configurations.keySet().stream().filter(key -> key.contains(SOURCE)).collect(Collectors.toList());
        Optional<String> source = sourceKeys.stream().filter(key -> key.contains(providerName)).findAny();
        return source.isPresent() && source.get() != null;

    }

    public boolean isProcessPresent(String providerName) {
        List<String> providerKeys = configurations.keySet().stream().filter(key -> key.contains(PROCESS)).collect(Collectors.toList());
        Optional<String> provider = providerKeys.stream().filter(key -> key.contains(providerName)).findAny();
        return provider.isPresent() && provider.get() != null;
    }
}
