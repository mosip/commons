package io.mosip.commons.packet.util;

import io.mosip.commons.packet.constants.PacketManagerConstants;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.cbeffutil.container.impl.CbeffContainerImpl;
import io.mosip.kernel.core.cbeffutil.common.CbeffValidator;
import io.mosip.kernel.core.cbeffutil.entity.BIR;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.BIRType;
import io.mosip.kernel.core.util.HMACUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Component
public class PacketManagerHelper {
	
	//@Autowired
	//private CbeffImpl xmlBuilder;
	
	public byte[] getXMLData(BiometricRecord biometricRecord) throws Exception {
		byte[] xmlBytes = null;
		try(InputStream xsd = new FileInputStream(new File("C:\\Users\\M1045447\\Desktop\\cbeff.xsd"))) {//getClass().getClassLoader().getResourceAsStream(PacketManagerConstants.CBEFF_SCHEMA_FILE_PATH)) {
			CbeffContainerImpl cbeffContainer = new CbeffContainerImpl();
			BIRType bir = cbeffContainer.createBIRType(biometricRecord.getSegments());
			xmlBytes =  CbeffValidator.createXMLBytes(bir, IOUtils.toByteArray(xsd));
		}
		return xmlBytes;
	}
	
	public byte[] generateHash(List<String> order, Map<String, byte[]> data) {
		if(order != null && !order.isEmpty()) {
			for(String name : order) {
				HMACUtils.update(data.get(name));
			}			
			return HMACUtils.digestAsPlainText(HMACUtils.updatedHash()).getBytes();
		}
		return null;
	}
}
