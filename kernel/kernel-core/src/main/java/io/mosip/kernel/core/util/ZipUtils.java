package io.mosip.kernel.core.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;

import io.mosip.kernel.core.exception.DataFormatException;
import io.mosip.kernel.core.exception.FileNotFoundException;
import io.mosip.kernel.core.exception.IOException;
import io.mosip.kernel.core.util.constant.ZipUtilConstants;

/**
 * Deflate/inflate byte arrays and extract zip archives with zip-slip guards.
 * <p>
 * Contract: static helpers only; this class is not instantiable. Does not
 * handle RAR or 7z. {@link #unZipDirectory(String, String)} is deprecated;
 * prefer the overload with entry/size/ratio thresholds. Does not perform MOSIP
 * HTTP.
 * </p>
 *
 * @author Megha Tanga
 * @since 1.0.0
 */
public class ZipUtils {

	/**
	 * Private Constructor for ZipUtil Class
	 */
	private ZipUtils() {

	}

	/**
	 * Deflates {@code input} with {@link Deflater#DEFLATED}.
	 *
	 * @param input never-null bytes to compress
	 * @return never-null compressed bytes
	 * @throws IOException when the deflate stream cannot be written
	 */
	public static byte[] zipByteArray(byte[] input) throws IOException {
		byte[] byReturn = null;
		Deflater oDeflate = new Deflater(Deflater.DEFLATED, false);
		oDeflate.setInput(input);
		oDeflate.finish();
		try (ByteArrayOutputStream oZipStream = new ByteArrayOutputStream()) {

			while (!oDeflate.finished()) {
				byte[] byRead = new byte[1024];
				int iBytesRead = oDeflate.deflate(byRead);
				if (iBytesRead == byRead.length) {
					oZipStream.write(byRead);
				} else {
					oZipStream.write(byRead, 0, iBytesRead);
				}
			}
			oDeflate.end();
			byReturn = oZipStream.toByteArray();
		} catch (java.io.IOException e) {
			throw new IOException(ZipUtilConstants.IO_ERROR_CODE.getErrorCode(),
					ZipUtilConstants.IO_ERROR_CODE.getMessage(), e.getCause());
		}

		return byReturn;
	}

	/**
	 * Inflates a previously {@link #zipByteArray(byte[])} payload.
	 *
	 * @param input never-null deflated bytes
	 * @return never-null original bytes
	 * @throws IOException         when inflate I/O fails
	 * @throws DataFormatException when {@code input} is not valid deflate data
	 */
	public static byte[] unzipByteArray(byte[] input) throws IOException {
		byte[] byReturn = null;

		Inflater oInflate = new Inflater(false);
		oInflate.setInput(input);

		try (ByteArrayOutputStream oZipStream = new ByteArrayOutputStream()) {
			while (!oInflate.finished()) {
				byte[] byRead = new byte[1024];
				int iBytesRead = oInflate.inflate(byRead);
				if (iBytesRead == byRead.length) {
					oZipStream.write(byRead);
				} else {
					oZipStream.write(byRead, 0, iBytesRead);
				}
			}
			byReturn = oZipStream.toByteArray();
		} catch (java.util.zip.DataFormatException e) {
			throw new DataFormatException(ZipUtilConstants.DATA_FORMATE_ERROR_CODE.getErrorCode(),
					ZipUtilConstants.DATA_FORMATE_ERROR_CODE.getMessage(), e.getCause());
		} catch (java.io.IOException e) {
			throw new IOException(ZipUtilConstants.IO_ERROR_CODE.getErrorCode(),
					ZipUtilConstants.IO_ERROR_CODE.getMessage(), e.getCause());
		}

		return byReturn;
	}

	/**
	 * Method used for zipping a single file
	 * 
	 * @param inputFile  pass single input file address as string, want to Zip it
	 *                   example : String inputFile = "D:\\Testfiles\\test.txt";
	 * @param outputFile pass Zip file address as string example: String
	 *                   outputZipFile = "D:\\Testfiles\\compressed.zip"
	 * 
	 * @return true if zip file is created
	 * 
	 * @throws FileNotFoundException when file is not found
	 * @throws IOException           when file unable to read
	 * 
	 */

