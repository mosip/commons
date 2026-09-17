package io.mosip.kernel.dataaccess.hibernate.repository.impl;

import java.util.List;
import java.util.Map;

import jakarta.persistence.CacheStoreMode;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

import org.hibernate.HibernateException;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.kernel.dataaccess.hibernate.constant.HibernateErrorCode;
import io.mosip.kernel.dataaccess.hibernate.constant.HibernatePersistenceConstant;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaQuery;

/**
 * Hibernate implementation of the {@link BaseRepository} interface.
 * 
 * @author Dharmesh Khandelwal
 * @author Shashank Agrawal
 * @since 1.0.0
 * 
 * @param <E> the type of the entity to handle
 * @param <T> the type of the entity's identifier
 */
@Transactional
@SuppressWarnings({ "unchecked", "rawtypes" })
public class HibernateRepositoryImpl<E, T> extends SimpleJpaRepository<E, T> implements BaseRepository<E, T> {

	/**
	 * Field for interface used to interact with the persistence context.
	 */
	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * Constructor for HibernateRepositoryImpl having JpaEntityInformation and
	 * EntityManager
	 * 
	 * @param entityInformation The entityInformation
	 * @param entityManager     The entityManager
	 */
	public HibernateRepositoryImpl(JpaEntityInformation<E, ?> entityInformation, EntityManager entityManager) {
		super(entityInformation, entityManager);
		this.entityManager = entityManager;
	}

	/**
	 * Persists {@code entity} and flushes the persistence context.
	 *
	 * @param entity entity to insert
	 * @return the persisted entity
	 * @throws io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException on Hibernate or other runtime failure
	 */
	@Override
	public E create(E entity) {
		try {
			entityManager.persist(entity);
			entityManager.flush();
		} catch (HibernateException hibernateException) {
			throw new DataAccessLayerException(HibernateErrorCode.HIBERNATE_EXCEPTION.getErrorCode(),
					hibernateException.getMessage(), hibernateException);
		} catch (RuntimeException runtimeException) {
			throw new DataAccessLayerException(HibernateErrorCode.ERR_DATABASE.getErrorCode(),
					runtimeException.getMessage(), runtimeException);
		}
		return entity;
	}

	/**
	 * Merges {@code entity} and flushes the persistence context.
	 *
	 * @param entity entity to update
	 * @return the merged entity
	 * @throws io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException on Hibernate or other runtime failure
	 */
	@Override
	public E update(E entity) {
		try {
			entity = entityManager.merge(entity);
			entityManager.flush();
		} catch (HibernateException hibernateException) {
			throw new DataAccessLayerException(HibernateErrorCode.HIBERNATE_EXCEPTION.getErrorCode(),
					hibernateException.getMessage(), hibernateException);
		} catch (RuntimeException runtimeException) {
			throw new DataAccessLayerException(HibernateErrorCode.ERR_DATABASE.getErrorCode(),
					runtimeException.getMessage(), runtimeException);
		}
		return entity;
	}

	/**
	 * Loads an entity by identifier, or {@code null} if none exists.
	 *
	 * @param entityClass entity type
	 * @param id          primary key
	 * @return the entity, or {@code null} if not found
	 * @throws io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException on Hibernate or other runtime failure
	 */
	@Override
	public E findById(Class<E> entityClass, T id) {
		try {
			return entityManager.find(entityClass, id);
		} catch (HibernateException hibernateException) {
			throw new DataAccessLayerException(HibernateErrorCode.HIBERNATE_EXCEPTION.getErrorCode(),
					hibernateException.getMessage(), hibernateException);
		} catch (RuntimeException runtimeException) {
			throw new DataAccessLayerException(HibernateErrorCode.ERR_DATABASE.getErrorCode(),
					runtimeException.getMessage(), runtimeException);
		}
	}

