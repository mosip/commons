package io.mosip.kernel.packetmanager.util;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.packetmanager.constants.LoggerFileConstant;
import io.mosip.kernel.packetmanager.logger.PacketUtilityLogger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Class to unzip the packets
 * 
 * @author Sowmya
 * @since 1.0.0
 */
public class ZipUtils {

	/** The reg proc logger. */
	private static Logger packetUtilityLogger = PacketUtilityLogger.getLogger(ZipUtils.class);

	private ZipUtils() {
		// DONOT DELETE
	}

	/**
	 * Method to unzip the file in-memeory and search the required file and return
	 * it
	 * 
	 * @param packetStream
	 *            zip file to be unzipped
	 * @param file
	 *            file to search within zip file
	 * @return return the corresponding file as inputStream
	 * @throws IOException
	 *             if any error occored while unzipping the file
	 */
	public static InputStream unzipAndGetFile(InputStream packetStream, String file) throws IOException {
		packetUtilityLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), "",
				"ZipUtils::unzipAndGetFile()::entry");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		boolean flag = false;
		byte[] buffer = new byte[2048];
		try (ZipInputStream zis = new ZipInputStream(packetStream)) {
			ZipEntry ze = zis.getNextEntry();
			while (ze != null) {
				String fileName = ze.getName();
				String fileNameWithOutExt = FilenameUtils.removeExtension(fileName);
				if (FilenameUtils.equals(fileNameWithOutExt, file, true, IOCase.INSENSITIVE)) {
					int len;
					flag = true;
					while ((len = zis.read(buffer)) > 0) {
						out.write(buffer, 0, len);
					}
					break;
				}
				zis.closeEntry();
				ze = zis.getNextEntry();
			}
			zis.closeEntry();
		} finally {
			packetStream.close();
			out.close();
		}
		if (flag) {
			packetUtilityLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					"", "ZipUtils::unzipAndGetFile()::exit");

			return new ByteArrayInputStream(out.toByteArray());
		}

		return null;
	}

	/**
	 * Method to unzip the file in-memeory and search the required file exists
	 * 
	 * @param packetStream
	 *            zip file to be unzipped
	 * @param file
	 *            file to search within zip file
	 * @return return true if found the required file, false otherwise
	 * @throws IOException
	 *             if any error occored while unzipping the file
	 */
	public static boolean unzipAndCheckIsFileExist(InputStream packetStream, String file) throws IOException {
		boolean isExist = false;
		packetUtilityLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), "",
				"ZipUtils::unzipAndCheckIsFileExist()::entry");

		try (ZipInputStream zis = new ZipInputStream(packetStream)) {
			ZipEntry ze = zis.getNextEntry();
			while (ze != null) {
				String fileName = ze.getName();
				String fileNameWithOutExt = FilenameUtils.removeExtension(fileName);
				if (FilenameUtils.equals(fileNameWithOutExt, file, true, IOCase.INSENSITIVE)) {
					isExist = true;
					break;
				}
				zis.closeEntry();
				ze = zis.getNextEntry();
			}
			zis.closeEntry();
		} finally {
			packetStream.close();
		}
		packetUtilityLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), "",
				"ZipUtils::unzipAndCheckIsFileExist()::exit");

		return isExist;
	}

	/**
	 * Method to unzip the file for passed destination path
	 * 
	 * @param input
	 *            zip file to be unzipped
	 * @param desDir
	 *            location where to unzip the files
	 * @throws IOException
	 *             if any error occurred while unzipping
	 */
	public static void unZipFromInputStream(InputStream input, String desDir) throws IOException {
		byte[] buffer = new byte[1024];
		packetUtilityLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), "",
				"ZipUtils::unZipFromInputStream()::entry");

		try (ZipInputStream zis = new ZipInputStream(input)) {
			File folder = FileUtils.getFile(desDir);
			if (!folder.exists()) {
				if (folder.mkdir()) {
					packetUtilityLogger.debug(LoggerFileConstant.SESSIONID.toString(),
							LoggerFileConstant.REGISTRATIONID.toString(), "", folder + "created");

				}
			}
			ZipEntry ze = zis.getNextEntry();
			while (ze != null) {
				if (ze.isDirectory()) {
					File file = FileUtils.getFile(desDir + ze.getName());
					if (file.mkdir()) {
						packetUtilityLogger.debug(LoggerFileConstant.SESSIONID.toString(),
								LoggerFileConstant.REGISTRATIONID.toString(), "", file + "created");

					}
				} else {
					String fileName = ze.getName();
					File newFile = FileUtils.getFile(desDir + File.separator + fileName);
					if (FileUtils.getFile(newFile.getParent()).mkdirs()) {
						FileOutputStream fos = new FileOutputStream(newFile);
						int len;
						while ((len = zis.read(buffer)) > 0) {
							fos.write(buffer, 0, len);
						}
						fos.close();
					}
				}
				ze = zis.getNextEntry();
			}
			zis.closeEntry();
		} finally {
			input.close();
		}
		packetUtilityLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), "",
				"ZipUtils::unZipFromInputStream()::exit");

	}
}
