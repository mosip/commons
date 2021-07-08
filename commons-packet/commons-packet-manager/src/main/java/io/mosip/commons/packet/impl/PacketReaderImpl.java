package io.mosip.commons.packet.impl;

import static io.mosip.commons.packet.constants.PacketManagerConstants.FORMAT;
import static io.mosip.commons.packet.constants.PacketManagerConstants.ID;
import static io.mosip.commons.packet.constants.PacketManagerConstants.IDENTITY;
import static io.mosip.commons.packet.constants.PacketManagerConstants.LABEL;
import static io.mosip.commons.packet.constants.PacketManagerConstants.META_INFO_OPERATIONS_DATA;
import static io.mosip.commons.packet.constants.PacketManagerConstants.TYPE;
import static io.mosip.commons.packet.constants.PacketManagerConstants.VALUE;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.core.util.JsonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.commons.packet.constants.PacketManagerConstants;
import io.mosip.commons.packet.dto.Document;
import io.mosip.commons.packet.dto.Packet;
import io.mosip.commons.packet.dto.PacketInfo;
import io.mosip.commons.packet.exception.ApiNotAccessibleException;
import io.mosip.commons.packet.exception.GetAllIdentityException;
import io.mosip.commons.packet.exception.GetAllMetaInfoException;
import io.mosip.commons.packet.exception.GetBiometricException;
import io.mosip.commons.packet.exception.GetDocumentException;
import io.mosip.commons.packet.exception.PacketDecryptionFailureException;
import io.mosip.commons.packet.exception.PacketKeeperException;
import io.mosip.commons.packet.exception.PacketValidationFailureException;
import io.mosip.commons.packet.keeper.PacketKeeper;
import io.mosip.commons.packet.spi.IPacketReader;
import io.mosip.commons.packet.util.IdSchemaUtils;
import io.mosip.commons.packet.util.PacketManagerHelper;
import io.mosip.commons.packet.util.PacketManagerLogger;
import io.mosip.commons.packet.util.PacketValidator;
import io.mosip.commons.packet.util.ZipUtils;
import io.mosip.kernel.biometrics.commons.CbeffValidator;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;


@RefreshScope
@Component
public class PacketReaderImpl implements IPacketReader {

	private static final Logger LOGGER = PacketManagerLogger.getLogger(PacketReaderImpl.class);

	@Value("${mosip.commons.packetnames}")
	private String packetNames;

	@Autowired
	private PacketKeeper packetKeeper;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private IdSchemaUtils idSchemaUtils;

	@Autowired
	private PacketValidator packetValidator;

	/**
	 * Perform packet validations and audit errors. List of validations - 1. schema
	 * & idobject reference validation 2. files validation 3. decrypted packet
	 * checksum validation 4. cbeff validation 5. document validation
	 *
	 *
	 * @param id
	 * @param process
	 * @return
	 */
	@Override
	public boolean validatePacket(String id, String source, String process) {
		try {
			return packetValidator.validate(id, source, process);
		} catch (BaseCheckedException | IOException | NoSuchAlgorithmException e) {
			LOGGER.error(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id,
					"Packet Validation exception : " + ExceptionUtils.getStackTrace(e));
			if (e instanceof BaseCheckedException)
				throw new PacketValidationFailureException(((BaseCheckedException) e).getMessage(), e);
			else
				throw new PacketValidationFailureException(((IOException) e).getMessage(), e);
		}
	}