	/**
	 * Returns all rows of {@code entityClass}, refreshing the query cache.
	 *
	 * @param entityClass entity type
	 * @return all matching entities (empty if none)
	 * @throws io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException on Hibernate or other runtime failure
	 */
	@Override
	public List<E> findAll(Class<E> entityClass) {
		List<E> entityList = null;
		try {
			CriteriaQuery criteriaQuery = entityManager.getCriteriaBuilder().createQuery();
			criteriaQuery.select(criteriaQuery.from(entityClass));
			Query query = entityManager.createQuery(criteriaQuery);
			query.setHint(HibernatePersistenceConstant.CACHE_QUERY_PROPERTY, CacheStoreMode.REFRESH);
			entityList = query.getResultList();
		} catch (HibernateException hibernateException) {
			throw new DataAccessLayerException(HibernateErrorCode.HIBERNATE_EXCEPTION.getErrorCode(),
					hibernateException.getMessage(), hibernateException);
		} catch (RuntimeException runtimeException) {
			throw new DataAccessLayerException(HibernateErrorCode.ERR_DATABASE.getErrorCode(),
					runtimeException.getMessage(), runtimeException);
		}
		return entityList;
	}

	/**
	 * Loads and removes the entity identified by {@code id}.
	 *
	 * @param entityClass entity type
	 * @param id          primary key of the row to delete
	 * @return {@code id} of the removed entity
	 * @throws io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException on Hibernate or other runtime failure
	 */
	@Override
	public T delete(Class<E> entityClass, T id) {
		try {
			E entityToBeDeleted = findById(entityClass, id);
			entityManager.remove(entityToBeDeleted);
			return id;
		} catch (HibernateException hibernateException) {
			throw new DataAccessLayerException(HibernateErrorCode.HIBERNATE_EXCEPTION.getErrorCode(),
					hibernateException.getMessage(), hibernateException);
		} catch (RuntimeException runtimeException) {
			throw new DataAccessLayerException(HibernateErrorCode.ERR_DATABASE.getErrorCode(),
					runtimeException.getMessage(), runtimeException);
		}
	}

	/**
	 * Executes a JPQL select with optional named parameters.
	 *
	 * @param qlString JPQL select
	 * @param params   named parameters; {@code null} means none
	 * @return result list (empty if none)
	 * @throws io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException if no result is expected, or on Hibernate / runtime failure
	 */
	@Override
	public List<E> createQuerySelect(String qlString, Map<String, Object> params) {
		List<E> entityList = null;
		try {
			Query query = entityManager.createQuery(qlString);
			query.setHint(HibernatePersistenceConstant.CACHE_QUERY_PROPERTY, CacheStoreMode.REFRESH);
			if (params != null) {
				params.forEach(query::setParameter);
			}
			entityList = query.getResultList();
		} catch (NoResultException noResultException) {
			throw new DataAccessLayerException(HibernateErrorCode.NO_RESULT_EXCEPTION.getErrorCode(),
					noResultException.getMessage(), noResultException);
		} catch (HibernateException hibernateException) {
			throw new DataAccessLayerException(HibernateErrorCode.HIBERNATE_EXCEPTION.getErrorCode(),
					hibernateException.getMessage(), hibernateException);
		} catch (RuntimeException runtimeException) {
			throw new DataAccessLayerException(HibernateErrorCode.ERR_DATABASE.getErrorCode(),
					runtimeException.getMessage(), runtimeException);
		}
		return entityList;
	}

	/**
	 * Executes a JPQL select with optional named parameters and a maximum result count.
	 *
	 * @param qlString JPQL select
	 * @param params   named parameters; {@code null} means none
	 * @param limit    maximum rows to return
	 * @return result list (empty if none)
	 * @throws io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException if no result is expected, or on Hibernate / runtime failure
	 */
	@Override
	public List<E> createQuerySelect(String qlString, Map<String, Object> params, int limit) {
		List<E> entityList = null;
		try {
			Query query = entityManager.createQuery(qlString);
			query.setHint(HibernatePersistenceConstant.CACHE_QUERY_PROPERTY, CacheStoreMode.REFRESH);
			if (params != null) {
				params.forEach(query::setParameter);
			}
			query.setMaxResults(limit);
			entityList = query.getResultList();
		} catch (NoResultException noResultException) {
			throw new DataAccessLayerException(HibernateErrorCode.NO_RESULT_EXCEPTION.getErrorCode(),
					noResultException.getMessage(), noResultException);
		} catch (HibernateException hibernateException) {
			throw new DataAccessLayerException(HibernateErrorCode.HIBERNATE_EXCEPTION.getErrorCode(),
					hibernateException.getMessage(), hibernateException);
		} catch (RuntimeException runtimeException) {
			throw new DataAccessLayerException(HibernateErrorCode.ERR_DATABASE.getErrorCode(),
					runtimeException.getMessage(), runtimeException);
		}
		return entityList;
	}

