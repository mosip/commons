package io.mosip.kernel.biometrics.commons;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.core.exception.BiometricSignatureValidationException;
import io.mosip.kernel.core.util.CryptoUtil;

public class BiometricsSignatureHelper {

	public static String extractJWTToken(BIR bir) throws BiometricSignatureValidationException, JSONException {

		String constructedJWTToken = null;

		Map<String, String> othersInfo = bir.getOthers();
		if (othersInfo == null || othersInfo.isEmpty()) {
			throw new BiometricSignatureValidationException("Others value is null / empty inside BIR");
		}

		String sb = new String(bir.getSb(), StandardCharsets.UTF_8);
		String bdb = CryptoUtil.encodeToURLSafeBase64(bir.getBdb());

		for (Map.Entry<String, String> entry : othersInfo.entrySet()) {
			if (entry.getKey().equals("PAYLOAD")) {
				String value = entry.getValue().replace("<bioValue>", bdb);
				String encodedPayloadValue = CryptoUtil.encodeToURLSafeBase64(value.getBytes());
				constructedJWTToken = constructJWTToken(sb, encodedPayloadValue);
				JSONObject jsonObject = new JSONObject(value);
				String digitalID = jsonObject.getString("digitalId");
				compareJWTForSameCertificates(constructedJWTToken, digitalID);
			}
		}
		return constructedJWTToken;

	}

	private static void compareJWTForSameCertificates(String jwtString1, String jwtString2)
			throws JSONException, BiometricSignatureValidationException {
		String jwtString1Header = new String(CryptoUtil.decodeURLSafeBase64(jwtString1.split("\\.")[0]));
		JSONObject jwtString1HeaderCertificate = new JSONObject(jwtString1Header);
		JSONArray jwtString1HeadercertificateJsonArray = jwtString1HeaderCertificate.getJSONArray("x5c");
		ArrayList<String> jwtString1Certificates = new ArrayList<String>();
		if (jwtString1HeadercertificateJsonArray != null) {
			for (int i = 0; i < jwtString1HeadercertificateJsonArray.length(); i++) {
				jwtString1Certificates.add(jwtString1HeadercertificateJsonArray.getString(i));
			}
		}

		String jwtString2Header = new String(CryptoUtil.decodeURLSafeBase64(jwtString2.split("\\.")[0]));
		JSONObject jwtString2HeaderCertificate = new JSONObject(jwtString2Header);
		JSONArray jwtString2HeadercertificateJsonArray = jwtString2HeaderCertificate.getJSONArray("x5c");
		ArrayList<String> jwtString2Certificates = new ArrayList<String>();
		if (jwtString2HeadercertificateJsonArray != null) {
			for (int i = 0; i < jwtString2HeadercertificateJsonArray.length(); i++) {
				jwtString2Certificates.add(jwtString2HeadercertificateJsonArray.getString(i));
			}
		}

		if (!jwtString1Certificates.containsAll(jwtString2Certificates)) {
			throw new BiometricSignatureValidationException("Header Certificate mismatch");
		}
	}

	private static String constructJWTToken(String sb, String encodedPayloadValue) {
		return sb.replace("..", "." + encodedPayloadValue + ".");
	}
}