	/**
	 * return data from idobject of all 3 subpackets
	 *
	 * @param id
	 * @param process
	 * @return
	 */
	@Override
	@Cacheable(value = "packets", key = "{'allFields'.concat('-').concat(#id).concat('-').concat(#process)}")
	public Map<String, Object> getAll(String id, String source, String process) {
		LOGGER.info(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id,
				"Getting all fields :: enrtry");
		Map<String, Object> finalMap = new LinkedHashMap<>();
		String[] sourcePacketNames = packetNames.split(",");

		try {
			for (String srcPacket : sourcePacketNames) {
				Packet packet = packetKeeper.getPacket(getPacketInfo(id, srcPacket, source, process));
				InputStream idJsonStream = ZipUtils.unzipAndGetFile(packet.getPacket(), "ID");
				if (idJsonStream != null) {
					byte[] bytearray = IOUtils.toByteArray(idJsonStream);
					String jsonString = new String(bytearray);
					LinkedHashMap<String, Object> currentIdMap = (LinkedHashMap<String, Object>) mapper
							.readValue(jsonString, LinkedHashMap.class).get(IDENTITY);

					currentIdMap.keySet().stream().forEach(key -> {
						Object value = currentIdMap.get(key);
						if (value != null && (value instanceof Number))
							finalMap.putIfAbsent(key, value);
						else if (value != null && (value instanceof String))
							finalMap.putIfAbsent(key, value.toString().replaceAll("^\"|\"$", ""));
						else {
							try {
								finalMap.putIfAbsent(key,
										value != null ? JsonUtils.javaObjectToJsonString(currentIdMap.get(key)) : null);
							} catch (io.mosip.kernel.core.util.exception.JsonProcessingException e) {
								LOGGER.error(ExceptionUtils.getStackTrace(e));
								throw new GetAllIdentityException(e.getMessage());
							}
						}
					});
				}
			}
		} catch (Exception e) {
			LOGGER.error(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id,
					ExceptionUtils.getStackTrace(e));
			if (e instanceof BaseCheckedException) {
				BaseCheckedException ex = (BaseCheckedException) e;
				throw new GetAllIdentityException(ex.getErrorCode(), ex.getMessage());
			} else if (e instanceof BaseUncheckedException) {
				BaseUncheckedException ex = (BaseUncheckedException) e;
				throw new GetAllIdentityException(ex.getErrorCode(), ex.getMessage());
			}
			throw new GetAllIdentityException(e.getMessage());
		}

		return finalMap;
	}

	@Override
	public String getField(String id, String field, String source, String process) {
		LOGGER.info(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id,
				"getField :: for - " + field);
		Map<String, Object> allFields = getAll(id, source, process);
		if (allFields != null) {
			Object fieldObj = allFields.get(field);
			return fieldObj != null ? fieldObj.toString() : null;
		}
		return null;
	}

	@Override
	public Map<String, String> getFields(String id, List<String> fields, String source, String process) {
		LOGGER.info(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id,
				"getFields :: for - " + fields.toString());
		Map<String, String> result = new HashMap<>();
		Map<String, Object> allFields = getAll(id, source, process);
		fields.stream().forEach(
				field -> result.put(field, allFields.get(field) != null ? allFields.get(field).toString() : null));

		return result;
	}

	@Override
	public Document getDocument(String id, String documentName, String source, String process) {
		LOGGER.info(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id,
				"getDocument :: for - " + documentName);
		Map<String, Object> idobjectMap = getAll(id, source, process);
		Double schemaVersion = idobjectMap.get(PacketManagerConstants.IDSCHEMA_VERSION) != null
				? Double.valueOf(idobjectMap.get(PacketManagerConstants.IDSCHEMA_VERSION).toString())
				: null;
		String documentString = (String) idobjectMap.get(documentName);
		try {
			if (documentString != null && schemaVersion != null) {
				JSONObject documentMap = new JSONObject(documentString);
				String packetName = idSchemaUtils.getSource(documentName, schemaVersion);
				Packet packet = packetKeeper.getPacket(getPacketInfo(id, packetName, source, process));
				String value = documentMap.has(VALUE) ? documentMap.get(VALUE).toString() : null;
				InputStream documentStream = ZipUtils.unzipAndGetFile(packet.getPacket(), value);
				if (documentStream != null) {
					Document document = new Document();
					document.setDocument(IOUtils.toByteArray(documentStream));
					document.setValue(value);
					document.setType(documentMap.get(TYPE) != null ? documentMap.get(TYPE).toString() : null);
					document.setFormat(documentMap.get(FORMAT) != null ? documentMap.get(FORMAT).toString() : null);
					return document;
				}
			}
		} catch (IOException | ApiNotAccessibleException | PacketDecryptionFailureException | JSONException
				| PacketKeeperException e) {
			LOGGER.error(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id,
					ExceptionUtils.getStackTrace(e));
			throw new GetDocumentException(e.getMessage());
		}
		return null;
	}

