package io.mosip.kernel.dataaccess.hibernate.config;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Interceptor;
import org.hibernate.type.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

/**
 * Hibernate interceptor that encrypts and decrypts fields marked {@link Encrypted}.
 * <p>
 * {@link #onSave} posts to {@code mosip.kernel.encrypt.url}; {@link #onLoad}
 * posts to {@code mosip.kerenl.decrypt.url}. The keymanager call in
 * {@link #doSaveOrloadAction} is currently commented out, so both callbacks
 * return {@code true} without changing state.
 * </p>
 *
 * @see Encrypted
 */
public class EncryptionInterceptor implements Interceptor {

	/** Keymanager encrypt endpoint from {@code mosip.kernel.encrypt.url}. */
	@Value("${mosip.kernel.encrypt.url:http://localhost:8088/v1/keymanager/encrypt}")
	String encryptUrl;

	/** Keymanager decrypt endpoint from {@code mosip.kerenl.decrypt.url}. */
	@Value("${mosip.kerenl.decrypt.url:http://localhost:8088/v1/keymanager/decrypt}")
	String decryptUrl;

	/**
	 * HTTP client used to call keymanager encrypt and decrypt APIs.
	 */

	@Autowired
	RestTemplate restTemplate;

	private List<String> reqParams;

	/**
	 * Encrypts {@link Encrypted} properties before insert.
	 *
	 * @param entity        entity being saved
	 * @param id            entity identifier
	 * @param state         current property values (may be mutated)
	 * @param propertyNames persistent property names aligned with {@code state}
	 * @param types         Hibernate types aligned with {@code state}
	 * @return {@code true} if {@code state} was modified
	 */
	@Override
	public boolean onSave(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {
		reqParams = new ArrayList<>();
		return doSaveOrloadAction(entity, state, propertyNames, types, encryptUrl);
	}

	/**
	 * Decrypts {@link Encrypted} properties after load.
	 *
	 * @param entity        entity being loaded
	 * @param id            entity identifier
	 * @param state         current property values (may be mutated)
	 * @param propertyNames persistent property names aligned with {@code state}
	 * @param types         Hibernate types aligned with {@code state}
	 * @return {@code true} if {@code state} was modified
	 */
	@Override
	public boolean onLoad(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {
		reqParams = new ArrayList<>();
		return doSaveOrloadAction(entity, state, propertyNames, types, decryptUrl);
	}

	/**
	 * Walks {@link Encrypted} fields and would call keymanager at {@code url}.
	 * Currently a no-op that always returns {@code true}.
	 *
	 * @param entity        entity being saved or loaded
	 * @param state         property values (would be replaced with cipher/plain text)
	 * @param propertyNames persistent property names aligned with {@code state}
	 * @param types         Hibernate types aligned with {@code state}
	 * @param url           keymanager encrypt or decrypt URL
	 * @return {@code true} (state is not modified while the call is commented out)
	 */
	private boolean doSaveOrloadAction(Object entity, Object[] state, String[] propertyNames, Type[] types,
			String url) {
//		try {
//			reqParams = new ArrayList<>();
//
//			UriComponentsBuilder regbuilder = UriComponentsBuilder.fromHttpUrl(url);
//			HttpHeaders headers = new HttpHeaders();
//			Map<String, Object> params = new HashMap<>();
//			headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
//			HttpEntity<RequestWrapper<String>> en = new HttpEntity<>(headers);
//			String uriBuilder = regbuilder.build().encode().toUriString();
//
//			Field[] fields = entity.getClass().getDeclaredFields();
//			for (Field field : fields) {
//				if (field.isAnnotationPresent(Encrypted.class)) {
//					System.out.println("field name  " + field.getName());
//					reqParams.add(field.getName());
//
//				}
//			}
//			for (int i = 0; i < propertyNames.length; i++) {
//				if (reqParams.contains(propertyNames[i])) {
//					System.out.println("Value " + state[i]);
//					uriBuilder += "/{data}";
//					params.put("data", state[i]);
//					ResponseEntity<ResponseWrapper<String>> responseEntity = restTemplate.exchange(uriBuilder,
//							HttpMethod.GET, en, new ParameterizedTypeReference<ResponseWrapper<String>>() {
//							}, params);
//					if (responseEntity.getBody().getErrors() != null
//							&& !responseEntity.getBody().getErrors().isEmpty()) {
//						// error
//					}
//					state[i] = responseEntity.getBody().getResponse();
//				}
//			}
//
//			return true;
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		return true;
	}
}
