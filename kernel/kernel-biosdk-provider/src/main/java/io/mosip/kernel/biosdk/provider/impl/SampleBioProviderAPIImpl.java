package io.mosip.kernel.biosdk.provider.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Component;

import io.mosip.kernel.biometrics.constant.BiometricFunction;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.Match;
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
import io.mosip.kernel.core.cbeffutil.entity.BDBInfo;
import io.mosip.kernel.core.cbeffutil.entity.BIR;
import io.mosip.kernel.core.cbeffutil.entity.BIRInfo;
import io.mosip.kernel.core.cbeffutil.entity.BIRVersion;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.ProcessedLevelType;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.PurposeType;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.QualityType;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.RegistryIDType;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.SingleType;


@Component
public class SampleBioProviderAPIImpl implements iBioProviderApi {
	
	private static final String API_VERSION = "0.9";
	private Map<BiometricType, Map<BiometricFunction, IBioApi>> sdkRegistry = new HashMap<>();
	
	@Override	
	public Map<BiometricType, List<BiometricFunction>> init(Map<BiometricType, Map<String, String>> params) 
			throws BiometricException {			
		for(BiometricType modality : params.keySet()) {
			Map<String, String> modalityParams = params.get(modality);
			
			//check if version matches supported API version of this provider
			if(modalityParams != null && !modalityParams.isEmpty() 
					&& API_VERSION.equals(modalityParams.get(ProviderConstants.VERSION))) {
				
				IBioApi iBioApi = (IBioApi) BioProviderUtil.getSDKInstance(modalityParams);
				SDKInfo sdkInfo = iBioApi.init(modalityParams);
				
				//cross check loaded SDK version and configured SDK version
				if(!API_VERSION.equals(sdkInfo.getApiVersion()))
					throw new BiometricException(ErrorCode.INVALID_SDK_VERSION.getErrorCode(), 
							ErrorCode.INVALID_SDK_VERSION.getErrorCode());
				
				addToRegistry(sdkInfo, iBioApi, modality);			
			}
		}
		return getSupportedModalities();		
	}