	@Override
	public BiometricRecord getBiometric(String id, String biometricFieldName, List<String> modalities, String source,
			String process) {
		LOGGER.info(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id,
				"getBiometric :: for - " + biometricFieldName);
		BiometricRecord biometricRecord = null;
		String packetName = null;
		String fileName = null;
		try {
			Map<String, Object> idobjectMap = getAll(id, source, process);
			String bioString = (String) idobjectMap.get(biometricFieldName);
			JSONObject biometricMap = null;
			if (bioString != null)
				biometricMap = new JSONObject(bioString);
			if (bioString == null || biometricMap == null || biometricMap.isNull(VALUE)) {
				// biometric file not present in idobject. Search in meta data.
				Map<String, String> metadataMap = getMetaInfo(id, source, process);
				String operationsData = metadataMap.get(META_INFO_OPERATIONS_DATA);
				if (StringUtils.isNotEmpty(operationsData)) {
					JSONArray jsonArray = new JSONArray(operationsData);
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject jsonObject = (JSONObject) jsonArray.get(i);
						if (jsonObject.has(LABEL)
								&& jsonObject.get(LABEL).toString().equalsIgnoreCase(biometricFieldName)) {
							packetName = ID;
							fileName = jsonObject.isNull(VALUE) ? null : jsonObject.get(VALUE).toString();
							break;
						}
					}
				}
			} else {
				Double schemaVersion = idobjectMap.get(PacketManagerConstants.IDSCHEMA_VERSION) != null
						? Double.valueOf(idobjectMap.get(PacketManagerConstants.IDSCHEMA_VERSION).toString())
						: null;
				packetName = idSchemaUtils.getSource(biometricFieldName, schemaVersion);
				fileName = biometricMap.get(VALUE).toString();
			}

			if (packetName == null || fileName == null)
				return null;

			Packet packet = packetKeeper.getPacket(getPacketInfo(id, packetName, source, process));
			InputStream biometrics = ZipUtils.unzipAndGetFile(packet.getPacket(), fileName);
			if (biometrics == null)
				return null;
			BIR bir = CbeffValidator.getBIRFromXML(IOUtils.toByteArray(biometrics));
			biometricRecord = new BiometricRecord();
			if(bir.getOthers() != null) {
				Map<String, String> others = new HashMap<>();
				bir.getOthers().forEach(e -> {
						others.put(e.getKey(), e.getValue());
				});
				biometricRecord.setOthers(others);
			}
			biometricRecord.setSegments(filterByModalities(modalities, bir.getBirs()));
		} catch (Exception e) {
			LOGGER.error(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id,
					ExceptionUtils.getStackTrace(e));
			if (e instanceof BaseCheckedException) {
				BaseCheckedException ex = (BaseCheckedException) e;
				throw new GetBiometricException(ex.getErrorCode(), ex.getMessage());
			} else if (e instanceof BaseUncheckedException) {
				BaseUncheckedException ex = (BaseUncheckedException) e;
				throw new GetBiometricException(ex.getErrorCode(), ex.getMessage());
			}
			throw new GetBiometricException(e.getMessage());
		}

