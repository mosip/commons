package io.mosip.kernel.keymanager.hsm.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStore.SecretKeyEntry;
import java.security.KeyStoreSpi;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import io.mosip.kernel.keymanager.hsm.impl.KeyStoreImpl;
import io.mosip.kernel.keymanager.hsm.impl.pkcs.PKCS12KeyStoreImpl;
import io.mosip.kernel.keymanager.hsm.util.CertificateUtility;
import io.mosip.kernel.core.keymanager.model.CertificateParameters;

@RunWith(SpringRunner.class)
public class KeyStoreImplTest {

	private java.security.KeyStore keyStore;

	private PKCS12KeyStoreImpl pkcs12Impl;

	private KeyStoreImpl keyStoreImpl;

	private CertificateParameters certParams;

	BouncyCastleProvider provider;
	SecureRandom random;

	@Before
	public void setUp() throws Exception {
		KeyStoreSpi keyStoreSpiMock = mock(KeyStoreSpi.class);
		keyStore = new java.security.KeyStore(keyStoreSpiMock, null, "test") {
		};
		keyStoreImpl = new KeyStoreImpl();
		
		Map<String, String> map = new HashMap<>();
		map.put("CONFIG_FILE_PATH", "configPath");
		map.put("PKCS11_KEYSTORE_PASSWORD", "keystorePass");
		map.put("SYM_KEY_ALGORITHM", "AES");
		map.put("SYM_KEY_SIZE", "256");
		map.put("ASYM_KEY_ALGORITHM", "RSA");
		map.put("ASYM_KEY_SIZE", "2048");
		map.put("CERT_SIGN_ALGORITHM", "SHA256withRSA");
		pkcs12Impl = new PKCS12KeyStoreImpl(map);
		//ReflectionTestUtils.setField(pkcs12Impl, "keyStore", keyStore);
		keyStore.load(null);
		pkcs12Impl.setKeyStore(keyStore);
		ReflectionTestUtils.setField(keyStoreImpl, "keyStore", pkcs12Impl);
		provider = new BouncyCastleProvider();
		Security.addProvider(provider);
		random = new SecureRandom();
		certParams = new CertificateParameters("commonName", "organizationalUnit",
				"organization", "location", "state", "country", LocalDateTime.now(), LocalDateTime.now().plusDays(100));
	}

	@Test
	public void testStoreAsymmetricKey() throws Exception {
		KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA", provider);
		keyGenerator.initialize(2048, random);
		KeyPair keyPair = keyGenerator.generateKeyPair();
		keyStoreImpl.generateAndStoreAsymmetricKey("alias", null, certParams);
		X509Certificate[] chain = new X509Certificate[1];
		chain[0] = CertificateUtility.generateX509Certificate(keyPair.getPrivate(), keyPair.getPublic(), "commonName", "organizationalUnit",
				"organization", "country", LocalDateTime.now(), LocalDateTime.now().plusDays(100), "SHA256withRSA", "BC");
		PrivateKeyEntry keyEntry = new PrivateKeyEntry(keyPair.getPrivate(), chain);
		when(keyStore.entryInstanceOf("alias", PrivateKeyEntry.class)).thenReturn(true);
		when(keyStore.getEntry(Mockito.anyString(), Mockito.any())).thenReturn(keyEntry);
		assertThat(keyStoreImpl.getPrivateKey("alias"), isA(PrivateKey.class));
	}

	@Test
	public void testStoreSymmetricKey() throws Exception {
		javax.crypto.KeyGenerator keyGenerator = javax.crypto.KeyGenerator.getInstance("AES");
		keyGenerator.init(256, random);
		SecretKeyEntry secretKeyEntry = new SecretKeyEntry(keyGenerator.generateKey());
		keyStoreImpl.generateAndStoreSymmetricKey("alias");
		when(keyStore.entryInstanceOf("alias", SecretKeyEntry.class)).thenReturn(true);
		when(keyStore.getEntry(Mockito.anyString(), Mockito.any())).thenReturn(secretKeyEntry);
		assertThat(keyStoreImpl.getSymmetricKey("alias"), isA(Key.class));
	}

	@Test
	public void testDeleteKey() throws Exception {
		keyStoreImpl.deleteKey("alias");
		assertThat(keyStoreImpl.getKey("alias"), is(nullValue()));
	}

