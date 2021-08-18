package io.mosip.kernel.biosdk.provider.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.mosip.kernel.biosdk.provider.util.BioSDKProviderLoggerFactory;
import io.mosip.kernel.core.logger.spi.Logger;
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
import io.mosip.kernel.biometrics.model.SDKInfo;
import io.mosip.kernel.biometrics.spi.IBioApi;
import io.mosip.kernel.biosdk.provider.spi.iBioProviderApi;
import io.mosip.kernel.biosdk.provider.util.BioProviderUtil;
import io.mosip.kernel.biosdk.provider.util.ErrorCode;
import io.mosip.kernel.biosdk.provider.util.ProviderConstants;
import io.mosip.kernel.core.bioapi.exception.BiometricException;

@Component
public class BioProviderImpl_V_0_9 implements iBioProviderApi {

	private static final Logger logger = BioSDKProviderLoggerFactory.getLogger(BioProviderImpl_V_0_9.class);

	private static final String API_VERSION = "0.9";
	private final Map<BiometricType, Map<BiometricFunction, IBioApi>> sdkRegistry = new HashMap<>();

	@Counted(value = "sdk.count", extraTags = {"api_version", API_VERSION})
	@Timed(value = "sdk.time", extraTags = {"api_version", API_VERSION})
	@Override
	public Map<BiometricType, List<BiometricFunction>> init(Map<BiometricType, Map<String, String>> params)
			throws BiometricException {
		for (BiometricType modality : params.keySet()) {
			Map<String, String> modalityParams = params.get(modality);

			// check if version matches supported API version of this provider
			if (modalityParams != null && !modalityParams.isEmpty()
					&& API_VERSION.equals(modalityParams.get(ProviderConstants.VERSION))) {

				IBioApi iBioApi = (IBioApi) BioProviderUtil.getSDKInstance(modalityParams);
				SDKInfo sdkInfo = iBioApi.init(modalityParams);

				// cross check loaded SDK version and configured SDK version
				if (!API_VERSION.equals(sdkInfo.getApiVersion()))
					throw new BiometricException(ErrorCode.INVALID_SDK_VERSION.getErrorCode(),
							ErrorCode.INVALID_SDK_VERSION.getErrorCode());

				addToRegistry(sdkInfo, iBioApi, modality);
			}
		}
		return getSupportedModalities();
	}

