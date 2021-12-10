package io.mosip.kernel.biosdk.provider.test;
import io.mosip.kernel.biometrics.constant.BiometricFunction;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.model.MatchDecision;
import io.mosip.kernel.biometrics.model.QualityCheck;
import io.mosip.kernel.biometrics.model.Response;
import io.mosip.kernel.biometrics.model.SDKInfo;
import io.mosip.kernel.biometrics.spi.IBioApi;

import java.util.List;
import java.util.Map;

public class TestSDK implements IBioApi {

    @Override
    public SDKInfo init(Map<String, String> initParams) {
        SDKInfo sdkInfo = new SDKInfo("0.9", "1.0", "MOCKVendor1", "test");
        sdkInfo.withSupportedMethod(BiometricFunction.MATCH, BiometricType.FINGER);
        sdkInfo.withSupportedMethod(BiometricFunction.EXTRACT, BiometricType.FINGER);
        sdkInfo.withSupportedMethod(BiometricFunction.QUALITY_CHECK, BiometricType.FINGER);

        sdkInfo.withSupportedMethod(BiometricFunction.MATCH, BiometricType.FACE);
        sdkInfo.withSupportedMethod(BiometricFunction.EXTRACT, BiometricType.FACE);
        sdkInfo.withSupportedMethod(BiometricFunction.QUALITY_CHECK, BiometricType.FACE);
        return sdkInfo;
    }

    @Override
    public Response<QualityCheck> checkQuality(BiometricRecord sample, List<BiometricType> modalitiesToCheck, Map<String, String> flags) {
        return null;
    }

    @Override
    public Response<MatchDecision[]> match(BiometricRecord sample, BiometricRecord[] gallery, List<BiometricType> modalitiesToMatch, Map<String, String> flags) {
        return null;
    }

    @Override
    public Response<BiometricRecord> extractTemplate(BiometricRecord sample, List<BiometricType> modalitiesToExtract, Map<String, String> flags) {
        return null;
    }

    @Override
    public Response<BiometricRecord> segment(BiometricRecord sample, List<BiometricType> modalitiesToSegment, Map<String, String> flags) {
        return null;
    }

    @Override
    public BiometricRecord convertFormat(BiometricRecord sample, String sourceFormat, String targetFormat, Map<String, String> sourceParams, Map<String, String> targetParams, List<BiometricType> modalitiesToConvert) {
        return null;
    }
}