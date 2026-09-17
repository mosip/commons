/**
 * Virus-scan SPI for MOSIP packets and documents.
 */
package io.mosip.kernel.core.virusscanner.spi;

import java.io.File;
import java.io.IOException;

/**
 * Scans files, folders, and byte arrays for malware.
 * <p>
 * Contract: implementations typically perform HTTP or a local daemon call to
 * an antivirus engine. Paths and arrays must be non-null. Call before
 * accepting registration packets or uploaded documents. Return type
 * {@code U} is implementation-defined (often {@link Boolean}).
 * </p>
 *
 * @param <U> scan result type
 * @param <V> file-handle type used by {@link #scanFile(Object)}
 * @author Mukul Puspam
 */
public interface VirusScanner<U, V> {

	/**
	 * Scans the file at the given path.
	 *
	 * @param fileName never-null, never-blank file-system path
	 * @return scan result; never null on a completed scan
	 */
	U scanFile(String fileName);

	/**
	 * Scans the given file handle.
	 *
	 * @param file never-null file handle of type {@code V}
	 * @return scan result; never null on a completed scan
	 */
	U scanFile(V file);

	/**
	 * Recursively scans all files under the given folder.
	 *
	 * @param folderPath never-null, never-blank directory path
	 * @return scan result; never null on a completed scan
	 */
	U scanFolder(String folderPath);

	/**
	 * Scans an in-memory document.
	 *
	 * @param array never-null document bytes; may be empty
	 * @return scan result; never null on a completed scan
	 * @throws IOException when the scanner I/O fails
	 */
	U scanDocument(byte[] array) throws IOException;

	/**
	 * Scans the given {@link File}.
	 *
	 * @param doc never-null existing file
	 * @return scan result; never null on a completed scan
	 * @throws IOException when the scanner I/O fails
	 */
	U scanDocument(File doc) throws IOException;
}