	@Test
	public void testGetPrivateKey() throws Exception {
		KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA", provider);
		keyGenerator.initialize(2048, random);
		KeyPair keyPair = keyGenerator.generateKeyPair();
		X509Certificate[] chain = new X509Certificate[1];
		chain[0] = CertificateUtility.generateX509Certificate(keyPair.getPrivate(), keyPair.getPublic(), "commonName", "organizationalUnit",
				"organization", "country", LocalDateTime.now(), LocalDateTime.now().plusDays(100),  "SHA256withRSA", "BC");
		PrivateKeyEntry keyEntry = new PrivateKeyEntry(keyPair.getPrivate(), chain);
		when(keyStore.entryInstanceOf("alias", PrivateKeyEntry.class)).thenReturn(true);
		when(keyStore.getEntry(Mockito.anyString(), Mockito.any())).thenReturn(keyEntry);
		assertThat(keyStoreImpl.getPrivateKey("alias"), isA(PrivateKey.class));
	}

	@Test
	public void testGetPublicKey() throws Exception {
		KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA", provider);
		keyGenerator.initialize(2048, random);
		KeyPair keyPair = keyGenerator.generateKeyPair();
		X509Certificate[] chain = new X509Certificate[1];
		chain[0] = CertificateUtility.generateX509Certificate(keyPair.getPrivate(), keyPair.getPublic(), "commonName", "organizationalUnit",
				"organization", "country", LocalDateTime.now(), LocalDateTime.now().plusDays(100), "SHA256withRSA", "BC");
		PrivateKeyEntry keyEntry = new PrivateKeyEntry(keyPair.getPrivate(), chain);
		when(keyStore.entryInstanceOf("alias", PrivateKeyEntry.class)).thenReturn(true);
		when(keyStore.getEntry(Mockito.anyString(), Mockito.any())).thenReturn(keyEntry);
		assertThat(keyStoreImpl.getPublicKey("alias"), isA(PublicKey.class));
	}

	@Test
	public void testGetCertificate() throws Exception {
		KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA", provider);
		keyGenerator.initialize(2048, random);
		KeyPair keyPair = keyGenerator.generateKeyPair();
		X509Certificate[] chain = new X509Certificate[1];
		chain[0] = CertificateUtility.generateX509Certificate(keyPair.getPrivate(), keyPair.getPublic(), "commonName", "organizationalUnit",
				"organization", "country", LocalDateTime.now(), LocalDateTime.now().plusDays(100), "SHA256withRSA", "BC");
		PrivateKeyEntry keyEntry = new PrivateKeyEntry(keyPair.getPrivate(), chain);
		when(keyStore.entryInstanceOf("alias", PrivateKeyEntry.class)).thenReturn(true);
		when(keyStore.getEntry(Mockito.anyString(), Mockito.any())).thenReturn(keyEntry);
		assertThat(keyStoreImpl.getCertificate("alias"), isA(X509Certificate.class));
	}

	@Test
	public void testGetAllAlias() throws Exception {
		@SuppressWarnings("unchecked")
		Enumeration<String> enumeration = mock(Enumeration.class);
		when(keyStore.aliases()).thenReturn(enumeration);
		assertThat(keyStoreImpl.getAllAlias(), isA(List.class));
	}

	@Test
	public void testGetKey() throws Exception {
		Key key = mock(Key.class);
		when(keyStore.getKey(Mockito.anyString(), Mockito.any())).thenReturn(key);
		assertThat(keyStoreImpl.getKey("alias"), isA(Key.class));
	}

	@Test
	public void testGetSymmetricKey() throws Exception {
		javax.crypto.KeyGenerator keyGenerator = javax.crypto.KeyGenerator.getInstance("AES", provider);
		keyGenerator.init(256, random);
		SecretKeyEntry secretKeyEntry = new SecretKeyEntry(keyGenerator.generateKey());
		when(keyStore.entryInstanceOf("alias", SecretKeyEntry.class)).thenReturn(true);
		when(keyStore.getEntry(Mockito.anyString(), Mockito.any())).thenReturn(secretKeyEntry);
		assertThat(keyStoreImpl.getSymmetricKey("alias"), isA(Key.class));
	}

}
