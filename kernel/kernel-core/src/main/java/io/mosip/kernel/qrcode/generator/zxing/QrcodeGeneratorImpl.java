package io.mosip.kernel.qrcode.generator.zxing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import io.mosip.kernel.core.qrcodegenerator.exception.QrcodeGenerationException;
import io.mosip.kernel.core.qrcodegenerator.spi.QrCodeGenerator;
import io.mosip.kernel.qrcode.generator.zxing.constant.QrVersion;
import io.mosip.kernel.qrcode.generator.zxing.constant.QrcodeConstants;
import io.mosip.kernel.qrcode.generator.zxing.constant.QrcodeExceptionConstants;
import io.mosip.kernel.qrcode.generator.zxing.util.QrcodegeneratorUtils;

/**
 * ZXing {@link QrCodeGenerator} that encodes a string as a PNG QR image.
 * <p>
 * Uses error-correction level {@code L}. Version selects both ZXing QR version
 * and pixel size via {@link QrVersion}. Binary payloads are split into 8-bit
 * groups, converted to characters, then encoded the same way.
 * </p>
 *
 * @author Urvil Joshi
 * @since 1.0.0
 * @see QrCodeGenerator
 */
@Component
public class QrcodeGeneratorImpl implements QrCodeGenerator<QrVersion> {

	/**
	 * {@link QRCodeWriter} instance
	 */
	private static QRCodeWriter qrCodeWriter;
	/**
	 * Configurations for QrCode Generator
	 */
	private static Map<EncodeHintType, Object> configMap;

	static {
		qrCodeWriter = new QRCodeWriter();
		configMap = new EnumMap<>(EncodeHintType.class);
		configMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
	}

	/**
	 * Encodes {@code data} as a PNG QR image of {@code version} size.
	 *
	 * @param data    payload to encode; must be non-null and non-blank
	 * @param version QR version and pixel size; must be non-null
	 * @return PNG bytes
	 * @throws QrcodeGenerationException if ZXing encoding fails
	 * @throws IOException               if the PNG cannot be written
	 * @throws io.mosip.kernel.core.exception.NullPointerException if {@code data} or {@code version} is {@code null}
	 * @throws io.mosip.kernel.core.qrcodegenerator.exception.InvalidInputException if {@code data} is blank
	 */
	@Override
	public byte[] generateQrCode(String data, QrVersion version) throws QrcodeGenerationException, IOException {
		QrcodegeneratorUtils.verifyInput(data, version);
		configMap.put(EncodeHintType.QR_VERSION, version.getVersion());
		BitMatrix byteMatrix = null;
		try {
			byteMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, version.getSize(), version.getSize(),
					configMap);
		} catch (WriterException | IllegalArgumentException exception) {
			throw new QrcodeGenerationException(QrcodeExceptionConstants.QRCODE_GENERATION_EXCEPTION.getErrorCode(),
					QrcodeExceptionConstants.QRCODE_GENERATION_EXCEPTION.getErrorMessage() + exception.getMessage(),
					exception);
		}
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		MatrixToImageWriter.writeToStream(byteMatrix, QrcodeConstants.FILE_FORMAT, outputStream);
		return outputStream.toByteArray();

	}

	/**
	 * Encodes a binary bit-string payload as a PNG QR image.
	 * <p>
	 * {@code data} is split into 8-bit groups, each parsed as a binary integer
	 * and converted to a character, then passed to {@link #generateQrCode(String, QrVersion)}.
	 * </p>
	 *
	 * @param data    concatenated 8-bit binary groups (for example {@code 01000001})
	 * @param version QR version and pixel size; must be non-null
	 * @return PNG bytes
	 * @throws QrcodeGenerationException if ZXing encoding fails
	 * @throws IOException               if the PNG cannot be written
	 * @throws io.mosip.kernel.core.exception.NullPointerException if {@code data} or {@code version} is {@code null}
	 * @throws io.mosip.kernel.core.qrcodegenerator.exception.InvalidInputException if {@code data} is blank
	 */
	@Override
	public byte[] generateQrCodeFromBinaryData(String data, QrVersion version)
			throws QrcodeGenerationException, IOException {
		QrcodegeneratorUtils.verifyInput(data, version);
		StringBuilder stringBuilder = new StringBuilder();
		Arrays.stream(data.split("(?<=\\G.{8})")).forEach(s -> stringBuilder.append((char) Integer.parseInt(s, 2))); 
		return generateQrCode(stringBuilder.toString(), version);
	}
}
