package io.mosip.kernel.core.test.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.Test;

import io.mosip.kernel.core.crypto.exception.InvalidKeyException;
import io.mosip.kernel.core.crypto.exception.NullDataException;
import io.mosip.kernel.core.util.CryptoUtil;

public class CryptoUtilTest {
	
	private SecretKeySpec setSymmetricUp(int length, String algo) throws java.security.NoSuchAlgorithmException {
		SecureRandom random = new SecureRandom();
		byte[] keyBytes = new byte[length];
		random.nextBytes(keyBytes);
		return new SecretKeySpec(keyBytes, algo);
	}

	@Test
	public void testCombineByteArray() {
		assertThat(CryptoUtil.combineByteArray("data".getBytes(), "key".getBytes(), "#KEY_SPLITTER#"),
				isA(byte[].class));
	}

	@Test
	public void testGetSplitterIndex() {
		assertThat(CryptoUtil.getSplitterIndex("data#KEY_SPLITTER#data".getBytes(), 0, "#KEY_SPLITTER#"),
				isA(int.class));
	}

	@Test
	public void testEncodeBase64() {
		assertThat(CryptoUtil.encodeBase64("data".getBytes()), isA(String.class));
	}

	@Test
	public void testDecodeBase64() {
		assertThat(CryptoUtil.decodeBase64("data"), isA(byte[].class));
	}
	
	@Test
	public void testsymmetricEncrypt() throws NoSuchAlgorithmException {
		SecretKey secretKey = setSymmetricUp(32, "AES");
		byte[] encryptedData = CryptoUtil.symmetricEncrypt(secretKey, "testData".getBytes());
		assertThat(encryptedData, isA(byte[].class));
	}
	
	@Test(expected = NullDataException.class)
	public void testsymmetricEncryptNullData() throws NoSuchAlgorithmException {
		SecretKey secretKey = setSymmetricUp(32, "AES");
		assertThat(CryptoUtil.symmetricEncrypt(secretKey, null), isA(byte[].class));
	}
	
	@Test(expected = NullDataException.class)
	public void testsymmetricEncryptEmptyData() throws NoSuchAlgorithmException {
		SecretKey secretKey = setSymmetricUp(32, "AES");
		assertThat(CryptoUtil.symmetricEncrypt(secretKey, "".getBytes()), isA(byte[].class));
	}
	
	@Test(expected =InvalidKeyException.class)
	public void testsymmetricEncryptInvalidKey() throws NoSuchAlgorithmException {
		SecretKey secretKey = setSymmetricUp(64, "AES");
		assertThat(CryptoUtil.symmetricEncrypt(secretKey, "testData".getBytes()), isA(byte[].class));
	}
	
	
	@Test
	public void testEncodeDecodeToURLSafeBase64() {
		assertThat(new String(CryptoUtil.decodeURLSafeBase64(CryptoUtil.encodeToURLSafeBase64("data".getBytes()))), is("data"));
	}
	
	@Test
	public void testNullEncodeToURLSafeBase64() {
		assertNull(CryptoUtil.encodeToURLSafeBase64(null));
	}
	
	@Test
	public void testNullDecodeToURLSafeBase64() {
		assertNull(CryptoUtil.decodeURLSafeBase64(""));
	}
	
	
	@Test
	public void testEncodeDecodeToBase64() {
		assertThat(new String(CryptoUtil.decodePlainBase64(CryptoUtil.encodeToPlainBase64("data".getBytes()))), is("data"));
	}
	
	@Test
	public void testNullEncodeToBase64() {
		assertNull(CryptoUtil.encodeToPlainBase64(null));
	}
	
	@Test
	public void testNullDecodeToBase64() {
		assertNull(CryptoUtil.decodePlainBase64(""));
	}
	
	@Test
	public void testComputeFingerPrint() {
		assertThat(CryptoUtil.computeFingerPrint("testcert","testMetadata"),isA(String.class));
	}
	
	@Test
	public void testNullMetadataComputeFingerPrint() {
		assertThat(CryptoUtil.computeFingerPrint("testcert",null),isA(String.class));
	}
	
	

}