	@Override
	public boolean verify(List<BIR> sample, List<BIR> record, BiometricType modality, Map<String, String> flags) {
		BiometricRecord galleryRecord = getBiometricRecord(record.toArray(new BIR[record.size()]));
		Response<MatchDecision[]> response = sdkRegistry.get(modality).get(BiometricFunction.MATCH)
				.match(getBiometricRecord(sample.toArray(new BIR[sample.size()])), new BiometricRecord[] {galleryRecord}, 
						Arrays.asList(modality), flags);
		
		if(isSuccessResponse(response)) {
			Map<BiometricType, Decision> decisions = response.getResponse()[0].getDecisions();
			if(decisions.containsKey(modality)) {
				Match matchResult = decisions.get(modality).getMatch();
				//TODO log analyticsinfo and errors
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
		int i=0;
		for(String key : gallery.keySet()) {
			keyIndexMapping.put(key, i);
			galleryRecords[i++] = getBiometricRecord(gallery.get(key).toArray(new BIR[gallery.get(key).size()]));
		}
		
		Response<MatchDecision[]> response = sdkRegistry.get(modality).get(BiometricFunction.MATCH)
				.match(getBiometricRecord(sample.toArray(new BIR[sample.size()])), galleryRecords, 
						Arrays.asList(modality), flags);
		
		Map<String, Boolean> result = new HashMap<>();
		if(isSuccessResponse(response)) {
			keyIndexMapping.forEach((key, index) -> {
				if(response.getResponse()[index].getDecisions().containsKey(modality)) {
					result.put(key, Match.MATCHED.equals(response.getResponse()[index].getDecisions().get(modality).getMatch()));
					//TODO log analyticsinfo and errors
				}
				else
					result.put(key, false);
			});
		}
		return result;
	}

	@Override
	public float[] getSegmentQuality(BIR[] sample, Map<String, String> flags) {		
		float scores[] = new float[sample.length];
		for(int i=0; i< sample.length; i++) {
			BiometricType modality = BiometricType.fromValue(sample[i].getBdbInfo().getType().get(0).value());
			Response<QualityCheck> response = sdkRegistry.get(modality).get(BiometricFunction.QUALITY_CHECK)
					.checkQuality(getBiometricRecord(sample[i]), Arrays.asList(modality), flags);
			
			if(isSuccessResponse(response) && response.getResponse().getScores() != null) {
				scores[i] = response.getResponse().getScores().containsKey(modality) ? 
						response.getResponse().getScores().get(modality).getScore() : 0;
				//TODO log analyticsInfo && errors
			}
			else
				scores[i] = 0;
		}
		return scores;
	}
	
	
	@Override
	public Map<BiometricType, Float> getModalityQuality(BIR[] sample, Map<String, String> flags) {		
		Set<BiometricType> modalitites = new HashSet<>();
		for(int i=0; i< sample.length; i++) {
			modalitites.add(BiometricType.fromValue(sample[i].getBdbInfo().getType().get(0).value()));
		}
		
		Map<BiometricType, Float> scoreMap = new HashMap<>();
		for(BiometricType modality : modalitites) {			
			Response<QualityCheck> response = sdkRegistry.get(modality).get(BiometricFunction.QUALITY_CHECK)
					.checkQuality(getBiometricRecord(sample), Arrays.asList(modality), flags);
			
			if(isSuccessResponse(response) && response.getResponse().getScores() != null) 
				scoreMap.put(modality, response.getResponse().getScores().containsKey(modality) ? 
						response.getResponse().getScores().get(modality).getScore() : 0);
			//TODO log analyticsInfo && errors
		}
		
		float scores[] = new float[sample.length];
		for(int i=0; i< sample.length; i++) {
			BiometricType modality = BiometricType.fromValue(sample[i].getBdbInfo().getType().get(0).value());
			if(scoreMap.containsKey(modality))
				scores[i] = scoreMap.get(modality);
			else
				scores[i] = 0;
		}
		return scoreMap;
	}

	@Override
	public List<BIR> extractTemplate(List<BIR> sample, Map<String, String> flags) {
		List<BIR> templates = new LinkedList<>();
		for(BIR bir : sample) {
			Response<BiometricRecord> response = sdkRegistry.get(BiometricType.fromValue(bir.getBdbInfo().getType().get(0).value()))
				.get(BiometricFunction.EXTRACT).extractTemplate(getBiometricRecord(bir), flags);
			
			templates.add( isSuccessResponse(response) ? 
						convertToBIR(response.getResponse().getSegments().get(0)) :	null);
		}
		return templates;
	}
	
	private boolean isSuccessResponse(Response<?> response) {
		if(response != null && response.getStatusCode() >= 200 
				&& response.getStatusCode() <= 299 && response.getResponse() != null)
			return true;
		return false;
	}
	
	private void addToRegistry(SDKInfo sdkInfo, IBioApi iBioApi, BiometricType modality) {
		for(BiometricFunction biometricFunction : sdkInfo.getSupportedMethods().keySet()) {
			if(sdkInfo.getSupportedMethods().get(biometricFunction).contains(modality)) {
				if(sdkRegistry.get(modality) == null)
					sdkRegistry.put(modality, new HashMap<>());
				
				sdkRegistry.get(modality).put(biometricFunction, iBioApi);
			}				
		}
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
	
	private io.mosip.kernel.biometrics.entities.BIR convertToBiometricRecordBIR(BIR bir) {
		List<BiometricType> bioTypes = new ArrayList<>();
		for(SingleType type : bir.getBdbInfo().getType()) {
			bioTypes.add(BiometricType.fromValue(type.value()));
		}	
		
		io.mosip.kernel.biometrics.entities.RegistryIDType format = new io.mosip.kernel.biometrics.entities.RegistryIDType(bir.getBdbInfo().getFormat().getOrganization(),
				bir.getBdbInfo().getFormat().getType());
		
		io.mosip.kernel.biometrics.constant.QualityType qualityType;
		
		if(Objects.nonNull(bir.getBdbInfo().getQuality())) {
			io.mosip.kernel.biometrics.entities.RegistryIDType birAlgorithm = new io.mosip.kernel.biometrics.entities.RegistryIDType(
					bir.getBdbInfo().getQuality().getAlgorithm().getOrganization(),
					bir.getBdbInfo().getQuality().getAlgorithm().getType());
			
			qualityType = new io.mosip.kernel.biometrics.constant.QualityType();
			qualityType.setAlgorithm(birAlgorithm);
			qualityType.setQualityCalculationFailed(bir.getBdbInfo().getQuality().getQualityCalculationFailed());
			qualityType.setScore(bir.getBdbInfo().getQuality().getScore());
			
		} else {
			qualityType = null;
		}
		
		io.mosip.kernel.biometrics.entities.VersionType version;
		if(Objects.nonNull(bir.getVersion())) {
			version = new io.mosip.kernel.biometrics.entities.VersionType(bir.getVersion().getMajor(), 
					bir.getVersion().getMinor());
		} else {
			version = null;
		}
		
		io.mosip.kernel.biometrics.entities.VersionType cbeffversion;
		if(Objects.nonNull(bir.getCbeffversion())) {
			cbeffversion = new io.mosip.kernel.biometrics.entities.VersionType(bir.getCbeffversion().getMajor(),
					bir.getCbeffversion().getMinor());
		} else {
			cbeffversion = null;
		}
		
		io.mosip.kernel.biometrics.constant.PurposeType purposeType;
		if(Objects.nonNull(bir.getBdbInfo().getPurpose())) {
			purposeType = io.mosip.kernel.biometrics.constant.PurposeType.fromValue(bir.getBdbInfo().getPurpose().name());
		} else {
			purposeType = null;
		}
		
		io.mosip.kernel.biometrics.constant.ProcessedLevelType processedLevelType;
		if(Objects.nonNull(bir.getBdbInfo().getLevel())) {
			processedLevelType = io.mosip.kernel.biometrics.constant.ProcessedLevelType.fromValue(
					bir.getBdbInfo().getLevel().name());
		} else{
			processedLevelType = null;
		}
		
		return new io.mosip.kernel.biometrics.entities.BIR.BIRBuilder()
					.withBdb(bir.getBdb())
					.withVersion(version)
					.withCbeffversion(cbeffversion)
					.withBirInfo(new io.mosip.kernel.biometrics.entities.BIRInfo.BIRInfoBuilder().withIntegrity(true).build())
					.withBdbInfo(new io.mosip.kernel.biometrics.entities.BDBInfo.BDBInfoBuilder()
							.withFormat(format)
							.withType(bioTypes)
							.withQuality(qualityType)
							.withCreationDate(bir.getBdbInfo().getCreationDate())
							.withIndex(bir.getBdbInfo().getIndex())
							.withPurpose(purposeType)
							.withLevel(processedLevelType)
							.withSubtype(bir.getBdbInfo().getSubtype()).build()).build();
	}
	
	private BIR convertToBIR(io.mosip.kernel.biometrics.entities.BIR bir) {
		List<SingleType> bioTypes = new ArrayList<>();
		for(BiometricType type : bir.getBdbInfo().getType()) {
			bioTypes.add(SingleType.fromValue(type.value()));
		}
		
		RegistryIDType format = new RegistryIDType();
		format.setOrganization(bir.getBdbInfo().getFormat().getOrganization());
		format.setType(bir.getBdbInfo().getFormat().getType());
		
		RegistryIDType birAlgorithm = new RegistryIDType();
		birAlgorithm.setOrganization(bir.getBdbInfo().getQuality().getAlgorithm().getOrganization());
		birAlgorithm.setType(bir.getBdbInfo().getQuality().getAlgorithm().getType());
		
		QualityType qualityType = new QualityType();
		qualityType.setAlgorithm(birAlgorithm);
		qualityType.setQualityCalculationFailed(bir.getBdbInfo().getQuality().getQualityCalculationFailed());
		qualityType.setScore(bir.getBdbInfo().getQuality().getScore());
		
		return new BIR.BIRBuilder()
				.withBdb(bir.getBdb())
				.withVersion(new BIRVersion.BIRVersionBuilder()
							.withMinor(bir.getVersion().getMinor())
							.withMajor(bir.getVersion().getMajor()).build())
				.withCbeffversion(new BIRVersion.BIRVersionBuilder()
							.withMinor(bir.getCbeffversion().getMinor())
							.withMajor(bir.getCbeffversion().getMajor()).build())
				.withBirInfo(new BIRInfo.BIRInfoBuilder().withIntegrity(true).build())
				.withBdbInfo(new BDBInfo.BDBInfoBuilder()
						.withFormat(format)
						.withType(bioTypes)
						.withQuality(qualityType)
						.withCreationDate(bir.getBdbInfo().getCreationDate())
						.withIndex(bir.getBdbInfo().getIndex())
						.withPurpose(PurposeType.fromValue(bir.getBdbInfo().getPurpose().name()))
						.withLevel(ProcessedLevelType.fromValue(bir.getBdbInfo().getLevel().name()))
						.withSubtype(bir.getBdbInfo().getSubtype()).build()).build();
	}
	
	//TODO - set cebffversion and version in biometricRecord
	private BiometricRecord getBiometricRecord(BIR[] birs) {
		BiometricRecord biometricRecord = new BiometricRecord();
		biometricRecord.setSegments(new LinkedList<>());
		for(int i=0; i< birs.length; i++) {
			biometricRecord.getSegments().add(convertToBiometricRecordBIR(birs[i]));
		}
		return biometricRecord;		
	}
	
	//TODO - set cebffversion and version in biometricRecord
	private BiometricRecord getBiometricRecord(BIR bir) {
		BiometricRecord biometricRecord = new BiometricRecord();
		biometricRecord.getSegments().add(convertToBiometricRecordBIR(bir));
		return biometricRecord;
	}

}
