package io.mosip.kernel.keygenerator.generator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.keymanager.spi.KeyStore;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.keymanagerservice.constant.KeymanagerConstant;
import io.mosip.kernel.keymanagerservice.entity.DataEncryptKeystore;
import io.mosip.kernel.keymanagerservice.entity.KeyAlias;
import io.mosip.kernel.keymanagerservice.helper.KeymanagerDBHelper;
import io.mosip.kernel.keymanagerservice.repository.DataEncryptKeystoreRepository;

/**
 * The Class RandomKeysGenerator.
 *
 * @author Mahammed Taheer
 */
@SuppressWarnings("restriction")
@Component
public class RandomKeysGenerator {

    private static final String CREATED_BY = "system";

    private static final String WRAPPING_TRANSFORMATION = "AES/ECB/NoPadding";

    // Temperory fix to generate the Master AES key in nCipher because not able to generate key using SunPKCS11.
    private static final String NCIPHER_KEY_GEN_UTILITY = "/opt/nfast/bin/generatekey";

    private static final String GENERATION_SUCCESS_MESSAGE = "Key successfully generated.";

    @Value("${zkcrypto.random.key.generate.count}")
    private long noOfKeysRequire;

    private static final int TEN_YEARS_VALIDITY = 365 * 10;

    @Value("${mosip.kernel.keygenerator.symmetric-algorithm-name:AES}")
    private String symmetricKeyAlgo;

    @Value("${mosip.kernel.keygenerator.symmetric-key-length:256}")
    private int symmetricKeySize;

    @Value("${mosip.kernel.utility.keygenerate.allow:true}")
    private boolean utilityKeyGenerateAllow;
    
    /**
     * Keystore instance to handles and store cryptographic keys.
     */
    @Autowired
    private KeyStore keyStore;

    @Autowired
    private KeymanagerDBHelper dbHelper;
    

    @Autowired
    DataEncryptKeystoreRepository dataEncryptKeystoreRepository;

    public void generateRandomKeys(String appId, String referenceId) {

        LocalDateTime localDateTimeStamp = DateUtils.getUTCCurrentDateTime();
        Map<String, List<KeyAlias>> keyAliasMap = dbHelper.getKeyAliases(appId, referenceId, localDateTimeStamp);
        List<KeyAlias> currentKeyAlias = keyAliasMap.get(KeymanagerConstant.CURRENTKEYALIAS);
        String alias = null;
        if (currentKeyAlias.isEmpty()) {
            System.out.println("Cache Master key not available, generating new key.");
            try {
                alias = UUID.randomUUID().toString();
                generateAndStore(appId, referenceId, alias, localDateTimeStamp);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("ZK Encryption Master key generat failed.");
            }
        } else {
            alias = currentKeyAlias.get(0).getAlias();
        }
        try {
            generate10KKeysAndStoreInDB(alias);
        } catch (Exception e) {
            System.err.println("Error generating Random Keys.");
            e.printStackTrace();
        }
    }

    private void generateAndStore(String appId, String referenceId, String keyAlias, LocalDateTime localDateTimeStamp) throws Exception {
        boolean isNCipherHSMProv = isNCipherHSMProvider();
        if (isNCipherHSMProv && utilityKeyGenerateAllow) {
            generateKeyUsingNCipherUtility(keyAlias);
        } else {
            keyStore.generateAndStoreSymmetricKey(keyAlias);
        }
        dbHelper.storeKeyInAlias(appId, localDateTimeStamp, referenceId, keyAlias, localDateTimeStamp.plusDays(TEN_YEARS_VALIDITY));
    }
    
    private void generate10KKeysAndStoreInDB(String cacheMasterKeyAlias) throws Exception {
		
        int noOfActiveKeys = (int) dataEncryptKeystoreRepository.findAll().stream()
                                    .filter(k->k.getKeyStatus().equals("active")).count();			
		int noOfKeysToGenerate = 0;
		if((noOfKeysRequire-noOfActiveKeys) > 0) {
			noOfKeysToGenerate = (int) (noOfKeysRequire-noOfActiveKeys);
		}
		
		System.out.println("No Of Keys To Generate:" + noOfKeysToGenerate);
		
		Long maxid = dataEncryptKeystoreRepository.findMaxId();
		int startIndex = maxid == null ? 0 : maxid.intValue() + 1;
        
        SecureRandom rand = new SecureRandom();
		KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        Cipher cipher = Cipher.getInstance(WRAPPING_TRANSFORMATION);
        Key masterKey = keyStore.getSymmetricKey(cacheMasterKeyAlias);

		for (int i = startIndex; i < noOfKeysToGenerate; i++) {
			keyGenerator.init(256, rand);
			SecretKey sKey = keyGenerator.generateKey();
			cipher.init(Cipher.ENCRYPT_MODE, masterKey);
			byte[] wrappedKey = cipher.doFinal(sKey.getEncoded());
			String encodedKey = Base64.getEncoder().encodeToString(wrappedKey);
			insertKeyIntoTable(i, encodedKey, "Active");
			System.out.println("Insert secrets in DB: " + i);
		}
	}

	private void insertKeyIntoTable(int id, String secretData, String status) throws Exception {
		DataEncryptKeystore data = new DataEncryptKeystore();
		data.setId(id);
		data.setKey(secretData);
		data.setKeyStatus(status);
		data.setCrBy(CREATED_BY);
		data.setCrDTimes(LocalDateTime.now());
		dataEncryptKeystoreRepository.save(data);
    }
    
    private boolean isNCipherHSMProvider(){

        Path genKeyUtilityPath  = Paths.get(NCIPHER_KEY_GEN_UTILITY);
        if (Files.exists(genKeyUtilityPath) && Files.isExecutable(genKeyUtilityPath)) {
            return true;
        }

        return false;
    }

    private void generateKeyUsingNCipherUtility(String alias) throws Exception {
        List<String> commands = new ArrayList<String>();
        commands.add(NCIPHER_KEY_GEN_UTILITY);
        commands.add("--generate");
        commands.add("pkcs11");
        commands.add("type=" + symmetricKeyAlgo);
        commands.add("size=" + symmetricKeySize);
        commands.add("plainname=" + alias);
        commands.add("nvram=no");

        System.out.println("commands:: " + commands);
        try {
            Process process = new ProcessBuilder(commands).redirectErrorStream(true).start();

            List<String> cmdOutput = new ArrayList<String>();
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            boolean successMessageFound = false;
            while ((line = br.readLine()) != null ) {
                cmdOutput.add(line);
                if(line.equalsIgnoreCase(GENERATION_SUCCESS_MESSAGE)){
                    successMessageFound = true;
                }
            }
           
            if (0 != process.waitFor()) {
                System.err.println("Error Stream: " + cmdOutput);
                throw new Exception("Process waitFor - Error generating AES Key.");
            }

            if(!successMessageFound) {
                System.err.println("Error Stream: " + cmdOutput);
                throw new Exception("Process output - ZK encryption Master key generation failed.");
            }

        } catch (Exception e) {
            System.err.println("Error generating AES Key.");
            e.printStackTrace();
            throw new Exception("Exception Block - ZK encryption Master key generation failed.");
        }
    }
}
