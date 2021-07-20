package io.mosip.kernel.biometrics.constant;

public class OtherKey {

    public static final String EXCEPTION = "EXCEPTION"; //value is either "true" / "false"
    public static final String RETRIES = "RETRIES"; // value is "3" int as string
    public static final String FORCE_CAPTURED  = "FORCE_CAPTURED"; //value is either "true" / "false"
    public static final String SDK_SCORE  = "SDK_SCORE"; //value is "80", can range from 1-100
    public static final String CONFIGURED    = "CONFIGURED"; //its string, list of configured bio-attributes
    public static final String PAYLOAD = "PAYLOAD"; //string payload with placeholder for bioValue
    public static final String SPEC_VERSION = "SPEC_VERSION"; //string
}
