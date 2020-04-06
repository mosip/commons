package io.mosip.kernel.fsadapter.proxy.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.fsadapter.exception.FSAdapterException;
import io.mosip.kernel.core.fsadapter.spi.FileSystemAdapter;
import io.mosip.kernel.fsadapter.proxy.constant.ProxyAdapterErrorCode;

/**
 * Proxy Implementation for developers to use instead of setting any server for
 * file storage
 * 
 * @author Urvil Joshi
 * @since 1.0.8
 *
 */
@Component
public class ProxyAdapterImpl implements FileSystemAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProxyAdapterImpl.class.getName());

	@Value("${kernel.proxy.fsa.basepath}")
	String basePath;
	
	@PostConstruct
	void init() {
		File baseDir=  new File(basePath);
		boolean dirCreated=baseDir.mkdir();
		if(dirCreated) {
			LOGGER.info("base directory created");
		}else {
			LOGGER.info("base directory not created Please create");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.fsadapter.hdfs.spi.FileSystemAdapter#checkFileExistence(java.
	 * lang.String, java.lang.String)
	 */
	@Override
	public boolean checkFileExistence(String id, String filePath) {
		LOGGER.info("Checking if file exist in packet {} with path {}", id, getFilePath(filePath));
		File file = new File(getPath(id, filePath));
		return file.exists();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.fsadapter.hdfs.spi.FileSystemAdapter#copyFile(java.lang.
	 * String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public boolean copyFile(String sourcePacket, String sourceFilePath, String destinationPacket,
			String destinationFilePath) {
		LOGGER.info("Copying file from packet {} with path {} to packet {} with path {}", sourcePacket, sourceFilePath,
				destinationPacket, destinationFilePath);
		return storeFile(destinationPacket, destinationFilePath, getFile(sourcePacket, sourceFilePath));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.fsadapter.hdfs.spi.FileSystemAdapter#deleteFile(java.lang.
	 * String, java.lang.String)
	 */
	@Override
	public boolean deleteFile(String id, String filePath) {
		LOGGER.info("Deleting file in packet {} with path {}", id, getFilePath(filePath));
		try {
			File file = new File(getPath(id, filePath));
			if (file.isDirectory()) {
				FileUtils.deleteDirectory(file);
				return !file.exists();
			} else {
				return Files.deleteIfExists(file.toPath());
			}
		} catch (IOException e) {
			throw new FSAdapterException(ProxyAdapterErrorCode.HDFS_ADAPTER_EXCEPTION.getErrorCode(),
					ProxyAdapterErrorCode.HDFS_ADAPTER_EXCEPTION.getErrorMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.fsadapter.hdfs.spi.FileSystemAdapter#deletePacket(java.lang.
	 * String)
	 */
	@Override
	public boolean deletePacket(String id) {
		LOGGER.info("Deleting packet {}", id);
		//
		try {
			File file = new File(getPath(id, id));
			FileUtils.deleteDirectory(file);
			return !file.exists();
		} catch (IOException e) {
			throw new FSAdapterException(ProxyAdapterErrorCode.HDFS_ADAPTER_EXCEPTION.getErrorCode(),
					ProxyAdapterErrorCode.HDFS_ADAPTER_EXCEPTION.getErrorMessage(), e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.fsadapter.hdfs.spi.FileSystemAdapter#getFile(java.lang.
	 * String, java.lang.String)
	 */
	@Override
	public InputStream getFile(String id, String filePath) {
		LOGGER.info("Getting file from packet {} with path {}", id, getFilePath(filePath));
		try {
			File file = new File(getPath(id, filePath));
			if (!checkFileExistence(id, filePath)) {
				throw new FSAdapterException(ProxyAdapterErrorCode.FILE_NOT_FOUND_EXCEPTION.getErrorCode(),
						ProxyAdapterErrorCode.FILE_NOT_FOUND_EXCEPTION.getErrorMessage());
			}
			return FileUtils.openInputStream(file);
		} catch (IOException e) {
			throw new FSAdapterException(ProxyAdapterErrorCode.HDFS_ADAPTER_EXCEPTION.getErrorCode(),
					ProxyAdapterErrorCode.HDFS_ADAPTER_EXCEPTION.getErrorMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.fsadapter.hdfs.spi.FileSystemAdapter#getPacket(java.lang.
	 * String)
	 */
	@Override
	public InputStream getPacket(String id) {
		LOGGER.info("Getting packet {} ", id);
		try {
			File file = new File(getPath(id, id));
			if (!checkFileExistence(id, "")) {
				LOGGER.error("Packet {} doesnot exist in filesystem {} ", id,
						ProxyAdapterErrorCode.FILE_NOT_FOUND_EXCEPTION.getErrorMessage());
				throw new FSAdapterException(ProxyAdapterErrorCode.FILE_NOT_FOUND_EXCEPTION.getErrorCode(),
						ProxyAdapterErrorCode.FILE_NOT_FOUND_EXCEPTION.getErrorMessage());
			}
			return FileUtils.openInputStream(file);
		} catch (IOException e) {
			LOGGER.error("Packet {} io exceptio occuered {}", id, ExceptionUtils.getStackTrace(e));
			throw new FSAdapterException(ProxyAdapterErrorCode.HDFS_ADAPTER_EXCEPTION.getErrorCode(),
					ProxyAdapterErrorCode.HDFS_ADAPTER_EXCEPTION.getErrorMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.fsadapter.hdfs.spi.FileSystemAdapter#isPacketPresent(java.
	 * lang.String)
	 */
	@Override
	public boolean isPacketPresent(String id) {
		LOGGER.info("Checking if packet {} exists", id);
		try {
			return checkFileExistence(id, id);
		} catch (Exception e) {
			throw new FSAdapterException(ProxyAdapterErrorCode.HDFS_ADAPTER_EXCEPTION.getErrorCode(),
					ProxyAdapterErrorCode.HDFS_ADAPTER_EXCEPTION.getErrorMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.fsadapter.hdfs.spi.FileSystemAdapter#storeFile(java.lang.
	 * String, java.lang.String, java.io.InputStream)
	 */
	@Override
	public boolean storeFile(String id, String filePath, InputStream content) {
		LOGGER.info("Storing file in packet {} with path {}", id, getFilePath(filePath));
		try (InputStream fileContent = content) {
			File file = new File(getPath(id, filePath));
			FileUtils.copyInputStreamToFile(fileContent, file);
		} catch (IOException e) {
			throw new FSAdapterException(ProxyAdapterErrorCode.HDFS_ADAPTER_EXCEPTION.getErrorCode(),
					ProxyAdapterErrorCode.HDFS_ADAPTER_EXCEPTION.getErrorMessage(), e);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.fsadapter.hdfs.spi.FileSystemAdapter#storePacket(java.lang.
	 * String, java.io.File)
	 */
	@Override
	public boolean storePacket(String id, File file) {
		LOGGER.info("Storing packet {}", id);
		try {
			File packet = new File(getPath(id, id));
			FileUtils.writeByteArrayToFile(packet, FileUtils.readFileToByteArray(file));
		} catch (IOException e) {
			throw new FSAdapterException(ProxyAdapterErrorCode.HDFS_ADAPTER_EXCEPTION.getErrorCode(),
					ProxyAdapterErrorCode.HDFS_ADAPTER_EXCEPTION.getErrorMessage(), e);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.fsadapter.hdfs.spi.FileSystemAdapter#storePacket(java.lang.
	 * String, java.io.InputStream)
	 */
	@Override
	public boolean storePacket(String id, InputStream content) {
		try {
			File packet = new File(getPath(id, id));
			FileUtils.copyInputStreamToFile(content, packet);
		} catch (IOException e) {
			throw new FSAdapterException(ProxyAdapterErrorCode.HDFS_ADAPTER_EXCEPTION.getErrorCode(),
					ProxyAdapterErrorCode.HDFS_ADAPTER_EXCEPTION.getErrorMessage(), e);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.fsadapter.hdfs.spi.FileSystemAdapter#unpackPacket(java.lang.
	 * String)
	 */
	@Override
	public void unpackPacket(String id) {
		LOGGER.info("Unpacking packet {}", id);
		InputStream packetStream = getPacket(id);
		ZipInputStream zis = new ZipInputStream(packetStream);
		byte[] buffer = new byte[2048];
		byte[] file;
		try {
			ZipEntry ze = zis.getNextEntry();
			while (ze != null) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				int len;
				while ((len = zis.read(buffer)) > 0) {
					out.write(buffer, 0, len);
				}
				file = out.toByteArray();
				InputStream inputStream = new ByteArrayInputStream(file);
				String filePath = FilenameUtils.getPathNoEndSeparator(ze.getName());
				String fileName = FilenameUtils.getBaseName(ze.getName());
				if (!fileName.isEmpty()) {
					storeFile(id, FilenameUtils.concat(filePath, fileName), inputStream);
				}
				inputStream.close();
				ze = zis.getNextEntry();
			}
			zis.closeEntry();
			zis.close();
		} catch (IOException e) {
			throw new FSAdapterException(ProxyAdapterErrorCode.HDFS_ADAPTER_EXCEPTION.getErrorCode(),
					ProxyAdapterErrorCode.HDFS_ADAPTER_EXCEPTION.getErrorMessage(), e);
		}
	}

	/**
	 * Construct a hadoop path from a String
	 * 
	 * @param id       the packetId
	 * @param filePath the filePath
	 * @return the path
	 */
	public String getPath(String id, String filePath) {
		return FilenameUtils.concat(FilenameUtils.concat(basePath, getFilePath(id)), getFilePath(filePath));
	}

	/**
	 * Get formatted filePath
	 * 
	 * @param filePath filePath
	 * @return formatted filePath
	 */
	public String getFilePath(String filePath) {
		return filePath.toUpperCase();
	}
}