	@Counted(value = "sdk.count", extraTags = {"api_version", API_VERSION})
	@Timed(value = "sdk.time", extraTags = {"api_version", API_VERSION})
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
				logger.info("AnalyticsInfo : {}, errors : {}", decisions.get(modality).getAnalyticsInfo(),
						decisions.get(modality).getErrors());
				return Match.MATCHED.equals(matchResult);
			}
		}

		return false;
	}

	@Counted(value = "sdk.count", extraTags = {"api_version", API_VERSION})
	@Timed(value = "sdk.time", extraTags = {"api_version", API_VERSION})
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
					logger.info("AnalyticsInfo : {}, errors : {}",
							response.getResponse()[index].getDecisions().get(modality).getAnalyticsInfo(),
							response.getResponse()[index].getDecisions().get(modality).getErrors());
				} else
					result.put(key, false);
			});
		}
		return result;
	}

	@Counted(value = "sdk.count", extraTags = {"api_version", API_VERSION})
	@Timed(value = "sdk.time", extraTags = {"api_version", API_VERSION})
	@Override
	public float[] getSegmentQuality(BIR[] sample, Map<String, String> flags) {
		float scores[] = new float[sample.length];
		for (int i = 0; i < sample.length; i++) {
			BiometricType modality = BiometricType.fromValue(sample[i].getBdbInfo().getType().get(0).value());
			Response<QualityCheck> response = sdkRegistry.get(modality).get(BiometricFunction.QUALITY_CHECK)
					.checkQuality(getBiometricRecord(sample[i]), Arrays.asList(modality), flags);

			if (isSuccessResponse(response) && response.getResponse().getScores() != null &&
					response.getResponse().getScores().containsKey(modality)) {
				scores[i] = response.getResponse().getScores().get(modality).getScore();
				logger.info("AnalyticsInfo : {}, errors : {}",
						response.getResponse().getScores().get(modality).getAnalyticsInfo(),
						response.getResponse().getScores().get(modality).getErrors());
			} else
				scores[i] = 0;
		}
		return scores;
	}

	@Counted(value = "sdk.count", extraTags = {"api_version", API_VERSION})
	@Timed(value = "sdk.time", extraTags = {"api_version", API_VERSION})
	@Override
	public Map<BiometricType, Float> getModalityQuality(BIR[] sample, Map<String, String> flags) {
		Set<BiometricType> modalitites = new HashSet<>();
		for (int i = 0; i < sample.length; i++) {
			modalitites.add(BiometricType.fromValue(sample[i].getBdbInfo().getType().get(0).value()));
		}

		Map<BiometricType, Float> scoreMap = new HashMap<>();
		for (BiometricType modality : modalitites) {
			Response<QualityCheck> response = sdkRegistry.get(modality).get(BiometricFunction.QUALITY_CHECK)
					.checkQuality(getBiometricRecord(sample), Arrays.asList(modality), flags);

			if (isSuccessResponse(response) && response.getResponse().getScores() != null &&
					response.getResponse().getScores().containsKey(modality)) {
				scoreMap.put(modality,response.getResponse().getScores().get(modality).getScore());
				logger.info("AnalyticsInfo : {}, errors : {}",
						response.getResponse().getScores().get(modality).getAnalyticsInfo(),
						response.getResponse().getScores().get(modality).getErrors());
			}
			else {	scoreMap.put(modality, 0f); }
		}

		float scores[] = new float[sample.length];
		for (int i = 0; i < sample.length; i++) {
			BiometricType modality = BiometricType.fromValue(sample[i].getBdbInfo().getType().get(0).value());
			if (scoreMap.containsKey(modality))
				scores[i] = scoreMap.get(modality);
			else
				scores[i] = 0;
		}
		return scoreMap;
	}

	@Counted(value = "sdk.count", extraTags = {"api_version", API_VERSION})
	@Timed(value = "sdk.time", extraTags = {"api_version", API_VERSION})
	@Override
	public List<BIR> extractTemplate(List<BIR> sample, Map<String, String> flags) {
		Map<BiometricType, List<BIR>> birsByModality = sample.stream().collect(Collectors.groupingBy(bir -> BiometricType.fromValue(bir
				.getBdbInfo()
				.getType()
				.get(0).value())));

		List<BIR> templates = birsByModality.entrySet().stream()
				.<BIR>flatMap(entry -> {
					BiometricType modality = entry.getKey();
					List<BIR> birsForModality = entry.getValue();

					BiometricRecord sampleRecord = getBiometricRecord(birsForModality.toArray(new BIR[birsForModality.size()]));

					Response<BiometricRecord> response = sdkRegistry
							.get(modality)
							.get(BiometricFunction.EXTRACT).extractTemplate(sampleRecord, null, flags);

					if(isSuccessResponse(response)) {
						return response.getResponse().getSegments().stream();
					}

					return Stream.empty();
				})
				.collect(Collectors.toList());

		return templates;
	}

	private boolean isSuccessResponse(Response<?> response) {
		if (response != null && response.getStatusCode() >= 200 && response.getStatusCode() <= 299
				&& response.getResponse() != null)
			return true;
		return false;
	}

	private void addToRegistry(SDKInfo sdkInfo, IBioApi iBioApi, BiometricType modality) {
		for (BiometricFunction biometricFunction : sdkInfo.getSupportedMethods().keySet()) {
			if (sdkInfo.getSupportedMethods().get(biometricFunction).contains(modality)) {
				if (sdkRegistry.get(modality) == null)
					sdkRegistry.put(modality, new HashMap<>());

				sdkRegistry.get(modality).put(biometricFunction, iBioApi);
			}
			logger.info("Successfully registered SDK : {}, BiometricFunction: {}",
					sdkInfo.getProductOwner().getOrganization(), biometricFunction);
		}
	}

	private Map<BiometricType, List<BiometricFunction>> getSupportedModalities() {
		Map<BiometricType, List<BiometricFunction>> result = new HashMap<>();
		sdkRegistry.forEach((modality, map) -> {
			if (result.get(modality) == null)
				result.put(modality, new ArrayList<BiometricFunction>());

			result.get(modality).addAll(map.keySet());
		});
		return result;
	}

	// TODO - set cebffversion and version in biometricRecord
	private BiometricRecord getBiometricRecord(BIR[] birs) {
		BiometricRecord biometricRecord = new BiometricRecord();
		biometricRecord.setSegments(Arrays.asList(birs));
		return biometricRecord;
	}

	// TODO - set cebffversion and version in biometricRecord
	private BiometricRecord getBiometricRecord(BIR bir) {
		BiometricRecord biometricRecord = new BiometricRecord();
		biometricRecord.getSegments().add(bir);
		return biometricRecord;
	}

}