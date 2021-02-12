package io.mosip.kernel.biosdk.provider.impl;
import static io.mosip.kernel.biosdk.provider.util.BioProviderUtil.getKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import io.mosip.kernel.biometrics.constant.BiometricFunction;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biosdk.provider.spi.iBioProviderApi;
import io.mosip.kernel.biosdk.provider.util.BioProviderUtil;
import io.mosip.kernel.biosdk.provider.util.ProviderConstants;
import io.mosip.kernel.core.bioapi.exception.BiometricException;
import io.mosip.kernel.core.bioapi.model.KeyValuePair;
import io.mosip.kernel.core.bioapi.model.MatchDecision;
import io.mosip.kernel.core.bioapi.model.QualityScore;
import io.mosip.kernel.core.bioapi.model.Response;
import io.mosip.kernel.core.bioapi.spi.IBioApi;
import io.mosip.kernel.core.cbeffutil.entity.BIR;

@Component
public class BioProviderImpl_V_0_8 implements iBioProviderApi {
	
	private static final String API_VERSION = "0.8";
	private Map<String, Map<BiometricFunction, IBioApi>> sdkRegistry = new HashMap<>();
	//TODO - as sdk instance is heavy (around 2GB), rethink on the way of reusing the instances

	@Override
	public Map<String, List<BiometricFunction>> init(Map<String, Map<String, String>> params) 
			throws BiometricException {
		for(String modality : params.keySet()) {
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
		sample = sample.stream().filter(obj -> modality == BiometricType.fromValue(obj.getBdbInfo().getType().get(0).value()))
			.collect(Collectors.toList());		
		return match("AUTH", sample, record.toArray(new BIR[record.size()]), modality, flags);
	}

	@Override
	public Map<String, Boolean> identify(List<BIR> sample, Map<String, List<BIR>> gallery, BiometricType modality,
			Map<String, String> flags) {
		Map<String, Boolean>  result = new HashMap<>();	
		
		sample = sample.stream().filter(obj -> modality == BiometricType.fromValue(obj.getBdbInfo().getType().get(0).value()))
				.collect(Collectors.toList());
		
		for(String key : gallery.keySet()) {			
			result.put(key, match("DEDUPE", sample, gallery.get(key).toArray(new BIR[gallery.get(key).size()]), modality, flags));
		}		
		return result;
	}

	@Override
	public float[] getSegmentQuality(BIR[] sample, Map<String, String> flags) {
		float score[] = new float[sample.length];
		for(int i=0; i< sample.length; i++) {
			Response<QualityScore> response = sdkRegistry.get(getKey(BiometricType.fromValue(sample[i].getBdbInfo().getType().get(0).value()), flags)).
					get(BiometricFunction.QUALITY_CHECK).checkQuality(sample[i], getKeyValuePairs(flags));
			
			score[i] = isSuccessResponse(response) ? response.getResponse().getScore() : 0;
			//TODO log the analytics info
		}		
		return score;
	}
	
	@Override
	public Map<BiometricType, Float> getModalityQuality(BIR[] sample, Map<String, String> flags) {
		Map<BiometricType, List<Float>> scoresByModality = new HashMap<>();
		for(int i=0; i< sample.length; i++) {
			BiometricType modality = BiometricType.fromValue(sample[i].getBdbInfo().getType().get(0).value());
			Response<QualityScore> response = sdkRegistry.get(getKey(modality, flags)).get(BiometricFunction.QUALITY_CHECK)
					.checkQuality(sample[i], getKeyValuePairs(flags));
			
			if(!scoresByModality.containsKey(modality))
				scoresByModality.put(modality, new ArrayList<>());
			
			scoresByModality.get(modality).add(isSuccessResponse(response) ? response.getResponse().getScore() : 0);
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
			Response<BIR> response = sdkRegistry.get(getKey(BiometricType.fromValue(bir.getBdbInfo().getType().get(0).value()), flags)).
				get(BiometricFunction.EXTRACT).extractTemplate(bir, getKeyValuePairs(flags));
			templates.add(isSuccessResponse(response) ? response.getResponse() : null);
		}
		return templates;
	}
	
	
	private boolean match(String operation, List<BIR> sample, BIR[] record, BiometricType modality, Map<String, String> flags) {
		List<MatchDecision[]> result = new LinkedList<>();
		for(int i=0; i< sample.size(); i++) {
			Response<MatchDecision[]> response = sdkRegistry.get(getKey(modality, flags)).
					get(BiometricFunction.MATCH).match(sample.get(i), record, getKeyValuePairs(flags));
			
			result.add(isSuccessResponse(response) ? response.getResponse() : null);
		}
		
		return evaluateMatchDecision(operation, sample, result);
	}

	private void addToRegistry(IBioApi iBioApi, String modality) {
		sdkRegistry.computeIfAbsent(modality, k -> new HashMap<>()).put(BiometricFunction.EXTRACT, iBioApi);
		sdkRegistry.computeIfAbsent(modality, k -> new HashMap<>()).put(BiometricFunction.QUALITY_CHECK, iBioApi);
		sdkRegistry.computeIfAbsent(modality, k -> new HashMap<>()).put(BiometricFunction.MATCH, iBioApi);
		sdkRegistry.computeIfAbsent(modality, k -> new HashMap<>()).put(BiometricFunction.SEGMENT, iBioApi);
	}
	
	private Map<String, List<BiometricFunction>> getSupportedModalities() {
		Map<String, List<BiometricFunction>> result = new HashMap<>();
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
	
	//TODO matching strategy based on caller (auth / dedupe)
	private boolean evaluateMatchDecision(String operation, List<BIR> sample, List<MatchDecision[]> result) {
		int segmentCount = sample.size();
		result = result.stream().filter(decision -> decision != null).collect(Collectors.toList());
		
		switch (operation) {
		case "AUTH":
			if(result.size() < segmentCount)
				return false;
			
			return result.stream().allMatch(decision -> Arrays.stream(decision).anyMatch(d -> d.isMatch()));
			
		case "DEDUPE":
			
			return result.stream().anyMatch(decision -> Arrays.stream(decision).anyMatch(d -> d.isMatch()));

		}		
		return false;
	}
	
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
}
