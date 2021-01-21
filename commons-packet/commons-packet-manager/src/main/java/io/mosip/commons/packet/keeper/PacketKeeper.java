package io.mosip.commons.packet.keeper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.commons.khazana.dto.ObjectDto;
import io.mosip.commons.khazana.spi.ObjectStoreAdapter;
import io.mosip.commons.packet.constants.ErrorCode;
import io.mosip.commons.packet.constants.PacketUtilityErrorCodes;
import io.mosip.commons.packet.dto.Packet;
import io.mosip.commons.packet.dto.PacketInfo;
import io.mosip.commons.packet.dto.TagDto;
import io.mosip.commons.packet.exception.CryptoException;
import io.mosip.commons.packet.exception.GetTagException;
import io.mosip.commons.packet.exception.ObjectStoreAdapterException;
import io.mosip.commons.packet.exception.PacketIntegrityFailureException;
import io.mosip.commons.packet.exception.PacketKeeperException;
import io.mosip.commons.packet.exception.TagCreationException;
import io.mosip.commons.packet.spi.IPacketCryptoService;
import io.mosip.commons.packet.util.PacketManagerHelper;
import io.mosip.commons.packet.util.PacketManagerLogger;
import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.HMACUtils2;

/**
 * The packet keeper is used to store & retrieve packet, creation of audit, encrypt and sign packet.
 * Packet keeper is used to get container information and list of sources from a packet.
 */
@Component
public class PacketKeeper {

    /**
     * The reg proc logger.
     */
    private static Logger LOGGER = PacketManagerLogger.getLogger(PacketKeeper.class);

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

    @Value("${objectstore.crypto.name}")
    private String cryptoName;

    @Autowired
    @Qualifier("OnlinePacketCryptoServiceImpl")
    private IPacketCryptoService onlineCrypto;

    @Autowired
    @Qualifier("OfflinePacketCryptoServiceImpl")
    private IPacketCryptoService offlineCrypto;

    private static final String UNDERSCORE = "_";

    /**
     * Get the manifest information for given packet id
     *
     * @param id : packet id
     * @return : Manifest
     */
    /*public Manifest getManifest(String id) {
        Manifest manifest = new Manifest();

        Map<String, Object> metaMap = getAdapter().getMetaData(PACKET_MANAGER_ACCOUNT, id, null);

        metaMap.entrySet().forEach(entry -> {
            Map<String, Object> tempMap = (Map<String, Object>) entry.getValue();
            PacketInfo packetInfo = PacketManagerHelper.getPacketInfo(tempMap);
            manifest.getPacketInfos().add(packetInfo);
        });
        return manifest;
    }*/

    /**
     * Check packet integrity.
     *
     * @param packetInfo : the packet information
     * @return : boolean
     */
    public boolean checkIntegrity(PacketInfo packetInfo, byte[] encryptedSubPacket) throws NoSuchAlgorithmException {
        String hash = CryptoUtil.encodeBase64(HMACUtils2.generateHash(encryptedSubPacket));
        boolean result = hash.equals(packetInfo.getEncryptedHash());
        LOGGER.info(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID,
                getName(packetInfo.getId(), packetInfo.getPacketName()), "Integrity check : " + result);
        return result;
    }

    /**
     * Check integrity and signature of the packet
     *
     *
     * @param packet
     * @param encryptedSubPacket
     * @return
     */
    public boolean checkSignature(Packet packet, byte[] encryptedSubPacket) throws NoSuchAlgorithmException {
        // TODO : disabling signature verification temporarily
        boolean result = true;//getCryptoService().verify(packet.getPacket(), CryptoUtil.decodeBase64(packet.getPacketInfo().getSignature()));
        if (result)
            result = checkIntegrity(packet.getPacketInfo(), encryptedSubPacket);
        LOGGER.info(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID,
                getName(packet.getPacketInfo().getId(), packet.getPacketInfo().getPacketName()), "Integrity and signature check : " + result);
        return result;
    }

