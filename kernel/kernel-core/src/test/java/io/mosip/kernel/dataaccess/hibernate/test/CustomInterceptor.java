package io.mosip.kernel.dataaccess.hibernate.test;

import org.hibernate.Interceptor;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Bal Vikash Sharma
 *
 */
@Component
public class CustomInterceptor implements Interceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomInterceptor.class);

	@Override
	public void onDelete(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {
		LOGGER.info("Delete call for : {}", entity.getClass().getSimpleName());
	}

	@Override
	public boolean onFlushDirty(Object entity, Object id, Object[] currentState, Object[] previousState,
			String[] propertyNames, Type[] types) {
		LOGGER.info("Update call for : {}", entity.getClass().getSimpleName());
		return false;
	}

	@Override
	public boolean onLoad(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {
		LOGGER.info("Get call for : {}", entity.getClass().getSimpleName());
		return false;
	}

	@Override
	public boolean onSave(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {
		LOGGER.info("Create call for : {}", entity.getClass().getSimpleName());
		return false;
	}

}
