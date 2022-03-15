package io.mosip.kernel.biosdk.provider.test.dto;

import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.core.bioapi.model.KeyValuePair;
import io.mosip.kernel.core.bioapi.model.MatchDecision;
import io.mosip.kernel.core.bioapi.model.QualityScore;
import io.mosip.kernel.core.bioapi.model.Response;
import io.mosip.kernel.core.bioapi.model.Score;


public class SDKInstanceThree0_7 {


	
	public QualityScore checkQuality(BIR sample, KeyValuePair[] flags) {
		 QualityScore qualityScore= new QualityScore();
    	 qualityScore.setScore(90.0F);
    	 qualityScore.setAnalyticsInfo(flags);
    	 qualityScore.setInternalScore(90);
    	 return qualityScore;
	}


	public Score[] match(BIR sample, BIR[] gallery, KeyValuePair[] flags) {
	    Score[] scores = new Score[1];
        Score score = new Score();
        score.setAnalyticsInfo(flags);
        score.setInternalScore(60);
        score.setScaleScore(60.0F);
        scores[0]=score;
        return scores;
	}



	public BIR extractTemplate(BIR sample, KeyValuePair[] flags) {
		return sample;
	}


	public Response<BIR[]> segment(BIR sample, KeyValuePair[] flags) {
		// TODO Auto-generated method stub
		return null;
	}
}