	/**
	 * Executes a JPQL update or delete with optional named parameters.
	 *
	 * @param qlString JPQL update or delete
	 * @param params   named parameters; {@code null} means none
	 * @return number of entities updated or deleted
	 * @throws io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException if no result is expected, or on Hibernate / runtime failure
	 */
	@Override
	public int createQueryUpdateOrDelete(String qlString, Map<String, Object> params) {
		int entitiesCount;
		try {
			Query query = entityManager.createQuery(qlString);
			if (params != null) {
				params.forEach(query::setParameter);
			}
			entitiesCount = query.executeUpdate();
		} catch (NoResultException noResultException) {
			throw new DataAccessLayerException(HibernateErrorCode.NO_RESULT_EXCEPTION.getErrorCode(),
					noResultException.getMessage(), noResultException);
		} catch (HibernateException hibernateException) {
			throw new DataAccessLayerException(HibernateErrorCode.HIBERNATE_EXCEPTION.getErrorCode(),
					hibernateException.getMessage(), hibernateException);
		} catch (RuntimeException runtimeException) {
			throw new DataAccessLayerException(HibernateErrorCode.ERR_DATABASE.getErrorCode(),
					runtimeException.getMessage(), runtimeException);
		}
		return entitiesCount;
	}

	/**
	 * Executes a named JPQL select with optional named parameters.
	 *
	 * @param name        named query name
	 * @param entityClass result entity type
	 * @param params      named parameters; {@code null} means none
	 * @return result list (empty if none)
	 * @throws io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException if no result is expected, or on Hibernate / runtime failure
	 */
	@Override
	public List<E> createNamedQuerySelect(String name, Class<E> entityClass, Map<String, Object> params) {
		List<E> entityList = null;
		try {
			Query query = entityManager.createNamedQuery(name, entityClass);
			query.setHint(HibernatePersistenceConstant.CACHE_QUERY_PROPERTY, CacheStoreMode.REFRESH);
			if (params != null) {
				params.forEach(query::setParameter);
			}
			entityList = query.getResultList();
		} catch (NoResultException noResultException) {
			throw new DataAccessLayerException(HibernateErrorCode.NO_RESULT_EXCEPTION.getErrorCode(),
					noResultException.getMessage(), noResultException);
		} catch (HibernateException hibernateException) {
			throw new DataAccessLayerException(HibernateErrorCode.HIBERNATE_EXCEPTION.getErrorCode(),
					hibernateException.getMessage(), hibernateException);
		} catch (RuntimeException runtimeException) {
			throw new DataAccessLayerException(HibernateErrorCode.ERR_DATABASE.getErrorCode(),
					runtimeException.getMessage(), runtimeException);
		}
		return entityList;
	}

	/**
	 * Executes a named JPQL update or delete with optional named parameters.
	 *
	 * @param name        named query name
	 * @param entityClass entity type of the named query
	 * @param params      named parameters; {@code null} means none
	 * @return number of entities updated or deleted
	 * @throws io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException if no result is expected, or on Hibernate / runtime failure
	 */
	@Override
	public int createNamedQueryUpdateOrDelete(String name, Class<E> entityClass, Map<String, Object> params) {
		int entitiesCount;
		try {
			Query query = entityManager.createNamedQuery(name, entityClass);
			if (params != null) {
				params.forEach(query::setParameter);
			}
			entitiesCount = query.executeUpdate();
		} catch (NoResultException noResultException) {
			throw new DataAccessLayerException(HibernateErrorCode.NO_RESULT_EXCEPTION.getErrorCode(),
					noResultException.getMessage(), noResultException);
		} catch (HibernateException hibernateException) {
			throw new DataAccessLayerException(HibernateErrorCode.HIBERNATE_EXCEPTION.getErrorCode(),
					hibernateException.getMessage(), hibernateException);
		} catch (RuntimeException runtimeException) {
			throw new DataAccessLayerException(HibernateErrorCode.ERR_DATABASE.getErrorCode(),
					runtimeException.getMessage(), runtimeException);
		}
		return entitiesCount;
	}
}
