package io.mosip.kernel.core.fsadapter.spi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Stores and retrieves MOSIP registration packets on a distributed file system
 * (DFS / HDFS / object store).
 * <p>
 * Contract: implementations perform remote I/O. {@code id} is the packet /
 * RID correlation key and must be non-blank. Streams returned by getters
 * must be closed by the caller. Call from registration processor when
 * persisting or unpacking packets.
 * </p>
 *
 * @author Pranav Kumar
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 * @see io.mosip.kernel.core.fsadapter.exception.FSAdapterException
 */
public interface FileSystemAdapter {

	/**
	 * Returns whether a named file exists under the given packet id.
	 *
	 * @param id       never-null, never-blank packet identifier
	 * @param fileName never-null, never-blank relative file name
	 * @return {@code true} if the file exists
	 */
	public boolean checkFileExistence(String id, String fileName);

	/**
	 * Copies a file from one packet folder to another.
	 *
	 * @param sourceFolderName      never-null source packet id
	 * @param sourceFileName        never-null source relative path
	 * @param destinationFolderName never-null destination packet id
	 * @param destinationFileName   never-null destination relative path
	 * @return {@code true} if the copy succeeded
	 */
	public boolean copyFile(String sourceFolderName, String sourceFileName, String destinationFolderName,
			String destinationFileName);

	/**
	 * Deletes a named file under the given packet id.
	 *
	 * @param id       never-null, never-blank packet identifier
	 * @param fileName never-null, never-blank relative file name
	 * @return {@code true} if the file was deleted
	 */
	public boolean deleteFile(String id, String fileName);

	/**
	 * Deletes the entire packet corresponding to {@code id}.
	 *
	 * @param id never-null, never-blank packet identifier
	 * @return {@code true} if the packet was deleted
	 */
	public boolean deletePacket(String id);

	/**
	 * Opens a named file under the given packet id.
	 *
	 * @param id       never-null, never-blank packet identifier
	 * @param fileName never-null, never-blank relative file name
	 * @return never-null input stream; caller must close
	 */
	public InputStream getFile(String id, String fileName);

	/**
	 * Opens the packed packet corresponding to {@code id}.
	 *
	 * @param id never-null, never-blank packet identifier
	 * @return never-null packet stream; caller must close
	 */
	public InputStream getPacket(String id);

	/**
	 * Returns whether a packet exists for {@code id}.
	 *
	 * @param id never-null, never-blank packet identifier
	 * @return {@code true} if the packet is present
	 */
	public boolean isPacketPresent(String id);

	/**
	 * Stores a document under the given packet id and key.
	 *
	 * @param id       never-null, never-blank packet identifier
	 * @param key      never-null relative path / object key
	 * @param document never-null document stream; not closed by this method
	 * @return {@code true} if the store succeeded
	 */
	public boolean storeFile(String id, String key, InputStream document);

	/**
	 * Stores a packet stream under the given id.
	 *
	 * @param id   never-null, never-blank packet identifier
	 * @param file never-null packet stream; not closed by this method
	 * @return {@code true} if the store succeeded
	 */
	public boolean storePacket(String id, InputStream file);

	/**
	 * Stores a packet file from the local file system under the given id.
	 *
	 * @param id       never-null, never-blank packet identifier
	 * @param filePath never-null existing local packet file
	 * @return {@code true} if the store succeeded
	 */
	public boolean storePacket(String id, File filePath);

	/**
	 * Unzips the stored packet and uploads its individual files.
	 *
	 * @param id never-null, never-blank packet identifier
	 * @throws IOException when unzip or store I/O fails
	 */
	public void unpackPacket(String id) throws IOException;

}
