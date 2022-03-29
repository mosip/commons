package io.mosip.kernel.biosdk.provider.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.springframework.data.util.ReflectionUtils;
import org.springframework.stereotype.Component;

import io.mosip.kernel.biometrics.constant.BiometricFunction;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biosdk.provider.spi.iBioProviderApi;
import io.mosip.kernel.biosdk.provider.util.BioProviderUtil;
import io.mosip.kernel.biosdk.provider.util.BioSDKProviderLoggerFactory;
import io.mosip.kernel.biosdk.provider.util.ProviderConstants;
import io.mosip.kernel.core.bioapi.exception.BiometricException;
import io.mosip.kernel.core.bioapi.model.CompositeScore;
import io.mosip.kernel.core.bioapi.model.KeyValuePair;
import io.mosip.kernel.core.bioapi.model.QualityScore;
import io.mosip.kernel.core.bioapi.model.Score;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;

@Component
public class BioProviderImpl_V_0_7 implements iBioProviderApi {
	
	private static final Logger LOGGER = BioSDKProviderLoggerFactory.getLogger(BioProviderImpl_V_0_7.class);
	
	private static final String METHOD_NAME_KEY = "_METHOD_NAME";
	private static final String THRESHOLD_KEY = "_THRESHOLD";	
	private static final String API_VERSION = "0.7";
	
	private Map<BiometricType, Object> sdkRegistry = new HashMap<>();
	private Map<BiometricType, String> thresholds = new HashMap<>();
	

	@Override
	public Map<BiometricType, List<BiometricFunction>> init(Map<BiometricType, Map<String, String>> params)
			throws BiometricException {
		for(BiometricType modality : params.keySet()) {
			Map<String, String> modalityParams = params.get(modality);
			
			//check if version matches supported API version of this provider
			if(modalityParams != null && !modalityParams.isEmpty() 
					&& API_VERSION.equals(modalityParams.get(ProviderConstants.VERSION))) {				
				Object instance = BioProviderUtil.getSDKInstance(modalityParams);
				addToRegistry(instance, modality);
				thresholds.put(modality, modalityParams.getOrDefault(ProviderConstants.THRESHOLD, "60"));
			}
		}
		return getSupportedModalities();
	}

	/*
	 * compositeMatch --> is intended to be used for match on multiple modalities
	 * NOte: compositeMatch should not be used on multiple segments of same modality
	 */
	@Override
	public boolean verify(List<BIR> sample, List<BIR> record, BiometricType modality, Map<String, String> flags) {
		LOGGER.info(ProviderConstants.LOGGER_SESSIONID, ProviderConstants.LOGGER_IDTYPE, "verify invoked", 
				"modality >>> " + modality);
		
		if(Objects.isNull(flags)) { flags = new HashMap<>(); }
		
		String methodName = flags.getOrDefault(METHOD_NAME_KEY, "match");
		String threshold = flags.getOrDefault(THRESHOLD_KEY, thresholds.getOrDefault(modality, "60"));
		
		sample = sample.stream().filter(obj -> modality == BiometricType.fromValue(obj.getBdbInfo().getType().get(0).value()))
				.collect(Collectors.toList());
		
		record = record.stream().filter(obj -> modality == BiometricType.fromValue(obj.getBdbInfo().getType().get(0).value()))
				.collect(Collectors.toList());
		
		switch (methodName) {
		case "match":	
			return getSDKMatchResult(sample, record.toArray(new BIR[record.size()]), modality, flags, threshold);			

		case "compositeMatch":			
			return getSDKCompositeMatchResult(sample, record.toArray(new BIR[record.size()]), modality, flags, threshold);
		}
		
		return false;
	}

