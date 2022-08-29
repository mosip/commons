package io.mosip.kernel.biosdk.provider.test.dto;



import io.mosip.kernel.core.bioapi.model.KeyValuePair;
import io.mosip.kernel.core.bioapi.model.MatchDecision;
import io.mosip.kernel.core.bioapi.model.QualityScore;
import io.mosip.kernel.core.bioapi.model.Response;
import io.mosip.kernel.core.bioapi.spi.IBioApi;
import io.mosip.kernel.core.cbeffutil.entity.BIR;

public class SDKInstanceOne0_8 implements IBioApi {


	@Override
	public Response<QualityScore> checkQuality(BIR sample, KeyValuePair[] flags) {
		 Response<QualityScore> response = new Response<>();
    	 QualityScore qualityScore= new QualityScore();
    	 qualityScore.setScore(90.0F);
    	 response.setResponse(qualityScore);
    	 response.setStatusCode(210);
    	 return response;
	}

	@Override
	public Response<MatchDecision[]> match(BIR sample, BIR[] gallery, KeyValuePair[] flags) {
		Response<MatchDecision[]> response = new Response<>();
        MatchDecision matchDecision = new MatchDecision();
        matchDecision.setMatch(true);
        matchDecision.setAnalyticsInfo(flags);
        MatchDecision[] matchDecisions = new MatchDecision[1];
        matchDecisions[0]=matchDecision;
        response.setStatusCode(210);
        response.setResponse(matchDecisions);
        return response;
	}

	@Override
	public Response<BIR> extractTemplate(BIR sample, KeyValuePair[] flags) {
		Response<BIR> response = new Response<>();
   	 	response.setStatusCode(210);
        response.setResponse(sample);
        return response;
	}

	@Override
	public Response<BIR[]> segment(BIR sample, KeyValuePair[] flags) {
		// TODO Auto-generated method stub
		return null;
	}

    
}
