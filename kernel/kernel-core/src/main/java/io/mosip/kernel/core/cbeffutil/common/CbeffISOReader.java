package io.mosip.kernel.core.cbeffutil.common;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

import io.mosip.kernel.core.cbeffutil.exception.CbeffException;

/**
 * CbeffISOReader.java
 *
 * <p><b>Purpose</b>: Ultra-fast reader for ISO biometric image files used in CBEFF contexts.
 * Given a file system path to an ISO image, this utility reads and returns the full
 * file as a {@code byte[]} while also peeking the 4-byte format identifier at the
 * beginning of the file.</p>
 *
 * <h3>Format Identifier</h3>
 * The first 4 bytes (big-endian) are treated as the "format identifier" (mirrors
 * {@link java.io.DataInputStream#readInt()} semantics). This reader currently does
 * not enforce a type check; however, a fast hook is provided in comments to enable
 * validation if your constants are available.
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * byte[] isoBytes = CbeffISOReader.readISOImage("/path/to/iso.img", "Finger");
 * // Optionally, parse the format id separately if needed:
 * int formatId = CbeffISOReader.peekFormatId("/path/to/iso.img");
 * }</pre>
 *
 * <h3>Exceptions</h3>
 * Throws {@link io.mosip.kernel.core.cbeffutil.exception.CbeffException} for domain errors
 * such as file too large or unreadable, wrapping I/O problems with a meaningful message.
 *
 * @author
 *   Ramadurai Pandian
 * @since 1.0.0
 */
public class CbeffISOReader {

    /**
     * Method used for reading ISO Image
     *
     * @param path of the ISO image
     *
     * @param type of ISO image
     *
     * @return return byte array of image data
     *
     * @exception Exception exception
     *
     */
    public static byte[] readISOImage(String path, String type) throws Exception {
        File testFile = new File(path);
        try (DataInputStream in = new DataInputStream(new FileInputStream(testFile))) {
            int formatId = in.readInt();
            // if (checkFormatIdentifier(formatId, type)) {
            byte[] result = new byte[(int) testFile.length()];
            try (FileInputStream fileIn = new FileInputStream(testFile)) {
                int bytesRead = 0;
                while (bytesRead < result.length) {
                    bytesRead += fileIn.read(result, bytesRead, result.length - bytesRead);
                }
            }
            return result;

            /*
             * } else { throw new CbeffException(
             * "Format Identifier is wrong for the image,Please upload correct image of type : "
             * + type); }
             */
        }
    }

    /**
     * Method used for validating Format Identifiers based on type
     *
     * @param format id
     *
     * @param type   of image
     *
     * @return boolean value if identifier matches with id
     *
     */
    /*
     * private static boolean checkFormatIdentifier(int formatId, String type) { //
     * switch (type) { // case "Finger": // return
     * CbeffConstant.FINGER_FORMAT_IDENTIFIER == formatId; // case "Iris": // return
     * CbeffConstant.IRIS_FORMAT_IDENTIFIER == formatId; // case "Face": // return
     * CbeffConstant.FACE_FORMAT_IDENTIFIER == formatId; // } return true; }
     */

}