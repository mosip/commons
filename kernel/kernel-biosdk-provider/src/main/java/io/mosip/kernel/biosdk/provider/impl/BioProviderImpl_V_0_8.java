package io.mosip.kernel.biosdk.provider.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import io.mosip.kernel.biometrics.constant.BiometricFunction;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.Match;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.model.Decision;
import io.mosip.kernel.biometrics.model.MatchDecision;
import io.mosip.kernel.biometrics.model.QualityCheck;
import io.mosip.kernel.biometrics.model.Response;
import io.mosip.kernel.biometrics.spi.IBioApi;
import io.mosip.kernel.biosdk.provider.spi.iBioProviderApi;
import io.mosip.kernel.biosdk.provider.util.BioProviderUtil;
import io.mosip.kernel.biosdk.provider.util.ProviderConstants;
import io.mosip.kernel.core.bioapi.exception.BiometricException;
import io.mosip.kernel.core.bioapi.model.KeyValuePair;




@Component
public class BioProviderImpl_V_0_8 implements iBioProviderApi {
	
	private static final String API_VERSION = "0.8";
	private Map<BiometricType, Map<BiometricFunction, IBioApi>> sdkRegistry = new HashMap<>();
	//TODO - as sdk instance is heavy (around 2GB), rethink on the way of reusing the instances

	@Override
	public Map<BiometricType, List<BiometricFunction>> init(Map<BiometricType, Map<String, String>> params) 
			throws BiometricException {
		for(BiometricType modality : params.keySet()) {
			Map<String, String> modalityParams = params.get(modality);
			
			//check if version matches supported API version of this provider
			if(modalityParams != null && !modalityParams.isEmpty() 
					&& API_VERSION.equals(modalityParams.get(ProviderConstants.VERSION))) {				
				IBioApi iBioApi = (IBioApi) BioProviderUtil.getSDKInstance(modalityParams);
				addToRegistry(iBioApi, modality);			
			}
		}
		return getSupportedModalities();	
	}

	@Override
	public boolean verify(List<BIR> sample, List<BIR> record, BiometricType modality, Map<String, String> flags) {		
		BiometricRecord galleryRecord = getBiometricRecord(record.toArray(new BIR[record.size()]));
		Response<MatchDecision[]> response = sdkRegistry.get(modality).get(BiometricFunction.MATCH).match(
				getBiometricRecord(sample.toArray(new BIR[sample.size()])), new BiometricRecord[] { galleryRecord },
				Arrays.asList(modality), flags);	
		if (isSuccessResponse(response)) {
			Map<BiometricType, Decision> decisions = response.getResponse()[0].getDecisions();
			if (decisions.containsKey(modality)) {
				Match matchResult = decisions.get(modality).getMatch();
				// TODO log analyticsinfo and errors
				return Match.MATCHED.equals(matchResult);
			}
		}

		return false;
	}

	@Override
	public Map<String, Boolean> identify(List<BIR> sample, Map<String, List<BIR>> gallery, BiometricType modality,
			Map<String, String> flags) {
		Map<String, Integer> keyIndexMapping = new HashMap<>();
		BiometricRecord galleryRecords[] = new BiometricRecord[gallery.size()];
		int i = 0;
		for (String key : gallery.keySet()) {
			keyIndexMapping.put(key, i);
			galleryRecords[i++] = getBiometricRecord(gallery.get(key).toArray(new BIR[gallery.get(key).size()]));
		}

		Response<MatchDecision[]> response = sdkRegistry.get(modality).get(BiometricFunction.MATCH).match(
				getBiometricRecord(sample.toArray(new BIR[sample.size()])), galleryRecords, Arrays.asList(modality),
				flags);

		Map<String, Boolean> result = new HashMap<>();
		if (isSuccessResponse(response)) {
			keyIndexMapping.forEach((key, index) -> {
				if (response.getResponse()[index].getDecisions().containsKey(modality)) {
					result.put(key, Match.MATCHED
							.equals(response.getResponse()[index].getDecisions().get(modality).getMatch()));
					// TODO log analyticsinfo and errors
				} else
					result.put(key, false);
			});
		}
		return result;

	}

	@Override
	public float[] getSegmentQuality(BIR[] sample, Map<String, String> flags) {
		
		
		float score[] = new float[sample.length];
		for (int i = 0; i < sample.length; i++) {
			BiometricType modality = BiometricType.fromValue(sample[i].getBdbInfo().getType().get(0).value());
			Response<QualityCheck> response = sdkRegistry.get(modality).get(BiometricFunction.QUALITY_CHECK)
					.checkQuality(getBiometricRecord(sample[i]), Arrays.asList(modality), flags);
	
			
			if (isSuccessResponse(response) && response.getResponse().getScores() != null) {
				score[i] = isSuccessResponse(response) ? response.getResponse().getScores().get(modality).getScore() : 0;
				
				// TODO log analyticsInfo && errors
			} else
				score[i] = 0;
		}
			//TODO log the analytics info
			
		return score;
	}
	
