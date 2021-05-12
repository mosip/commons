package io.mosip.kernel.bioapi.impl;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;

import io.mosip.kernel.core.bioapi.model.KeyValuePair;
import io.mosip.kernel.core.bioapi.model.MatchDecision;
import io.mosip.kernel.core.bioapi.model.QualityScore;
import io.mosip.kernel.core.bioapi.model.Response;
import io.mosip.kernel.core.bioapi.spi.IBioApi;
import io.mosip.kernel.core.cbeffutil.entity.BDBInfo;
import io.mosip.kernel.core.cbeffutil.entity.BIR;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.QualityType;

/**
 * The Class BioApiImpl.
 * 
 * @author Sanjay Murali
 * @author Manoj SP
 * 
 */
@Component
public class BioApiImpl implements IBioApi {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.bioapi.spi.IBioApi#checkQuality(io.mosip.kernel.core.
	 * bioapi.model.BIR, io.mosip.kernel.core.bioapi.model.KeyValuePair[])
	 */
	@Override
	public Response<QualityScore> checkQuality(BIR sample, KeyValuePair[] flags) {
		QualityScore qualityScore = new QualityScore();
		int major = Optional.ofNullable(sample.getBdbInfo()).map(BDBInfo::getQuality).map(QualityType::getScore)
				.orElse(0L).intValue();
		qualityScore.setScore(major);
		Response<QualityScore> response = new Response<>();
		response.setStatusCode(200);
		response.setResponse(qualityScore);
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.bioapi.spi.IBioApi#match(io.mosip.kernel.core.bioapi.
	 * model.BIR, io.mosip.kernel.core.bioapi.model.BIR[],
	 * io.mosip.kernel.core.bioapi.model.KeyValuePair[])
	 */
	@Override
	public Response<MatchDecision[]> match(BIR sample, BIR[] gallery, KeyValuePair[] flags) {
		MatchDecision matchingScore[] = new MatchDecision[gallery.length];
		int count = 0;
		for (BIR recordedValue : gallery) {
			matchingScore[count] = new MatchDecision();
			if (Objects.nonNull(recordedValue) && Objects.nonNull(recordedValue.getBdb())
					&& recordedValue.getBdb().length != 0 && Arrays.equals(recordedValue.getBdb(), sample.getBdb())) {
				matchingScore[count].setMatch(true);
			} else {
				matchingScore[count].setMatch(false);
			}
			count++;
		}
		Response<MatchDecision[]> response = new Response<>();
		response.setStatusCode(200);
		response.setResponse(matchingScore);
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.bioapi.spi.IBioApi#extractTemplate(io.mosip.kernel.core.
	 * bioapi.model.BIR, io.mosip.kernel.core.bioapi.model.KeyValuePair[])
	 */
	@Override
	public Response<BIR> extractTemplate(BIR sample, KeyValuePair[] flags) {
		Response<BIR> response = new Response<>();
		response.setStatusCode(200);
		response.setResponse(sample);
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.bioapi.spi.IBioApi#segment(io.mosip.kernel.core.bioapi.
	 * model.BIR, io.mosip.kernel.core.bioapi.model.KeyValuePair[])
	 */
	@Override
	public Response<BIR[]> segment(BIR sample, KeyValuePair[] flags) {
		BIR[] bir = new BIR[1];
		bir[0] = sample;
		Response<BIR[]> response = new Response<>();
		response.setStatusCode(200);
		response.setResponse(bir);
		return response;
	}

}