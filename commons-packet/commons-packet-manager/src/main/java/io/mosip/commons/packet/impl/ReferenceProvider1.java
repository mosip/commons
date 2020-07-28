package io.mosip.commons.packet.impl;

import io.mosip.commons.packet.dto.Document;
import io.mosip.commons.packet.spi.IPacketReader;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ReferenceProvider1 implements IPacketReader {


    @Override
    public boolean validatePacket(String id, String process) {
        return false;
    }

    @Override
    public Map<String, Object> getAll(String id, String process) {
        return null;
    }

    @Override
    public String getField(String id, String field, String process) {
        return null;
    }

    @Override
    public Map<String, String> getFields(String id, List<String> fields, String process) {
        return null;
    }

    @Override
    public Document getDocument(String id, String documentName, String process) {
        return null;
    }

    @Override
    public BiometricRecord getBiometric(String id, String biometricSchemaField, List<BiometricType> modalities, String process) {
        return null;
    }

    @Override
    public Map<String, String> getMetaInfo(String id, String process) {
        return null;
    }
}