    /**
     * Get packet
     *
     * @param packetInfo : packet info
     * @return : Packet
     */
    public Packet getPacket(PacketInfo packetInfo) throws PacketKeeperException {
        try {
            InputStream is = getAdapter().getObject(PACKET_MANAGER_ACCOUNT, packetInfo.getId(), packetInfo.getSource(),
                    packetInfo.getProcess(), getName(packetInfo.getId(), packetInfo.getPacketName()));
            if (is == null) {
                LOGGER.error(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID,
                        getName(packetInfo.getId(), packetInfo.getPacketName()), packetInfo.getProcess() + " Packet is not present in packet store.");
                throw new PacketKeeperException(ErrorCode.PACKET_NOT_FOUND.getErrorCode(), ErrorCode.PACKET_NOT_FOUND.getErrorMessage());
            }
            byte[] encryptedSubPacket = IOUtils.toByteArray(is);
            byte[] subPacket = getCryptoService().decrypt(packetInfo.getId(), encryptedSubPacket);

            Packet packet = new Packet();
            packet.setPacket(subPacket);
            Map<String, Object> metaInfo = getAdapter().getMetaData(PACKET_MANAGER_ACCOUNT, packetInfo.getId(),
                    packetInfo.getSource(), packetInfo.getProcess(), getName(packetInfo.getId(), packetInfo.getPacketName()));
            if (metaInfo != null && !metaInfo.isEmpty())
                packet.setPacketInfo(PacketManagerHelper.getPacketInfo(metaInfo));
            else {
                LOGGER.info(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID,
                        getName(packetInfo.getId(), packetInfo.getPacketName()), "metainfo not found for this packet");
                packet.setPacketInfo(packetInfo);
            }


            if (!checkSignature(packet, encryptedSubPacket)) {
                LOGGER.error(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID,
                        getName(packet.getPacketInfo().getId(), packetInfo.getPacketName()), "Packet Integrity and Signature check failed");
                throw new PacketIntegrityFailureException();
            }

            return packet;
        } catch (Exception e) {
            LOGGER.error(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, packetInfo.getId(), ExceptionUtils.getStackTrace(e));
            if (e instanceof BaseCheckedException) {
                BaseCheckedException ex = (BaseCheckedException) e;
                throw new PacketKeeperException(ex.getErrorCode(), ex.getMessage());
            }
            else if (e instanceof BaseUncheckedException) {
                BaseUncheckedException ex = (BaseUncheckedException) e;
                throw new PacketKeeperException(ex.getErrorCode(), ex.getMessage());
            } else
                throw new PacketKeeperException(PacketUtilityErrorCodes.PACKET_KEEPER_GET_ERROR.getErrorCode(),
                    "Exception occured reading packet : " + e.getMessage(), e);
        }
    }