		return biometricRecord;
	}

	@Override
	public Map<String, String> getMetaInfo(String id, String source, String process) {
		Map<String, String> finalMap = new LinkedHashMap<>();
		String[] sourcePacketNames = packetNames.split(",");

		try {
			for (String packetName : sourcePacketNames) {
				Packet packet = packetKeeper.getPacket(getPacketInfo(id, packetName, source, process));
				InputStream idJsonStream = ZipUtils.unzipAndGetFile(packet.getPacket(), "PACKET_META_INFO");
				if (idJsonStream != null) {
					byte[] bytearray = IOUtils.toByteArray(idJsonStream);
					String jsonString = new String(bytearray);
					LinkedHashMap<String, Object> currentIdMap = (LinkedHashMap<String, Object>) mapper
							.readValue(jsonString, LinkedHashMap.class).get(IDENTITY);

					currentIdMap.keySet().stream().forEach(key -> {
						try {
							finalMap.putIfAbsent(key,
									currentIdMap.get(key) != null ? JsonUtils
											.javaObjectToJsonString(currentIdMap.get(key)).replaceAll("^\"|\"$", "")
											: null);
						} catch (io.mosip.kernel.core.util.exception.JsonProcessingException e) {
							throw new GetAllMetaInfoException(e.getMessage());
						}
					});
				}
			}
		} catch (Exception e) {
			if (e instanceof BaseCheckedException) {
				BaseCheckedException ex = (BaseCheckedException) e;
				throw new GetAllMetaInfoException(ex.getErrorCode(), ex.getMessage());
			} else if (e instanceof BaseUncheckedException) {
				BaseUncheckedException ex = (BaseUncheckedException) e;
				throw new GetAllMetaInfoException(ex.getErrorCode(), ex.getMessage());
			}
			throw new GetAllMetaInfoException(e.getMessage());
		}
		return finalMap;
	}

	@Override
	public List<Map<String, String>> getAuditInfo(String id, String source, String process) {
		LOGGER.info(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id, "getAuditInfo :: enrtry");
		List<Map<String, String>> finalMap = new ArrayList<>();
		String[] sourcePacketNames = packetNames.split(",");
		try {
			for (String srcPacket : sourcePacketNames) {
				Packet packet = packetKeeper.getPacket(getPacketInfo(id, srcPacket, source, process));
				InputStream auditJson = ZipUtils.unzipAndGetFile(packet.getPacket(), "audit");
				if (auditJson != null) {
					byte[] bytearray = IOUtils.toByteArray(auditJson);
					String jsonString = new String(bytearray);
					List<Map<String, String>> currentMap = (List<Map<String, String>>) mapper.readValue(jsonString,
							List.class);
					finalMap.addAll(currentMap);
				}
			}
		} catch (Exception e) {
			LOGGER.error(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id,
					ExceptionUtils.getStackTrace(e));
			if (e instanceof BaseCheckedException) {
				BaseCheckedException ex = (BaseCheckedException) e;
				throw new GetAllIdentityException(ex.getErrorCode(), ex.getMessage());
			} else if (e instanceof BaseUncheckedException) {
				BaseUncheckedException ex = (BaseUncheckedException) e;
				throw new GetAllIdentityException(ex.getErrorCode(), ex.getMessage());
			}
			throw new GetAllIdentityException(e.getMessage());
		}
		return finalMap;
	}

	private PacketInfo getPacketInfo(String id, String packetName, String source, String process) {
		PacketInfo packetInfo = new PacketInfo();
		packetInfo.setId(id);
		packetInfo.setPacketName(packetName);
		packetInfo.setProcess(process);
		packetInfo.setSource(source);
		return packetInfo;
	}

	public List<BIR> filterByModalities(List<String> modalities,
			List<BIR> birList) {
		List<BIR> segments = new ArrayList<>();
		if (CollectionUtils.isEmpty(modalities)) {
			birList.forEach(bir -> segments.add(bir));
		} else {
			// first search modalities in subtype and if not present search in type
			for (BIR bir : birList) {
				if (CollectionUtils.isNotEmpty(bir.getBdbInfo().getSubtype())
						&& isModalityPresentInTypeSubtype(bir.getBdbInfo().getSubtype(), modalities)) {
						segments.add(bir);
				} else {
					for (BiometricType type : bir.getBdbInfo().getType()) {
						if (isModalityPresentInTypeSubtype(Lists.newArrayList(type.value()), modalities))
							segments.add(bir);
					}
				}
			}
		}
			return segments;
	}

	private boolean isModalityPresentInTypeSubtype(List<String> typeSubtype, List<String> modalities) {
		boolean isPresent = false;
		for (String modality : modalities) {
			String[] modalityArray = modality.split(" ");
			if (ArrayUtils.isNotEmpty(modalityArray) && ListUtils.isEqualList(typeSubtype, Arrays.asList(modalityArray)))
				isPresent = true;
		}
		return isPresent;
	}

}
