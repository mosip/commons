package io.mosip.kernel.biosdk.provider.test.dto;

import io.mosip.kernel.biometrics.constant.BiometricFunction;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.Match;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.model.Decision;
import io.mosip.kernel.biometrics.model.MatchDecision;
import io.mosip.kernel.biometrics.model.QualityCheck;
import io.mosip.kernel.biometrics.model.QualityScore;
import io.mosip.kernel.biometrics.model.Response;
import io.mosip.kernel.biometrics.model.SDKInfo;
import io.mosip.kernel.biometrics.spi.IBioApiV2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SDKInstanceTwo0_9 implements IBioApiV2 {


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
    	 Response<QualityCheck> response = new Response<>();
    	 QualityCheck qualityCheck = new QualityCheck();
    	 Map<BiometricType, QualityScore> scores = new HashMap<>();
    	 QualityScore qualityScore= new QualityScore();
    	 qualityScore.setScore(90.0F);
    	 scores.put(BiometricType.FINGER,qualityScore);
    	 qualityCheck.setScores(scores);
    	 response.setStatusCode(199);
    	 response.setResponse(qualityCheck);
    	return response;
    }

    @Override
    public Response<MatchDecision[]> match(BiometricRecord sample, BiometricRecord[] gallery, List<BiometricType> modalitiesToMatch, Map<String, String> flags) {
        Response<MatchDecision[]> response = new Response<>();
        MatchDecision matchDecision = new MatchDecision(0);
        Map<BiometricType, Decision> decisions = new HashMap<>();
        Decision decision = new Decision();
        decision.setMatch(Match.MATCHED);
        decisions.put(BiometricType.FINGER, decision);
        matchDecision.setDecisions(decisions);
        MatchDecision[] matchDecisions = new MatchDecision[1];
        matchDecisions[0]=matchDecision;
        response.setStatusCode(199);
        response.setResponse(matchDecisions);
        return response;
    }

    @Override
    public Response<BiometricRecord> extractTemplate(BiometricRecord sample, List<BiometricType> modalitiesToExtract, Map<String, String> flags) {
    	 Response<BiometricRecord> response = new Response<>();
    	 response.setStatusCode(199);
         response.setResponse(sample);
    	 return response;
    }

    @Override
    public Response<BiometricRecord> segment(BiometricRecord sample, List<BiometricType> modalitiesToSegment, Map<String, String> flags) {
        return null;
    }

    @Override
    public Response<BiometricRecord> convertFormatV2(BiometricRecord sample, String sourceFormat, String targetFormat, Map<String, String> sourceParams, Map<String, String> targetParams, List<BiometricType> modalitiesToConvert) {
        return null;
    }

    @Override
    public BiometricRecord convertFormat(BiometricRecord sample, String sourceFormat, String targetFormat, Map<String, String> sourceParams, Map<String, String> targetParams, List<BiometricType> modalitiesToConvert) {
        return null;
    }
}
