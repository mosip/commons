package io.mosip.kernel.bioextractor.service.helper;

import static io.mosip.kernel.bioextractor.config.constant.BiometricExtractionErrorConstants.TECHNICAL_ERROR;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.kernel.bioextractor.exception.BiometricExtractionException;
import io.mosip.kernel.biometrics.constant.BiometricFunction;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biosdk.provider.factory.BioAPIFactory;
import io.mosip.kernel.biosdk.provider.spi.iBioProviderApi;
import io.mosip.kernel.core.cbeffutil.entity.BIR;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.BIRType;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.SingleType;
import io.mosip.kernel.core.cbeffutil.spi.CbeffUtil;

@Component
public class BioExtractionHelper {
	
	@Autowired
	private CbeffUtil cbeffUtil;
	
	@Autowired
	private BioAPIFactory bioApiFactory;
	
	public byte[] extractTemplates(byte[] cbeffContent) throws BiometricExtractionException {
		try {
			List<BIRType> birDataFromXML = cbeffUtil.getBIRDataFromXML(cbeffContent);
			List<BIR> birs = cbeffUtil.convertBIRTypeToBIR(birDataFromXML);
			Map<SingleType, List<BIR>> birsByType = birs.stream().collect(Collectors.groupingBy(bir -> bir.getBdbInfo().getType().get(0)));
			
			List<BIR> allExtractedTemplates =  new ArrayList<>();
			
			for (Entry<SingleType,List<BIR>> entry : birsByType.entrySet()) {
				SingleType modality = entry.getKey();
				iBioProviderApi bioProvider = bioApiFactory.getBioProvider(BiometricType.fromValue(modality.value()),
						BiometricFunction.EXTRACT);
				Map<String, String> flags = new LinkedHashMap<>();
				List<BIR> extractedTemplates = bioProvider.extractTemplate(entry.getValue(), flags);
				allExtractedTemplates.addAll(extractedTemplates);
			}
			
			return cbeffUtil.createXML(allExtractedTemplates);
			
		} catch (Exception e) {
			throw new BiometricExtractionException(TECHNICAL_ERROR, e);
		}
	}
	

}
