package io.mosip.kernel.keygenerator.bouncycastle.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import io.mosip.kernel.core.exception.NoSuchAlgorithmException;
import io.mosip.kernel.keygenerator.bouncycastle.util.KeyGeneratorUtils;


@RunWith(SpringRunner.class)
public class KeyGeneratorExceptionTest {

	@Test(expected = NoSuchAlgorithmException.class)
	public void testGetAsymmetricKeyException() {
		KeyGeneratorUtils.getKeyPairGenerator("AES", 204);
	}

	@Test(expected = NoSuchAlgorithmException.class)
	public void testGetSymmetricKeyException() {
		KeyGeneratorUtils.getKeyGenerator("RSA", 204);
	}

}