	/*
	 * public static boolean zipFile(String inputFile, String outputFile) throws
	 * IOException {
	 * 
	 * try (ZipOutputStream zipOut = new ZipOutputStream(new
	 * FileOutputStream(outputFile)); FileInputStream fis = new FileInputStream(new
	 * File(inputFile))) {
	 * 
	 * ZipEntry zipEntry = new ZipEntry(new File(inputFile).getName());
	 * zipOut.putNextEntry(zipEntry); readFile(zipOut, fis);
	 * 
	 * } catch (java.io.FileNotFoundException e) { throw new
	 * FileNotFoundException(ZipUtilConstants.FILE_NOT_FOUND_ERROR_CODE.getErrorCode
	 * (), ZipUtilConstants.FILE_NOT_FOUND_ERROR_CODE.getMessage(), e.getCause()); }
	 * catch (java.io.IOException e) { throw new
	 * IOException(ZipUtilConstants.IO_ERROR_CODE.getErrorCode(),
	 * ZipUtilConstants.IO_ERROR_CODE.getMessage(), e.getCause()); }
	 * 
	 * return true; }
	 */

	/**
	 * This is inner method to read a file
	 * 
	 * @param zipOut ZipOutStream object of inputFile
	 * @param fis    FileInputStream object of inputFile
	 * @throws java.io.IOException when file unable to read
	 */
	private static void readFile(ZipOutputStream zipOut, FileInputStream fis) throws java.io.IOException {
		final byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zipOut.write(bytes, 0, length);
		}
	}

	/**
	 * Method used for zipping a multiple file
	 * 
	 * @param inputMultFile pass list of file names as array of String example :
	 *                      String[] inputMultFile = {"D:\\Testfiles\\test.txt",
	 *                      "D:\\Testfiles\\test.txt"}
	 * 
	 * @param outputFile    pass Zip file address as string example: String
	 *                      outputMulFile="D:\\Testfiles\\compressedMult.zip"
	 * 
	 * @return true if zip file is created
	 * 
	 * @throws FileNotFoundException when file is not found
	 * 
	 * @throws IOException           when file unable to read
	 */

	/*
	 * public static boolean zipMultipleFile(String[] inputMultFile, String
	 * outputFile) throws IOException {
	 * 
	 * List<String> srcFiles = new ArrayList<>(Arrays.asList(inputMultFile)); for
	 * (String srcFile : srcFiles) { File fileToZip = new File(srcFile);
	 * 
	 * try (ZipOutputStream zipOut = new ZipOutputStream(new
	 * FileOutputStream(outputFile)); FileInputStream fis = new
	 * FileInputStream(fileToZip)) {
	 * 
	 * ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
	 * zipOut.putNextEntry(zipEntry); readFile(zipOut, fis); } catch
	 * (java.io.FileNotFoundException e) { throw new
	 * FileNotFoundException(ZipUtilConstants.FILE_NOT_FOUND_ERROR_CODE.getErrorCode
	 * (), ZipUtilConstants.FILE_NOT_FOUND_ERROR_CODE.getMessage(), e.getCause()); }
	 * catch (java.io.IOException e) { throw new
	 * IOException(ZipUtilConstants.IO_ERROR_CODE.getErrorCode(),
	 * ZipUtilConstants.IO_ERROR_CODE.getMessage(), e.getCause()); } } return true;
	 * }
	 */

	/**
	 * Method used for zipping a directory
	 * 
	 * @param inputDir      pass Directory name need to be zip example : String
	 *                      inputDir = "D:\\Testfiles\\TestDir";
	 * 
	 * @param destDirectory pass Zip file address as string example: String
	 *                      outputDir ="D:\\Testfiles\\compressedDir.zip";
	 * 
	 * @return true if zip directory is created
	 * 
	 * @throws FileNotFoundException when file is not found
	 * 
	 * @throws IOException           when file unable to read
	 */
	/*
	 * public static boolean zipDirectory(String inputDir, String destDirectory)
	 * throws IOException {
	 * 
	 * try (ZipOutputStream zipOut = new ZipOutputStream(new
	 * FileOutputStream(destDirectory))) {
	 * 
	 * File fileToZip = new File(inputDir); zipFileInDir(fileToZip,
	 * fileToZip.getName(), zipOut);
	 * 
	 * } catch (java.io.FileNotFoundException e) { throw new
	 * FileNotFoundException(ZipUtilConstants.FILE_NOT_FOUND_ERROR_CODE.getErrorCode
	 * (), ZipUtilConstants.FILE_NOT_FOUND_ERROR_CODE.getMessage(), e.getCause()); }
	 * catch (java.io.IOException e) { throw new
	 * IOException(ZipUtilConstants.IO_ERROR_CODE.getErrorCode(),
	 * ZipUtilConstants.IO_ERROR_CODE.getMessage(), e.getCause()); } return true; }
	 */

	/**
	 * Inner method of zipDirectory Method, called for zip all files of the given
	 * Directory
	 * 
	 * @param fileToZip files from given Directory
	 * @param fileName  file names from given Directory
	 * @return true if zip created in directory
	 * @throws FileNotFoundException when file is not found
	 * @throws IOException           when file unable to read
	 */

	/*
	 * private static boolean zipFileInDir(File fileToZip, String fileName,
	 * ZipOutputStream zipOut) throws IOException {
	 * 
	 * if (fileToZip.isHidden()) { return false; } if (fileToZip.isDirectory()) {
	 * File[] children = fileToZip.listFiles(); for (File childFile : children) {
	 * zipFileInDir(childFile, fileName + File.separator + childFile.getName(),
	 * zipOut); } return false; } try (FileInputStream fis = new
	 * FileInputStream(fileToZip)) { ZipEntry zipEntry = new ZipEntry(fileName);
	 * zipOut.putNextEntry(zipEntry); readFile(zipOut, fis); } catch
	 * (java.io.FileNotFoundException e) { throw new
	 * FileNotFoundException(ZipUtilConstants.FILE_NOT_FOUND_ERROR_CODE.getErrorCode
	 * (), ZipUtilConstants.FILE_NOT_FOUND_ERROR_CODE.getMessage(), e.getCause()); }
	 * catch (java.io.IOException e) { throw new
	 * IOException(ZipUtilConstants.IO_ERROR_CODE.getErrorCode(),
	 * ZipUtilConstants.IO_ERROR_CODE.getMessage(), e.getCause()); } return true; }
	 */

	/**
	 * This method to UnZip files from Zipped File. It will unZip only zip files,
	 * not zippedDir.
	 * 
	 * @param inputZipFile pass Zip file address as String example : String inputDir
	 *                     = "D:\\Testfiles\\compressedDir.zip";
	 * 
	 * @param outputUnZip  pass UpZipfile address as string example : String
	 *                     inputDir = "D:\\Testfiles\\unzip";
	 * 
	 * @return true if given zipped file is unziped
	 * 
	 * @throws FileNotFoundException when file is not found
	 * 
	 * @throws IOException           when file unable to read
	 */

	/*
	 * public static boolean unZipFile(String inputZipFile, String outputUnZip)
	 * throws IOException {
	 * 
	 * try (ZipInputStream zis = new ZipInputStream(new
	 * FileInputStream(inputZipFile))) { ZipEntry zipEntry = zis.getNextEntry();
	 * 
	 * while (zipEntry != null) { String fileName = zipEntry.getName(); File newFile
	 * = new File(outputUnZip + fileName); createOutputFile(zis, newFile); zipEntry
	 * = zis.getNextEntry(); } } catch (java.io.FileNotFoundException e) { throw new
	 * FileNotFoundException(ZipUtilConstants.FILE_NOT_FOUND_ERROR_CODE.getErrorCode
	 * (), ZipUtilConstants.FILE_NOT_FOUND_ERROR_CODE.getMessage(), e.getCause()); }
	 * catch (java.lang.NullPointerException e) { throw new
	 * NullPointerException(ZipUtilConstants.FILE_NOT_FOUND_ERROR_CODE.getErrorCode(
	 * ), ZipUtilConstants.NULL_POINTER_ERROR_CODE.getMessage(), e.getCause()); }
	 * catch (java.io.IOException e) { throw new
	 * IOException(ZipUtilConstants.IO_ERROR_CODE.getErrorCode(),
	 * ZipUtilConstants.IO_ERROR_CODE.getMessage(), e.getCause()); } return true; }
	 */

	/**
	 * This is inner method for unZipFile method used for created output folder
	 *
	 * @param zipInStream next Entry inside the zip folder
	 * @param newFile     output unZip file
	 * @throws FileNotFoundException when file is not found
	 * @throws IOException           when file unable to read
	 */
	/*
	 * private static void createOutputFile(ZipInputStream zipInStream, File
	 * newFile) throws java.io.IOException, FileNotFoundException {
	 * 
	 * byte[] buffer = new byte[1024]; try (FileOutputStream fos = new
	 * FileOutputStream(newFile)) { int len; while ((len = zipInStream.read(buffer))
	 * > 0) { fos.write(buffer, 0, len); } } catch (java.io.FileNotFoundException e)
	 * { throw new
	 * FileNotFoundException(ZipUtilConstants.FILE_NOT_FOUND_ERROR_CODE.getErrorCode
	 * (), ZipUtilConstants.FILE_NOT_FOUND_ERROR_CODE.getMessage(), e.getCause()); }
	 * }
	 */

	/**
	 * Extracts {@code zipFilePath} into {@code destDirectory} with a basic zip-slip check.
	 *
	 * @param zipFilePath   never-null, never-blank path to the zip file
	 * @param destDirectory never-null destination directory (created if missing)
	 * @return {@code true} if extraction completed
	 * @throws FileNotFoundException when the zip file is missing
	 * @throws IOException           when extraction I/O fails
	 * @throws Exception             zip-slip or other archive errors
	 * @deprecated use {@link #unZipDirectory(String, String, int, long, int)} with thresholds
	 */
	@Deprecated
	public static boolean unZipDirectory(String zipFilePath, String destDirectory) throws Exception {
		File destDir = new File(destDirectory);
		String canonicalDestinationDirPath = destDir.getCanonicalPath();

		if (!destDir.exists()) {
			boolean isCreated = destDir.mkdir();
			if (!isCreated) {
				throw new IOException(ZipUtilConstants.IO_ERROR_CODE.getErrorCode(),
						ZipUtilConstants.IO_ERROR_CODE.getMessage());
			}
		}

		try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath))) {

			ZipEntry entry = zipIn.getNextEntry();    //NOSONAR Setting the ZipEntry here.
			while (entry != null) {

				String filePath = destDirectory + File.separator + entry.getName();
				File dir = new File(filePath);
				String canonicalDestinationFile = dir.getCanonicalPath();

				if (!canonicalDestinationFile.startsWith(canonicalDestinationDirPath + File.separator)) {
					throw new ZipException(ZipUtilConstants.ARCHIVER_ERROR_CODE.getMessage());
				}

				if (!entry.isDirectory()) {
					boolean isCreated = new File(filePath).getParentFile().mkdirs();
					if (!isCreated) {
						throw new IOException(ZipUtilConstants.IO_ERROR_CODE.getErrorCode(),
								ZipUtilConstants.IO_ERROR_CODE.getMessage());
					}
					extractFile(zipIn, filePath);
				} else {
					boolean isCreated = dir.mkdirs();
					if (!isCreated) {
						throw new IOException(ZipUtilConstants.IO_ERROR_CODE.getErrorCode(),
								ZipUtilConstants.IO_ERROR_CODE.getMessage());
					}
				}
				zipIn.closeEntry();
				entry = zipIn.getNextEntry();     //NOSONAR Setting the Next Entry here.
			}
		} catch (java.io.FileNotFoundException e) {
			throw new FileNotFoundException(ZipUtilConstants.FILE_NOT_FOUND_ERROR_CODE.getErrorCode(),
					ZipUtilConstants.FILE_NOT_FOUND_ERROR_CODE.getMessage(), e.getCause());
		} catch (java.io.IOException e) {
			throw new IOException(ZipUtilConstants.IO_ERROR_CODE.getErrorCode(),
					ZipUtilConstants.IO_ERROR_CODE.getMessage(), e.getCause());
		}

		return true;
	}

	/**
	 * Extracts {@code zipFilePath} with zip-slip protection and bomb thresholds.
	 *
	 * @param zipFilePath      never-null, never-blank path to the zip file
	 * @param destDirectory    never-null destination under the current working directory
	 * @param thresholdEntries maximum number of zip entries allowed
	 * @param thresholdSize    maximum total uncompressed bytes allowed
	 * @param thresholdRatio   maximum uncompressed/compressed ratio per entry
	 * @return {@code true} if extraction completed
	 * @throws FileNotFoundException when the zip file is missing
	 * @throws IOException           when extraction I/O fails
	 * @throws Exception             zip-slip, threshold, or other archive errors
	 */
	public static boolean unZipDirectory(String zipFilePath, String destDirectory, int thresholdEntries,
			long thresholdSize, int thresholdRatio) throws Exception {
		zipSlipCheck(destDirectory);
		File destDir = new File(destDirectory);

		if (!destDir.exists()) {
			boolean isCreated = destDir.mkdir();
			if (!isCreated) {
				throw new IOException(ZipUtilConstants.IO_ERROR_CODE.getErrorCode(),
						ZipUtilConstants.IO_ERROR_CODE.getMessage());
			}
		}

		try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath))) {

			int totalEntries = 0;
			long totalReadArchiveSize = 0;
			ZipEntry zipEntry;
			while ((zipEntry = zipIn.getNextEntry()) != null) {
				totalEntries++;
				Path newPath = zipSlipProtect(zipEntry, Paths.get(destDir.toURI()));
				if (zipEntry.isDirectory()) {
					Files.createDirectories(newPath);
				} else {
					byte[] fileSize = IOUtils.toByteArray(zipIn);
					double compressionRatio = (double) fileSize.length / zipEntry.getCompressedSize();
					if (compressionRatio > thresholdRatio) {
						throw new ZipException(ZipUtilConstants.THRESHOLD_RATIO_EXCEPTION.getMessage());
					}
					totalReadArchiveSize = totalReadArchiveSize + fileSize.length;
					if (totalReadArchiveSize > thresholdSize) {
						throw new ZipException(ZipUtilConstants.THRESHOLD_SIZE_EXCEPTION.getMessage());
					}

					if (newPath.getParent() != null && Files.notExists(newPath.getParent())) {
							Files.createDirectories(newPath.getParent());
					}
					try (FileOutputStream fos = new FileOutputStream(newPath.toFile())) {
						byte[] buffer = new byte[1024];
						int len;
						while ((len = zipIn.read(buffer)) > 0) {
							fos.write(buffer, 0, len);
						}
					}
				}
				if (totalEntries > thresholdEntries) {
					throw new ZipException(ZipUtilConstants.THRESHOLD_ENTRIES_EXCEPTION.getMessage());
				}
				zipIn.closeEntry();
			}
		} catch (java.io.FileNotFoundException e) {
			throw new FileNotFoundException(ZipUtilConstants.FILE_NOT_FOUND_ERROR_CODE.getErrorCode(),
					ZipUtilConstants.FILE_NOT_FOUND_ERROR_CODE.getMessage(), e.getCause());
		} catch (java.io.IOException e) {
			throw new IOException(ZipUtilConstants.IO_ERROR_CODE.getErrorCode(),
					ZipUtilConstants.IO_ERROR_CODE.getMessage(), e.getCause());
		}

		return true;
	}

	/**
	 * Resolves {@code zipEntry} under {@code targetDir} and rejects path traversal.
	 *
	 * @param zipEntry  never-null archive entry
	 * @param targetDir never-null extraction root
	 * @return never-null normalized path inside {@code targetDir}
	 * @throws IOException  when path resolution fails
	 * @throws ZipException when the entry would escape {@code targetDir}
	 */
	public static Path zipSlipProtect(ZipEntry zipEntry, Path targetDir) throws IOException, ZipException {
		Path targetDirResolved = targetDir.resolve(zipEntry.getName());
		Path normalizePath = targetDirResolved.normalize();
		if (!normalizePath.startsWith(targetDir)) {
			throw new ZipException(ZipUtilConstants.ARCHIVER_ERROR_CODE.getMessage());
		}

		return normalizePath;
	}

	/**
	 * Rejects {@code destDirectory} that is not under the current working directory.
	 *
	 * @param destDirectory never-null destination path
	 * @throws java.io.IOException when the path cannot be canonicalized or traverses outside CWD
	 */
	private static void zipSlipCheck(String destDirectory) throws java.io.IOException {
		String canonicalDestinationPath = new File(destDirectory).getCanonicalPath();
		String canonicalCurrentPath = new File(".").getCanonicalPath();
		if (!canonicalDestinationPath.contains(canonicalCurrentPath)) {
			throw new ZipException(ZipUtilConstants.PATH_TRAVERSAL_EXCEPTION.getMessage());
		}
	}

	/**
	 * This is inner method for Extracts a zip entry (file entry)
	 * 
	 * @param zipIn    Inner entries
	 * @param filePath output Directory
	 * @throws IOException when file unable to read
	 */
	private static void extractFile(ZipInputStream zipIn, String filePath) throws java.io.IOException {
		try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
			byte[] bytesIn = new byte[10000];
			int read = 0;
			while ((read = zipIn.read(bytesIn)) != -1) {
				bos.write(bytesIn, 0, read);
			}
		}
	}
}
