package io.mosip.kernel.packetmanager.impl;

import io.mosip.kernel.core.fsadapter.spi.FileSystemAdapter;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.packetmanager.constants.LoggerFileConstant;
import io.mosip.kernel.packetmanager.exception.ApiNotAccessibleException;
import io.mosip.kernel.packetmanager.exception.FileNotFoundInDestinationException;
import io.mosip.kernel.packetmanager.exception.PacketDecryptionFailureException;
import io.mosip.kernel.packetmanager.logger.PacketUtilityLogger;
import io.mosip.kernel.packetmanager.spi.PacketDecryptor;
import io.mosip.kernel.packetmanager.spi.PacketReaderService;
import io.mosip.kernel.packetmanager.util.ZipUtils;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;


/**
 * The Class PacketReaderServiceImpl.
 *
 * @author Sowmya
 */
public class PacketReaderServiceImpl implements PacketReaderService {

	/** The file system adapter. */
	@Autowired
	private FileSystemAdapter fileSystemAdapter;

	/** The decryptor. */
	@Autowired
	private PacketDecryptor decryptor;

	/** The reg proc logger. */
	private static Logger packetUtilityLogger = PacketUtilityLogger.getLogger(PacketReaderServiceImpl.class);

	/** The Constant PACKET_NOTAVAILABLE_ERROR_DESC. */
	private static final String PACKET_NOTAVAILABLE_ERROR_DESC = "the requested file is not found in the destination";

	private static final String IDENTITY="identity";
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.registration.processor.packet.utility.service.PacketReaderService#
	 * checkFileExistence(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public boolean checkFileExistence(String id, String fileName, String source)
			throws PacketDecryptionFailureException, IOException, ApiNotAccessibleException {
		packetUtilityLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), id,
				"PacketReaderServiceImpl::checkFileExistence()::entry");
		InputStream decryptedData = getFile(id, source);
		packetUtilityLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), id,
				"PacketReaderServiceImpl::checkFileExistence()::extractZip");
		return ZipUtils.unzipAndCheckIsFileExist(decryptedData, fileName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.registration.processor.packet.utility.service.PacketReaderService#
	 * getFile(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public InputStream getFile(String id, String fileName, String source) throws
			PacketDecryptionFailureException, IOException, ApiNotAccessibleException {
		packetUtilityLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), id,
				"PacketReaderServiceImpl::getFile()::entry");
		InputStream decryptedData = getFile(id, source);
		packetUtilityLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), id,
				"PacketReaderServiceImpl::getFile()::extractZip");
		return ZipUtils.unzipAndGetFile(decryptedData, fileName);
	}

	/**
	 * Gets the file.
	 *
	 * @param id     the id
	 * @param source the source
	 * @return the file
	 * @throws PacketDecryptionFailureException the packet decryption failure
	 *                                          exception
	 * @throws IOException                      Signals that an I/O exception has
	 *                                          occurred.
	 */
	private InputStream getFile(String id, String source)
			throws PacketDecryptionFailureException, IOException, ApiNotAccessibleException {
		packetUtilityLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), id,
				"PacketReaderServiceImpl::fileSystemAdapter.getPacket()");
		InputStream data = fileSystemAdapter.getPacket(id);
		if (data == null) {
			throw new FileNotFoundInDestinationException(
					PACKET_NOTAVAILABLE_ERROR_DESC);
		}
		packetUtilityLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), id,
				"PacketReaderServiceImpl::getFile()::extractSubfolderZip");

		InputStream sourceFolderInputStream = ZipUtils.unzipAndGetFile(data, id + "_" + source);

		if (sourceFolderInputStream == null) {
			throw new FileNotFoundInDestinationException(
					PACKET_NOTAVAILABLE_ERROR_DESC);
		}

		packetUtilityLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), id,
				"PacketReaderServiceImpl::getFile(regid)::decryptor");
		InputStream decryptedData = decryptor.decrypt(sourceFolderInputStream, id);
		if (decryptedData == null) {
			throw new PacketDecryptionFailureException();
		}
		return decryptedData;
	}

	@Override
	public InputStream getEncryptedSourcePacket(String rid, InputStream inputStream, String source) throws IOException {
		return ZipUtils.unzipAndGetFile(inputStream, rid + "_" + source);
	}

	@SuppressWarnings({ "rawtypes", "unchecked"})
	@Override
	public Object getCompleteIdObject(String rid,String sourcepackets) throws PacketDecryptionFailureException, ApiNotAccessibleException, IOException {
		String[] sources = sourcepackets.split(",");

		JSONObject finalIdObject = null;

		for (String source : sources) {
			InputStream idJsonStream = ZipUtils.unzipAndGetFile(getFile(rid, source), "ID");
			if(idJsonStream != null) {
			byte[] bytearray = IOUtils.toByteArray(idJsonStream);
			String jsonString = new String(bytearray);
			ObjectMapper mapper = new ObjectMapper();
			JSONObject idObject = mapper.readValue(jsonString, JSONObject.class);
			
			if(finalIdObject == null) {
				finalIdObject=idObject;
			}else if(finalIdObject != null){
				
				LinkedHashMap currentidentityMap = (LinkedHashMap) idObject.get(IDENTITY);
				LinkedHashMap finalidMap =(LinkedHashMap) finalIdObject.get(IDENTITY);
				LinkedHashMap finalidentityMap=new LinkedHashMap<>(finalidMap);
				currentidentityMap.forEach((key, value) -> 
					finalidentityMap.merge(key, value, (v1, v2) -> v1 !=null? v1 : v2));
				LinkedHashMap objectMap=new LinkedHashMap<>();
				objectMap.put(IDENTITY, finalidentityMap);
				finalIdObject=new JSONObject(objectMap);
			}
			}
		}

		return finalIdObject;
	}
}
