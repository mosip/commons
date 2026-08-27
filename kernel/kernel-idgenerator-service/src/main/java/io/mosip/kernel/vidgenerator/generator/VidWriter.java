package io.mosip.kernel.vidgenerator.generator;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.kernel.vidgenerator.entity.VidEntity;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Persists VID entities to the database using a dedicated Hibernate session
 * (outside Spring's transaction scope) so bulk generation from Vert.x worker
 * threads achieves the same JDBC batch throughput as UinWriter.
 */
@Component
public class VidWriter {

	private static final Logger LOGGER = LoggerFactory.getLogger(VidWriter.class);

	private static final int FLUSH_BATCH_SIZE = 500;

	@Autowired
	private EntityManager entityManager;

	private EntityManager dedicatedEntityManager;
	private Session session;

	public void setSession() {
		dedicatedEntityManager = entityManager.getEntityManagerFactory().createEntityManager();
		this.session = dedicatedEntityManager.unwrap(Session.class);
	}

	/**
	 * Persists a batch of VIDs in a single transaction.
	 * Flushes and clears every FLUSH_BATCH_SIZE rows to bound first-level cache memory.
	 * On failure, rolls back and falls back to per-row inserts.
	 */
	public void persistVidBatch(List<VidEntity> items) {
		if (items == null || items.isEmpty()) {
			return;
		}
		Session currentSession = getSession();
		currentSession.getTransaction().begin();
		try {
			for (int i = 0; i < items.size(); i++) {
				currentSession.persist(items.get(i));
				if ((i + 1) % FLUSH_BATCH_SIZE == 0) {
					currentSession.flush();
					currentSession.clear();
				}
			}
			currentSession.flush();
			currentSession.getTransaction().commit();
			currentSession.clear();
		} catch (PersistenceException e) {
			LOGGER.warn("Batch VID insert failed ({}), falling back to per-row inserts for this batch", e.getMessage());
			currentSession.getTransaction().rollback();
			currentSession.clear();
			persistPerRow(items, currentSession);
		}
	}

	private void persistPerRow(List<VidEntity> items, Session currentSession) {
		for (VidEntity item : items) {
			currentSession.getTransaction().begin();
			currentSession.persist(item);
			try {
				currentSession.flush();
				currentSession.getTransaction().commit();
			} catch (PersistenceException e) {
				currentSession.getTransaction().rollback();
			} finally {
				currentSession.clear();
			}
		}
	}

	private Session getSession() {
		if (session == null) {
			setSession();
		}
		return session;
	}

	public void closeSession() {
		try {
			if (session != null && session.isOpen()) {
				session.close();
			}
		} catch (Exception e) {
			LOGGER.warn("Error closing Hibernate session: {}", e.getMessage());
		} finally {
			session = null;
		}
		try {
			if (dedicatedEntityManager != null && dedicatedEntityManager.isOpen()) {
				dedicatedEntityManager.close();
			}
		} catch (Exception e) {
			LOGGER.warn("Error closing EntityManager: {}", e.getMessage());
		} finally {
			dedicatedEntityManager = null;
		}
	}
}
