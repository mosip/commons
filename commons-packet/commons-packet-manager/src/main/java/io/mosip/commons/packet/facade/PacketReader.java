package io.mosip.commons.packet.facade;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import io.mosip.commons.khazana.spi.ObjectStoreAdapter;
import io.mosip.commons.packet.constants.PacketUtilityErrorCodes;
import io.mosip.commons.packet.dto.Document;
import io.mosip.commons.packet.dto.TagResponseDto;
import io.mosip.commons.packet.exception.GetTagException;
import io.mosip.commons.packet.exception.NoAvailableProviderException;
import io.mosip.commons.packet.exception.ObjectStoreAdapterException;
import io.mosip.commons.packet.spi.IPacketReader;
import io.mosip.commons.packet.util.PacketHelper;
import io.mosip.commons.packet.util.PacketManagerLogger;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;

/**
 * The packet Reader facade
 */
@RefreshScope
@Component
public class PacketReader {

    private static final Logger LOGGER = PacketManagerLogger.getLogger(PacketReader.class);

	@Value("${packet.manager.account.name}")
	private String PACKET_MANAGER_ACCOUNT;

	@Autowired
	@Qualifier("SwiftAdapter")
	private ObjectStoreAdapter swiftAdapter;

	@Autowired
	@Qualifier("S3Adapter")
	private ObjectStoreAdapter s3Adapter;

	@Autowired
	@Qualifier("PosixAdapter")
	private ObjectStoreAdapter posixAdapter;

	@Value("${objectstore.adapter.name}")
	private String adapterName;

    @Autowired(required = false)
    @Qualifier("referenceReaderProviders")
    @Lazy
    private List<IPacketReader> referenceReaderProviders;

    /**
     * Get a field from identity file
     *
     * @param id      : the registration id
     * @param field   : field name to search
     * @param source  : the source packet. If not present return default
     * @param process : the process
     * @return String field
     */
    @PreAuthorize("hasRole('DATA_READ')")
    public String getField(String id, String field, String source, String process, boolean bypassCache) {
        LOGGER.info(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id,
                "getFields for fields : " + field + " source : " + source + " process : " + process);
        String value;
        if (bypassCache)
            value = getProvider(source, process).getField(id, field, source, process);
        else {
            Optional<Object> optionalValue = getAllFields(id, source, process).entrySet().stream().filter(m-> m.getKey().equalsIgnoreCase(field)).map(m -> m.getValue()).findAny();
            value = optionalValue.isPresent() ? optionalValue.get().toString() : null;
        }
        return value;
    }

    /**
     * Get fields from identity file
     *
     * @param id      : the registration id
     * @param fields  : fields to search
     * @param source  : the source packet. If not present return default
     * @param process : the process
     * @return Map fields
     */
    @PreAuthorize("hasRole('DATA_READ')")
    public Map<String, String> getFields(String id, List<String> fields, String source, String process, boolean bypassCache) {
        LOGGER.info(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id,
                "getFields for fields : " + fields.toString() + " source : " + source + " process : " + process);
        Map<String, String> values;
        if (bypassCache)
            values = getProvider(source, process).getFields(id, fields, source, process);
        else {
            values = getAllFields(id, source, process).entrySet()
                    .stream().filter(m -> fields.contains(m.getKey())).collect(Collectors.toMap(m -> m.getKey(), m -> m.getValue() != null ? m.getValue().toString() : null));
        }
        return values;
    }

    /**
     * Get document by registration id, document name, source and process
     *
     * @param id           : the registration id
     * @param documentName : the document name
     * @param source       : the source packet. If not present return default
     * @param process      : the process
     * @return Document : document information
     */
    @PreAuthorize("hasRole('DOCUMENT_READ')")
    @Cacheable(value = "packets", key = "'documents'.concat('-').concat(#id).concat('-').concat(#documentName).concat('-').concat(#source).concat('-').concat(#process)")
    public Document getDocument(String id, String documentName, String source, String process) {
        LOGGER.info(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id,
                "getDocument for documentName : " + documentName + " source : " + source + " process : " + process);
        return getProvider(source, process).getDocument(id, documentName, source, process);
    }

    /**
     * Get biometric information by registration id, document name, source and process
     *
     * @param id         : the registration id
     * @param person     : The person (ex - applicant, operator, supervisor, introducer etc)
     * @param modalities : list of biometric modalities
     * @param source     : the source packet. If not present return default
     * @param process    : the process
     * @return BiometricRecord : the biometric record
     */
    @PreAuthorize("hasRole('BIOMETRIC_READ')")
    @Cacheable(value = "packets", key = "'biometrics'.concat('-').#id.concat('-').concat(#person).concat('-').concat(#modalities).concat('-').concat(#source).concat('-').concat(#process)", condition = "#bypassCache == false")
    public BiometricRecord getBiometric(String id, String person, List<String> modalities, String source, String process, boolean bypassCache) {
        LOGGER.info(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id,
                "getBiometric for source : " + source + " process : " + process);
        return getProvider(source, process).getBiometric(id, person, modalities, source, process);
    }

