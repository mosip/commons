package io.mosip.kernel.core.qrcodegenerator.spi;

import java.io.IOException;

import io.mosip.kernel.core.qrcodegenerator.exception.QrcodeGenerationException;

/**
 * Encodes payload data as a PNG QR code image.
 * <p>
 * Contract: implementations perform CPU-bound encoding with no MOSIP HTTP.
 * {@code data} must be non-null; empty data may be rejected as
 * {@link io.mosip.kernel.core.qrcodegenerator.exception.InvalidInputException}.
 * Call when printing receipts or ID cards that embed a QR.
 * </p>
 *
 * @param <T> QR version / size type used by the implementation
 * @author Urvil Joshi
 * @since 1.0.0
 */
public interface QrCodeGenerator<T> {

	/**
	 * Encodes {@code data} as a PNG QR image.
	 *
	 * @param data    never-null text to encode
	 * @param version never-null QR version / size hint
	 * @return never-null PNG bytes
	 * @throws QrcodeGenerationException when encoding fails
	 * @throws IOException               when writing PNG bytes fails
	 */
	byte[] generateQrCode(String data, T version) throws QrcodeGenerationException, IOException;

	/**
	 * Encodes binary {@code data} (typically hex or Base64) as a PNG QR image.
	 *
	 * @param data    never-null binary payload represented as text
	 * @param version never-null QR version / size hint
	 * @return never-null PNG bytes
	 * @throws QrcodeGenerationException when encoding fails
	 * @throws IOException               when writing PNG bytes fails
	 */
	byte[] generateQrCodeFromBinaryData(String data, T version) throws QrcodeGenerationException, IOException;
}
