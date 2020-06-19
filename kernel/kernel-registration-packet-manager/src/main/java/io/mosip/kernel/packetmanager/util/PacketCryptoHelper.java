package io.mosip.kernel.packetmanager.util;

import io.mosip.kernel.core.crypto.spi.CryptoCoreSpec;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.keygenerator.bouncycastle.KeyGenerator;
import io.mosip.kernel.packetmanager.constants.ErrorCode;
import io.mosip.kernel.packetmanager.exception.PacketCreatorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;


@Component
public class PacketCryptoHelper {
	
	@Value("${mosip.kernel.data-key-splitter}")
	private String KEY_SPLITTER;
	
	@Autowired
	private KeyGenerator keyGenerator;
	
	@Autowired
    private CryptoCoreSpec<byte[], byte[], SecretKey, PublicKey, PrivateKey, String> cryptoCore;
	
	public byte[] encryptPacket(byte[] data, byte[] encryptionKey) throws PacketCreatorException {		
		//supports larger key lengths, Not required to specified in java 9
		Security.setProperty("crypto.policy", "unlimited");
		final SecretKey sessionKey = keyGenerator.getSymmetricKey();
		final byte[] cipherText = cryptoCore.symmetricEncrypt(sessionKey, data, null);		
		PublicKey publicKey = null;
		try {
			publicKey = KeyFactory.getInstance("RSA").generatePublic(
					new X509EncodedKeySpec(CryptoUtil.decodeBase64(new String(encryptionKey))));
		} catch (InvalidKeySpecException  | NoSuchAlgorithmException e) {
			throw new PacketCreatorException(ErrorCode.PACKET_ENCRYPT_ERROR.getErrorCode(),
					ErrorCode.PACKET_ENCRYPT_ERROR.getErrorMessage().concat(ExceptionUtils.getStackTrace(e)));
		}	
		byte[] encryptedSessionKey = cryptoCore.asymmetricEncrypt(publicKey, sessionKey.getEncoded());		
		return CryptoUtil.combineByteArray(cipherText, encryptedSessionKey, KEY_SPLITTER);
	}

}
