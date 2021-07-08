package io.mosip.kernel.keymigrate.constant;

public interface KeyMigratorConstants {
    
    String SESSIONID = "keyMigrateSessionID";

	String BASE_KEY = "BASE_KEY";

	String EMPTY = "";

    String PARTNER_APPID = "PARTNER";

    String MIGRAION_SUCCESS = "Migration Success";

    String MIGRAION_FAILED = "Error in Migration";

    String MIGRAION_NOT_ALLOWED = "Migration Not Allowed. Valid Key Exists.";

    String ZK_KEYS = "ZK_KEYS";

    String ZK_TEMP_KEY_APP_ID = "KEY_MIGRATE";

    String ZK_TEMP_KEY_REF_ID = "ZK_TEMP_KEY";

    String ZK_CERT_COMMON_NAME = "ZKKeysSelfSignedKey";

    String ACTIVE_STATUS = "Active";
}