    /**
     * Get packet meta information by registration id, source and process
     *
     * @param id      : the registration id
     * @param source  : the source packet. If not present return default
     * @param process : the process
     * @return Map fields
     */
    @PreAuthorize("hasRole('METADATA_READ')")
    @Cacheable(value = "packets", key = "{'metaInfo'.concat('-').concat(#id).concat('-').concat(#source).concat('-').concat(#process)}", condition = "#bypassCache == false")
    public Map<String, String> getMetaInfo(String id, String source, String process, boolean bypassCache) {
        LOGGER.info(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id,
                "getMetaInfo for source : " + source + " process : " + process);
        return getProvider(source, process).getMetaInfo(id, source, process);
    }

    /**
     * Get all fields from packet by id, source and process
     *
     * @param id      : the registration id
     * @param source  : the source packet. If not present return default
     * @param process : the process
     * @return Map fields
     */
    private Map<String, Object> getAllFields(String id, String source, String process) {
        LOGGER.info(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id,
                "getAllFields for source : " + source + " process : " + process);
        return getProvider(source, process).getAll(id, source, process);
    }

    /**
     * Get all fields from packet by id, source and process
     *
     * @param id      : the registration id
     * @param source  : the source packet. If not present return default
     * @param process : the process
     * @return Map fields
     */
    @Cacheable(value = "packets", key = "{#id.concat('-').concat(#source).concat('-').concat(#process)}", condition = "#bypassCache == false")
    public List<Map<String, String>> getAudits(String id, String source, String process, boolean bypassCache) {
        LOGGER.info(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id,
                "getAllFields for source : " + source + " process : " + process);
        return getProvider(source, process).getAuditInfo(id, source, process);
    }

    public boolean validatePacket(String id, String source, String process) {
        return getProvider(source, process).validatePacket(id, source, process);
    }

    private IPacketReader getProvider(String source, String process) {
        LOGGER.info(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, null,
                "getProvider for source : " + source + " process : " + process);
        IPacketReader provider = null;
        if (referenceReaderProviders != null && !referenceReaderProviders.isEmpty()) {
            Optional<IPacketReader> refProvider = referenceReaderProviders.stream().filter(refPr ->
                    (PacketHelper.isSourceAndProcessPresent(refPr.getClass().getName(), source, process, PacketHelper.Provider.READER))).findAny();
            if (refProvider.isPresent() && refProvider.get() != null)
                provider = refProvider.get();
        }

        if (provider == null) {
            LOGGER.error(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, null,
                    "No available provider found for source : " + source + " process : " + process);
            throw new NoAvailableProviderException();
        }

        return provider;
    }

	@Cacheable(value = "tags", key = "{#id}", condition = "#tagNames == null")
	public TagResponseDto getTags(String id, List<String> tagNames) {
		TagResponseDto tagResponseDto = new TagResponseDto();
		try {
			Map<String, String> tags = new HashMap<String, String>();
			Map<String, String> existingTags = getAdapter().getTags(PACKET_MANAGER_ACCOUNT, id);
			if (tagNames != null && !tagNames.isEmpty()) {
				for (String tag : tagNames) {
					if (existingTags.containsKey(tag)) {
						tags.put(tag, existingTags.get(tag));
					} else {
						throw new GetTagException(PacketUtilityErrorCodes.TAG_NOT_FOUND.getErrorCode(),
								PacketUtilityErrorCodes.TAG_NOT_FOUND.getErrorMessage() + tag);
					}
				}
				tagResponseDto.setTags(tags);
			} else {
				tagResponseDto.setTags(existingTags);
			}

		} catch (Exception e) {
			LOGGER.error(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id,
					ExceptionUtils.getStackTrace(e));
			if (e instanceof BaseCheckedException) {
				BaseCheckedException ex = (BaseCheckedException) e;
				throw new GetTagException(ex.getErrorCode(), ex.getMessage());
			} else if (e instanceof BaseUncheckedException) {
				BaseUncheckedException ex = (BaseUncheckedException) e;
				throw new GetTagException(ex.getErrorCode(), ex.getMessage());
			}
			throw new GetTagException(e.getMessage());

		}

		return tagResponseDto;

	}

	private ObjectStoreAdapter getAdapter() {
		if (adapterName.equalsIgnoreCase(swiftAdapter.getClass().getSimpleName()))
			return swiftAdapter;
		else if (adapterName.equalsIgnoreCase(posixAdapter.getClass().getSimpleName()))
			return posixAdapter;
		else if (adapterName.equalsIgnoreCase(s3Adapter.getClass().getSimpleName()))
			return s3Adapter;
		else
			throw new ObjectStoreAdapterException();
	}
}