	@Override
	public Map<String, Boolean> identify(List<BIR> sample, Map<String, List<BIR>> gallery, BiometricType modality,
			Map<String, String> flags) {
		LOGGER.info(ProviderConstants.LOGGER_SESSIONID, ProviderConstants.LOGGER_IDTYPE, "identify invoked", 
				"modality >>> " + modality);
		
		if(Objects.isNull(flags)) { flags = new HashMap<>(); }
		
		String methodName = flags.getOrDefault(METHOD_NAME_KEY, "compositeMatch");
		String threshold = flags.getOrDefault(THRESHOLD_KEY, thresholds.getOrDefault(modality, "60"));
		
		sample = sample.stream().filter(obj -> modality == BiometricType.fromValue(obj.getBdbInfo().getType().get(0).value()))
				.collect(Collectors.toList());		 
		
		Map<String , Boolean> result = new HashMap<>();
		for(Entry<String, List<BIR>> entry : gallery.entrySet()) {
			
			if(Objects.nonNull(entry.getValue())) {
				
				List<BIR> record = entry.getValue().stream().filter(obj -> modality == BiometricType.fromValue(obj.getBdbInfo().getType().get(0).value()))
						.collect(Collectors.toList());
				
				switch (methodName) {
				case "match":	
					result.put(entry.getKey(), getSDKMatchResult(sample, record.toArray(new BIR[record.size()]),
							modality, flags, threshold));
					break;			

				case "compositeMatch":			
					result.put(entry.getKey(), getSDKCompositeMatchResult(sample, record.toArray(new BIR[record.size()]), 
							modality, flags, threshold));
					break;
				}
			}			
		}		
		return result;
	}

	//QualityScore checkQuality(BIR sample, KeyValuePair[] flags)	
	@Override
	public float[] getSegmentQuality(BIR[] sample, Map<String, String> flags) {
		float[] scores = new float[sample.length];		
		for(int i =0; i< sample.length; i++) {			
			BiometricType modality = BiometricType.fromValue(sample[i].getBdbInfo().getType().get(0).value());
			Method method = ReflectionUtils.findRequiredMethod(this.sdkRegistry.get(modality).getClass(), 
					"checkQuality", BIR.class, KeyValuePair[].class);
			method.setAccessible(true);

			if(Objects.nonNull(method)) {
				try {
					Object response =  method.invoke(this.sdkRegistry.get(modality), sample[i], getKeyValuePairs(flags));
					if(Objects.nonNull(response)) {
						QualityScore  qualityScore = (QualityScore) response;
						scores[i] = qualityScore.getInternalScore();
					}
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					LOGGER.error(ProviderConstants.LOGGER_SESSIONID, ProviderConstants.LOGGER_IDTYPE, 
							"getSegmentQuality invoked", ExceptionUtils.getStackTrace(e));
				}
			}
			else
				scores[i] = 0;
		}
		
		return scores;
	}

	//QualityScore checkQuality(BIR sample, KeyValuePair[] flags)	
	@Override
	public Map<BiometricType, Float> getModalityQuality(BIR[] sample, Map<String, String> flags) {			
		Map<BiometricType, LongStream.Builder> result = new HashMap<>();
		for(BIR bir : sample) {
			BiometricType modality = BiometricType.fromValue(bir.getBdbInfo().getType().get(0).value());
			Method method = ReflectionUtils.findRequiredMethod(this.sdkRegistry.get(modality).getClass(), 
					"checkQuality", BIR.class, KeyValuePair[].class);
			method.setAccessible(true);
			
			if(Objects.nonNull(method)) {
				try {
					Object response =  method.invoke(this.sdkRegistry.get(modality), bir, getKeyValuePairs(flags));
					if(Objects.nonNull(response)) {
						QualityScore  qualityScore = (QualityScore) response;
						result.computeIfAbsent(modality, k -> LongStream.builder()).add(qualityScore.getInternalScore());
					}
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					LOGGER.error(ProviderConstants.LOGGER_SESSIONID, ProviderConstants.LOGGER_IDTYPE, 
							"getModalityQuality invoked", ExceptionUtils.getStackTrace(e));
				}
			}
		}
		
		Map<BiometricType, Float> finalResult = new HashMap<BiometricType, Float>();
		result.forEach((k , v) ->  { 
			OptionalDouble avg = v.build().average();
			if(avg.isPresent())
				finalResult.put(k, (float) avg.getAsDouble());
		}); 
		return finalResult;
	}

	//BIR extractTemplate(BIR paramBIR, KeyValuePair[] paramArrayOfKeyValuePair)
	@Override
	public List<BIR> extractTemplate(List<BIR> sample, Map<String, String> flags) {		
		List<BIR> extracts = new ArrayList<>();
		for(BIR bir : sample) {
			BiometricType modality = BiometricType.fromValue(bir.getBdbInfo().getType().get(0).value());
			Method method = ReflectionUtils.findRequiredMethod(this.sdkRegistry.get(modality).getClass(), 
					"extractTemplate", BIR.class, KeyValuePair[].class);
			method.setAccessible(true);
			
			if(Objects.nonNull(method)) {
				try {
					Object response =  method.invoke(this.sdkRegistry.get(modality), bir, getKeyValuePairs(flags));
					extracts.add(Objects.nonNull(response) ? (BIR) response : null);
					
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					LOGGER.error(ProviderConstants.LOGGER_SESSIONID, ProviderConstants.LOGGER_IDTYPE, 
							"extractTemplate invoked", ExceptionUtils.getStackTrace(e));
				}
			}
		}		
		return extracts;
	}

