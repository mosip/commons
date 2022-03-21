package io.mosip.kernel.core.test.util;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.security.NoSuchAlgorithmException;

import org.junit.Test;

import io.mosip.kernel.core.util.HMACUtils2;

public class HMACUtilsTest {

	@Test
	public void testGenerateHash() throws NoSuchAlgorithmException{
		String name = "Bal Vikash Sharma";
		assertNotNull(HMACUtils2.generateHash(name.getBytes()));
	}


	@Test
	public void testDigestAsPlainText() throws NoSuchAlgorithmException{
		assertNotNull(HMACUtils2.digestAsPlainText("Bal Vikash Sharma".getBytes()));
	}
	
	@Test
	public void testDigestAsPlainWithSaltText() throws NoSuchAlgorithmException{
		assertNotNull(HMACUtils2.digestAsPlainTextWithSalt("testData".getBytes(),"randomsalt".getBytes()));
	}

	@Test
	public void testGenerateRandomIV() {
		assertNotEquals(HMACUtils2.generateSalt(), HMACUtils2.generateSalt());
	}

	@Test
	public void testGenerateRandomIVInputBytes() {
		assertNotEquals(HMACUtils2.generateSalt(16), HMACUtils2.generateSalt(16));
	}

}
