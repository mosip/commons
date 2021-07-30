package io.mosip.kernel.biometrics.commons;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.Entry;
import io.mosip.kernel.core.exception.BiometricSignatureValidationException;
import io.mosip.kernel.core.util.CryptoUtil;

public class BiometricsSignatureHelper {

	public static String extractJWTToken(BIR bir) throws BiometricSignatureValidationException, JSONException {

		String constructedJWTToken = null;
		String sb = new String(bir.getSb(), StandardCharsets.UTF_8);
		String bdb = Base64.getUrlEncoder().encodeToString(bir.getBdb());

		for (Entry entry : bir.getOthers()) {
			if (entry.getKey().equals("PAYLOAD")) {
				String value = entry.getValue().replace("<bioValue>", bdb);
				String encodedPayloadValue = CryptoUtil.encodeBase64(value.getBytes());
				constructedJWTToken = constructJWTToken(sb, encodedPayloadValue);
				JSONObject jsonObject = new JSONObject(value);
				String digitalID = jsonObject.getString("digitalId").split("\\.")[0];
				compareJWTForSameCertificates(constructedJWTToken, digitalID);
			}
		}
		return constructedJWTToken;

	}

	private static void compareJWTForSameCertificates(String constructedJWTToken, String digitalID)
			throws JSONException, BiometricSignatureValidationException {
		String digitalIdHeader = new String(CryptoUtil.decodeBase64(digitalID));
		JSONObject digitalHeaderCertificate = new JSONObject(digitalIdHeader);
		JSONArray digitalHeadercertificateJsonArray = digitalHeaderCertificate.getJSONArray("x5c");
		ArrayList<String> digitalCertificates = new ArrayList<String>();
		if (digitalHeadercertificateJsonArray != null) {
			for (int i = 0; i < digitalHeadercertificateJsonArray.length(); i++) {
				digitalCertificates.add(digitalHeadercertificateJsonArray.getString(i));
			}
		}

		String sbHeader = new String(CryptoUtil.decodeBase64(constructedJWTToken.split("\\.")[0]));
		JSONObject sbHeaderCertificate = new JSONObject(sbHeader);
		JSONArray sbHeadercertificateJsonArray = sbHeaderCertificate.getJSONArray("x5c");
		ArrayList<String> sbCertificates = new ArrayList<String>();
		if (sbHeadercertificateJsonArray != null) {
			for (int i = 0; i < sbHeadercertificateJsonArray.length(); i++) {
				sbCertificates.add(sbHeadercertificateJsonArray.getString(i));
			}
		}

		if (!digitalCertificates.containsAll(sbCertificates)) {
			throw new BiometricSignatureValidationException("Header Certificate mismatch");
		}
	}

	private static String constructJWTToken(String sb, String encodedPayloadValue) {
		return sb.replace("..", "." + encodedPayloadValue + ".");
	}

}