	//Score[] match(BIR sample, BIR[] gallery, KeyValuePair[] flags)
	private boolean getSDKMatchResult(List<BIR> sample, BIR[] record, BiometricType modality, Map<String, String> flags, 
			String threshold) {
			
		Method method = ReflectionUtils.findRequiredMethod(this.sdkRegistry.get(modality).getClass(), "match", 
				BIR.class, BIR[].class, KeyValuePair[].class);
		method.setAccessible(true);
		
		boolean isMatched = false;
		//TODO check for duplicate segment in sample. will SDK handle it or should this be handled in provider ?
		
		if(Objects.nonNull(method)) {
			LOGGER.debug(ProviderConstants.LOGGER_SESSIONID, ProviderConstants.LOGGER_IDTYPE, "verify invoked", "Match method found");		
			 
			LongStream.Builder scaleScores = LongStream.builder();
			for(int i=0;i<sample.size();i++) {					
				try {
					Object[] response = (Object[]) method.invoke(this.sdkRegistry.get(modality), sample.get(i), 
							record, getKeyValuePairs(flags));
					
					if( Objects.nonNull(response) ) {
						Score[] scores = Arrays.copyOf(response, response.length, Score[].class);
						Optional<Score> result = Arrays.stream(scores)
								.max((s1, s2) ->  (int) (s1.getScaleScore() - s2.getScaleScore()));
						scaleScores.add(result.isPresent() ? (long) result.get().getScaleScore() : 0L);
					}
					
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					LOGGER.error(ProviderConstants.LOGGER_SESSIONID, ProviderConstants.LOGGER_IDTYPE, 
							"getSDKMatchResult invoked", ExceptionUtils.getStackTrace(e));
				}				
			}			
			OptionalDouble result = scaleScores.build().average();
			isMatched = ( result.isPresent() && result.getAsDouble() >= Float.valueOf(threshold) ) ? true : false;
		}
		return isMatched;
	}
	
	
	//CompositeScore compositeMatch(BIR[] sampleList, BIR[] recordList, KeyValuePair[] flags)
	private boolean getSDKCompositeMatchResult(List<BIR> sample, BIR[] record, BiometricType modality, Map<String, String> flags, 
			String threshold) {
		Method method = ReflectionUtils.findRequiredMethod(this.sdkRegistry.get(modality).getClass(), "compositeMatch", 
				BIR[].class, BIR[].class, KeyValuePair[].class);
		method.setAccessible(true);
		
		boolean isMatched = false;
		if(Objects.nonNull(method)) {
			LOGGER.debug(ProviderConstants.LOGGER_SESSIONID, ProviderConstants.LOGGER_IDTYPE, "verify invoked", "CompositeMatch method found");
			
			try {
				Object response = method.invoke(this.sdkRegistry.get(modality), sample.toArray(new BIR[sample.size()]), 
						record, getKeyValuePairs(flags));
				
				if( Objects.nonNull(response) ) {
					CompositeScore  compositeScore = (CompositeScore) response;
					if(compositeScore.getScaledScore() >= Float.valueOf(threshold))
						isMatched = true;
				}
				
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				LOGGER.error(ProviderConstants.LOGGER_SESSIONID, ProviderConstants.LOGGER_IDTYPE, 
						"getSDKCompositeMatchResult invoked", ExceptionUtils.getStackTrace(e));
			}
		}
		return isMatched;
	}
	
	
	
	private Map<BiometricType, List<BiometricFunction>> getSupportedModalities() {
		Map<BiometricType, List<BiometricFunction>> result = new HashMap<>();
		sdkRegistry.forEach((modality, map) -> {
			result.put(modality, Arrays.asList(BiometricFunction.values()));
		});
		return result;
	}
	
	private void addToRegistry(Object sdkInstance, BiometricType modality) {
		sdkRegistry.put(modality, sdkInstance);
	}
	
		
	private KeyValuePair[] getKeyValuePairs(Map<String, String> flags) {
		if(flags == null)
			return null;
		
		flags.remove(METHOD_NAME_KEY);
		flags.remove(THRESHOLD_KEY);
		
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
