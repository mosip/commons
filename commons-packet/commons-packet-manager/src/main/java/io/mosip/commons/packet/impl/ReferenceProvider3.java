package io.mosip.commons.packet.impl;

import io.mosip.commons.packet.dto.Document;
import io.mosip.commons.packet.dto.PacketInfo;
import io.mosip.commons.packet.dto.packet.AuditDto;
import io.mosip.commons.packet.exception.PacketCreatorException;
import io.mosip.commons.packet.spi.IPacketWriter;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.core.cbeffutil.entity.BIR;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.BIRType;
import org.json.JSONException;

import java.util.List;
import java.util.Map;

public class ReferenceProvider3 implements IPacketWriter {
    @Override
    public void setField(String id, String fieldName, String value) {

    }

    @Override
    public void setFields(String id, Map<String, String> fields) throws JSONException {

    }

    @Override
    public void setBiometric(String id, String fieldName, BiometricRecord biometricRecord) {

    }

    @Override
    public void setDocument(String id, String documentName, Document document) {

    }

    @Override
    public void setMetaInfo(String id, Map<String, String> metaInfo) {

    }


    @Override
    public PacketInfo persistPacket(String id, double version, String schemaJson) {
        return null;
    }
    @Override
    public void setAudits(String id, List<AuditDto> auditList) {

    }
}
