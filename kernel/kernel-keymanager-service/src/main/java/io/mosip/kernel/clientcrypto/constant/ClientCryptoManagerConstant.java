package io.mosip.kernel.clientcrypto.constant;

/**
 * @author Anusha Sunkada
 * @since 1.1.2
 */
public interface ClientCryptoManagerConstant {

    String SESSIONID = "ccSessionID";
    String INITIALIZATION = "INITIALIZATION";
    String EMPTY = "";

    String TPM = "TPM";
    String NON_TPM = "NON-TPM";

    String SERVER_PROD_PROFILE = "PROD";

    String KEY_PATH = System.getProperty("user.dir");
    String KEYS_DIR = ".mosipkeys";
    String DB_PWD_FILE = "db.conf";

    String ENABLED = "Y";
    String DISABLED = "N";
}
