package io.mosip.kernel.uingenerator.generator;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;

import java.util.List;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.kernel.uingenerator.entity.UinEntity;

/**
 * This class have functionality to persists the list of uins in database
 * 
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
@Component
public class UinWriter {

	/**
	 * The Logger instance
	 */
	// private static final Logger LOGGER =
	// LoggerFactory.getLogger(UinWriter.class);

	/**
	 * Interface used to interact with the persistence context.
	 */
	@Autowired
	private EntityManager entityManager;

	/**
	 * The main runtime interface between a Java application and Hibernate.
	 */
	private Session session;

	/**
	 * Persist a uin in database. If that uin already exists than rollback
	 * 
	 * @param item the item
	 */
	public void persistUin(UinEntity item) {
		Session currentSession = getSession();
		if (!currentSession.getTransaction().isActive()) {
			currentSession.getTransaction().begin();
		}
		currentSession.save(item);
		try {
			currentSession.flush();
			currentSession.getTransaction().commit();
		} catch (PersistenceException e) {
			currentSession.getTransaction().rollback();
		} finally {
			currentSession.clear();
		}
	}

	/**
	 * Function to set {@link #session} from {@link #entityManager}
	 */
	public void setSession() {
		entityManager = entityManager.getEntityManagerFactory().createEntityManager();
		this.session = entityManager.unwrap(Session.class);
	}

	/**
	 * Function to get session
	 * 
	 * @return {@link #session}
	 */
	private Session getSession() {
		if (session == null) {
			setSession();
		}
		return session;
	}

	public int saveBatch(List<UinEntity> batch) {
		int successCount = 0;
		EntityManager em = entityManager.getEntityManagerFactory().createEntityManager();
		Session localSession = em.unwrap(Session.class);

		try {
			localSession.beginTransaction();
			for (int i = 0; i < batch.size(); i++) {
				localSession.persist(batch.get(i));
				successCount++;

				// Flush and clear periodically to avoid memory overhead
				if (i % 50 == 0) {
					localSession.flush();
					localSession.clear();
				}
			}
			localSession.getTransaction().commit();
		} catch (Exception e) {
			if (localSession.getTransaction().isActive()) {
				localSession.getTransaction().rollback();
			}
			throw e;
		} finally {
			localSession.close();
		}
		return successCount;
	}

	public void closeSession() {
		if (session != null) {
			session.clear();
		}
	}
}