    /**
     * Put packet into storage/cache
     *
     * @param packet : the Packet
     * @return PacketInfo
     */
    public PacketInfo putPacket(Packet packet) throws PacketKeeperException {
        try {
            // encrypt packet
            byte[] encryptedSubPacket = getCryptoService().encrypt(packet.getPacketInfo().getId(), packet.getPacket());

            // put packet in object store
            boolean response = getAdapter().putObject(PACKET_MANAGER_ACCOUNT,
                    packet.getPacketInfo().getId(), packet.getPacketInfo().getSource(),
                    packet.getPacketInfo().getProcess(), packet.getPacketInfo().getPacketName(), new ByteArrayInputStream(encryptedSubPacket));

            if (response) {
                PacketInfo packetInfo = packet.getPacketInfo();
                // sign encrypted packet
                packetInfo.setSignature(CryptoUtil.encodeBase64(getCryptoService().sign(packet.getPacket())));
                // generate encrypted packet hash
                packetInfo.setEncryptedHash(CryptoUtil.encodeBase64(HMACUtils2.generateHash(encryptedSubPacket)));
                Map<String, Object> metaMap = PacketManagerHelper.getMetaMap(packetInfo);
                metaMap = getAdapter().addObjectMetaData(PACKET_MANAGER_ACCOUNT,
                        packet.getPacketInfo().getId(), packet.getPacketInfo().getSource(), packet.getPacketInfo().getProcess(), packet.getPacketInfo().getPacketName(), metaMap);
                return PacketManagerHelper.getPacketInfo(metaMap);
            } else
                throw new PacketKeeperException(PacketUtilityErrorCodes
                        .PACKET_KEEPER_PUT_ERROR.getErrorCode(), "Unable to store packet in object store");


        } catch (Exception e) {
            LOGGER.error(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, packet.getPacketInfo().getId(), ExceptionUtils.getStackTrace(e));
            if (e instanceof BaseCheckedException) {
                BaseCheckedException ex = (BaseCheckedException) e;
                throw new PacketKeeperException(ex.getErrorCode(), ex.getMessage());
            } else if (e instanceof BaseUncheckedException) {
                BaseUncheckedException ex = (BaseUncheckedException) e;
                throw new PacketKeeperException(ex.getErrorCode(), ex.getMessage());
            }
            throw new PacketKeeperException(PacketUtilityErrorCodes.PACKET_KEEPER_PUT_ERROR.getErrorCode(),
                    "Failed to persist packet in object store : " + e.getMessage(), e);
        }
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

    private IPacketCryptoService getCryptoService() {
        if (cryptoName.equalsIgnoreCase(onlineCrypto.getClass().getSimpleName()))
            return onlineCrypto;
        else if (cryptoName.equalsIgnoreCase(offlineCrypto.getClass().getSimpleName()))
            return offlineCrypto;
        else
            throw new CryptoException();
    }

    private static String getName(String id, String name) {
        return id + UNDERSCORE + name;
    }

    public boolean deletePacket(String id, String source, String process) {
        return getAdapter().removeContainer(PACKET_MANAGER_ACCOUNT, id, source, process);
    }

    public boolean pack(String id, String source, String process) {
        return getAdapter().pack(PACKET_MANAGER_ACCOUNT, id, source, process);
    }

	public Map<String, String> addTags(TagDto tagDto) {
		try {
			Map<String, String> newTags = new HashMap<String, String>();
			Map<String, String> existingTags = getAdapter().getTags(PACKET_MANAGER_ACCOUNT, tagDto.getId());
			if (existingTags.isEmpty()) {
				newTags.putAll(tagDto.getTags());
			} else {
				for (Entry<String, String> entry : tagDto.getTags().entrySet()) {
					if (existingTags.containsKey(entry.getKey())) {

						throw new TagCreationException(PacketUtilityErrorCodes.TAG_ALREADY_EXIST.getErrorCode(),
								PacketUtilityErrorCodes.TAG_ALREADY_EXIST.getErrorMessage());

					} else {
						newTags.put(entry.getKey(), entry.getValue());
					}
				}
			}
			Map<String, String> tags = getAdapter().addTags(PACKET_MANAGER_ACCOUNT, tagDto.getId(), newTags);
			return tags;
		} catch (Exception e) {
				LOGGER.error(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, tagDto.getId(),
						ExceptionUtils.getStackTrace(e));
				if (e instanceof BaseCheckedException) {
					BaseCheckedException ex = (BaseCheckedException) e;
					throw new TagCreationException(ex.getErrorCode(), ex.getMessage());
				} else if (e instanceof BaseUncheckedException) {
					BaseUncheckedException ex = (BaseUncheckedException) e;
					throw new TagCreationException(ex.getErrorCode(), ex.getMessage());
				}
				throw new TagCreationException(e.getMessage());

			}
	}

	public Map<String, String> updateTags(TagDto tagDto) {
		try {

			Map<String, String> tags = getAdapter().addTags(PACKET_MANAGER_ACCOUNT, tagDto.getId(), tagDto.getTags());
			return tags;

		} catch (Exception e) {
			LOGGER.error(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, tagDto.getId(),
					ExceptionUtils.getStackTrace(e));
			if (e instanceof BaseCheckedException) {
				BaseCheckedException ex = (BaseCheckedException) e;
				throw new TagCreationException(ex.getErrorCode(), ex.getMessage());
			} else if (e instanceof BaseUncheckedException) {
				BaseUncheckedException ex = (BaseUncheckedException) e;
				throw new TagCreationException(ex.getErrorCode(), ex.getMessage());
			}
			throw new TagCreationException(e.getMessage());

		}
	}

	public Map<String, String> getTags(String id, List<String> tagNames) {
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
				return tags;
			} else {
				return existingTags;
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
	}

    public List<ObjectDto> getAll(String id) {
        List<ObjectDto> allObjects = getAdapter().getAllObjects(PACKET_MANAGER_ACCOUNT, id);
        return allObjects;
    }
}