	@Override
	public Map<BiometricType, Float> getModalityQuality(BIR[] sample, Map<String, String> flags) {
		Map<BiometricType, List<Float>> scoresByModality = new HashMap<>();
		for(int i=0; i< sample.length; i++) {
			BiometricType modality = BiometricType.fromValue(sample[i].getBdbInfo().getType().get(0).value());
			Response<QualityCheck> response = sdkRegistry.get(modality).get(BiometricFunction.QUALITY_CHECK)
					.checkQuality(getBiometricRecord(sample[i]),Arrays.asList(modality), flags);
			
			if(!scoresByModality.containsKey(modality))
				scoresByModality.put(modality, new ArrayList<>());
			
			scoresByModality.get(modality).add(isSuccessResponse(response) ? response.getResponse().getScores().get(modality).getScore() : 0);
			//TODO log the analytics info
		}
		
		Map<BiometricType, Float> result = new HashMap<>();
		scoresByModality.forEach((modality, scores) -> {
			result.put(modality, (float) scores.stream().mapToDouble(s -> s).average().getAsDouble());
		});
		return result;
	}

	@Override
	public List<BIR> extractTemplate(List<BIR> sample, Map<String, String> flags) {
		List<BIR> templates = new LinkedList<>();
		for(BIR bir : sample) {
			Response<BiometricRecord> response = sdkRegistry.get(BiometricType.fromValue(bir.getBdbInfo().getType().get(0).value())).
				get(BiometricFunction.EXTRACT).extractTemplate(getBiometricRecord(sample.toArray(new BIR[sample.size()])),null, flags);
			templates.addAll(isSuccessResponse(response) ? response.getResponse().getSegments() : null);
		}
		return templates;
	}
	
	
	/*
	 * private boolean match(String operation, List<BIR> sample, BIR[] record,
	 * BiometricType modality, Map<String, String> flags) { BiometricRecord
	 * galleryRecord = getBiometricRecord(record); List<MatchDecision[]> result =
	 * new LinkedList<>(); for(int i=0; i< sample.size(); i++) {
	 * Response<MatchDecision[]> response = sdkRegistry.get(modality).
	 * get(BiometricFunction.MATCH).match(getBiometricRecord(sample.get(i)), new
	 * BiometricRecord[] { galleryRecord },Arrays.asList(modality), flags);
	 * 
	 * result.add(isSuccessResponse(response) ? response.getResponse() : null); }
	 * 
	 * return evaluateMatchDecision(operation, sample, result); }
	 */
	private void addToRegistry(IBioApi iBioApi, BiometricType modality) {
		sdkRegistry.computeIfAbsent(modality, k -> new HashMap<>()).put(BiometricFunction.EXTRACT, iBioApi);
		sdkRegistry.computeIfAbsent(modality, k -> new HashMap<>()).put(BiometricFunction.QUALITY_CHECK, iBioApi);
		sdkRegistry.computeIfAbsent(modality, k -> new HashMap<>()).put(BiometricFunction.MATCH, iBioApi);
		sdkRegistry.computeIfAbsent(modality, k -> new HashMap<>()).put(BiometricFunction.SEGMENT, iBioApi);
	}
	
	private Map<BiometricType, List<BiometricFunction>> getSupportedModalities() {
		Map<BiometricType, List<BiometricFunction>> result = new HashMap<>();
		sdkRegistry.forEach((modality, map) -> {
			if(result.get(modality) == null)
				result.put(modality, new ArrayList<BiometricFunction>());
			
			result.get(modality).addAll(map.keySet());
		});
		return result;
	}
	
	private boolean isSuccessResponse(Response<?> response) {
		if(response != null && response.getStatusCode() >= 200 
				&& response.getStatusCode() <= 299 && response.getResponse() != null)
			return true;
		return false;
	}
	/*
	 * //TODO matching strategy based on caller (auth / dedupe) private boolean
	 * evaluateMatchDecision(String operation, List<BIR> sample,
	 * List<MatchDecision[]> result) { int segmentCount = sample.size(); result =
	 * result.stream().filter(decision -> decision !=
	 * null).collect(Collectors.toList());
	 * 
	 * switch (operation) { case "AUTH": if(result.size() < segmentCount) return
	 * false;
	 * 
	 * return result.stream().allMatch(decision ->
	 * Arrays.stream(decision).anyMatch(d -> d.isMatch()));
	 * 
	 * case "DEDUPE":
	 * 
	 * return result.stream().anyMatch(decision ->
	 * Arrays.stream(decision).anyMatch(d -> d.isMatch()));
	 * 
	 * } return false; }
	 */
	
	private KeyValuePair[] getKeyValuePairs(Map<String, String> flags) {
		if(flags == null)
			return null;
		
		int i=0;
		KeyValuePair kvp[] = new KeyValuePair[flags.size()]; 
		for(String k : flags.keySet()){
			KeyValuePair keyValuePair = new KeyValuePair();
			keyValuePair.setKey(k);
			keyValuePair.setValue(flags.get(k));
			kvp[i++] = keyValuePair;
		}
		return kvp;
	}
	
	// TODO - set cebffversion and version in biometricRecord
	private BiometricRecord getBiometricRecord(BIR[] birs) {
		BiometricRecord biometricRecord = new BiometricRecord();
		biometricRecord.setSegments(Arrays.asList(birs));
		
		return biometricRecord;
	}
	
	private BiometricRecord getBiometricRecord(BIR bir) {
		BiometricRecord biometricRecord = new BiometricRecord();
		biometricRecord.getSegments().add(bir);
		return biometricRecord;
	}
}
