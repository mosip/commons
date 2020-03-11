package io.mosip.kernel.masterdata.utils;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.dataaccess.hibernate.constant.HibernateErrorCode;
import io.mosip.kernel.masterdata.constant.MasterdataSearchErrorCode;
import io.mosip.kernel.masterdata.constant.OrderEnum;
import io.mosip.kernel.masterdata.dto.request.Pagination;
import io.mosip.kernel.masterdata.dto.request.SearchDto;
import io.mosip.kernel.masterdata.dto.request.SearchFilter;
import io.mosip.kernel.masterdata.dto.request.SearchSort;
import io.mosip.kernel.masterdata.entity.BaseEntity;
import io.mosip.kernel.masterdata.entity.Device;
import io.mosip.kernel.masterdata.entity.Machine;
import io.mosip.kernel.masterdata.entity.Zone;
import io.mosip.kernel.masterdata.exception.RequestException;
import io.mosip.kernel.masterdata.validator.FilterTypeEnum;

/**
 * Generating dynamic query for masterdata based on the search filters.
 * 
 * @author Abhishek Kumar
 * @since 1.0.0
 */
@Repository
@Transactional(readOnly = true)
public class MasterdataSearchHelper {

	@Value("${master.search.maximum.rows}")
	private int maximumRows;

	private static final String LANGCODE_COLUMN_NAME = "langCode";
	private static final String ENTITY_IS_NULL = "entity is null";
	private static final String WILD_CARD_CHARACTER = "%";
	private static final String TYPE_NAME = "typeName";
	private static final String DECOMISSION = "isDeleted";
	private static final String DEVICE_NAME = "deviceName";
	private static final String MAC_ADDRESS = "macAddress";
	private static final String SERIAL_NUMBER = "serialNum";
	private static final String MACHINE_SPEC_ID = "machineSpecId";
	private static final String DEVICE_SPEC_ID = "deviceSpecId";
	private static final String IS_ACTIVE_COLUMN_NAME = "isActive";

	/**
	 * Field for interface used to interact with the persistence context.
	 */
	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * Constructor for MasterdataSearchHelper having EntityManager
	 * 
	 * @param entityManager The entityManager
	 */
	public MasterdataSearchHelper(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	/**
	 * Method to search and sort the masterdata.
	 * 
	 * @param entity          the entity class for which search will be applied
	 * @param searchDto       which contains the list of filters, sort and
	 *                        pagination
	 * @param optionalFilters filters to be considered as 'or' statements
	 * 
	 * @return {@link Page} of entity
	 */
	public <E> Page<E> searchMasterdata(Class<E> entity, SearchDto searchDto, OptionalFilter[] optionalFilters) {
		long rows = 0l;
		List<E> result;
		Objects.requireNonNull(entity, ENTITY_IS_NULL);
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<E> selectQuery = criteriaBuilder.createQuery(entity);
		CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
		Pagination pagination = searchDto.getPagination();
		pagination.setPageFetch(pagination.getPageFetch() > maximumRows ? maximumRows : pagination.getPageFetch());
		searchDto.setPagination(pagination);
		// root Query
		Root<E> rootQuery = selectQuery.from(entity);
		// count query
		countQuery.select(criteriaBuilder.count(countQuery.from(entity)));
		// applying filters
		filterQuery(criteriaBuilder, rootQuery, selectQuery, countQuery, searchDto.getFilters(),
				searchDto.getLanguageCode(), optionalFilters);

		// applying sorting
		sortQuery(criteriaBuilder, rootQuery, selectQuery, searchDto.getSort());

		try {
			// creating executable query from select criteria query
			TypedQuery<E> executableQuery = entityManager.createQuery(selectQuery);
			// creating executable query from count criteria query
			TypedQuery<Long> countExecutableQuery = entityManager.createQuery(countQuery);
			// getting the rows count
			rows = countExecutableQuery.getSingleResult();
			// adding pagination
			paginationQuery(executableQuery, searchDto.getPagination());
			// executing query and returning data
			result = executableQuery.getResultList();
		} catch (HibernateException hibernateException) {
			throw new DataAccessLayerException(HibernateErrorCode.HIBERNATE_EXCEPTION.getErrorCode(),
					hibernateException.getMessage(), hibernateException);
		} catch (RequestException e) {
			throw e;
		} catch (RuntimeException runtimeException) {
			throw new DataAccessLayerException(HibernateErrorCode.ERR_DATABASE.getErrorCode(),
					runtimeException.getMessage(), runtimeException);
		}
		return new PageImpl<>(result,
				PageRequest.of(searchDto.getPagination().getPageStart(), searchDto.getPagination().getPageFetch()),
				rows);

	}

	/**
	 * Method to add the filters to the criteria query
	 * 
	 * @param builder     used to construct criteria queries
	 * @param root        root type in the from clause,always refers entity
	 * @param selectQuery criteria select query
	 * @param countQuery  criteria count query
	 * @param filters     list of {@link SearchFilter}
	 * @param langCode    language code if applicable
	 */
	private <E> void filterQuery(CriteriaBuilder builder, Root<E> root, CriteriaQuery<E> selectQuery,
			CriteriaQuery<Long> countQuery, List<SearchFilter> filters, String langCode,
			OptionalFilter[] optionalFilters) {
		final List<Predicate> predicates = new ArrayList<>();
		if (filters != null && !filters.isEmpty()) {
			filters.stream().filter(this::validateFilters).map(i -> buildFilters(builder, root, i))
					.filter(Objects::nonNull).collect(Collectors.toCollection(() -> predicates));
		}

		if (optionalFilters != null && optionalFilters.length != 0) {
			Arrays.stream(optionalFilters).forEach(i -> buildOptionalFilter(builder, root, i, predicates));

		}
		Predicate langCodePredicate = setLangCode(builder, root, langCode);
		if (langCodePredicate != null) {
			predicates.add(langCodePredicate);
		}
		Predicate isDeletedTrue = builder.equal(root.get(DECOMISSION), Boolean.FALSE);
		Predicate isDeletedNull = builder.isNull(root.get(DECOMISSION));
		Predicate isDeleted = builder.or(isDeletedTrue, isDeletedNull);
		predicates.add(isDeleted);
		if (!predicates.isEmpty()) {
			Predicate whereClause = builder.and(predicates.toArray(new Predicate[predicates.size()]));
			selectQuery.where(whereClause);
			countQuery.where(whereClause);
		}

	}

	private <E> void buildOptionalFilter(CriteriaBuilder builder, Root<E> root, final OptionalFilter optionalFilters,
			List<Predicate> predicates) {
		if (optionalFilters.getFilters() != null && !optionalFilters.getFilters().isEmpty()) {
			List<Predicate> optionalPredicates = optionalFilters.getFilters().stream().filter(this::validateFilters)
					.map(i -> buildFilters(builder, root, i)).filter(Objects::nonNull).collect(Collectors.toList());
			if (!optionalPredicates.isEmpty()) {
				Predicate orPredicate = builder
						.or(optionalPredicates.toArray(new Predicate[optionalPredicates.size()]));
				predicates.add(orPredicate);
			}
		}
	}

	/**
	 * Method to build {@link Predicate} out the {@link SearchFilter}
	 * 
	 * @param builder used to construct criteria queries
	 * @param root    root type in the from clause,always refers entity
	 * @param filter  search filter
	 * @return {@link Predicate}
	 */
	private <E> Predicate buildFilters(CriteriaBuilder builder, Root<E> root, SearchFilter filter) {
		String columnName = filter.getColumnName();
		String value = filter.getValue();
		String filterType = filter.getType();
		if (FilterTypeEnum.CONTAINS.name().equalsIgnoreCase(filterType)) {
			Expression<String> lowerCase = builder.lower(root.get(columnName));
			if (value.startsWith("*") && value.endsWith("*")) {
				String replacedValue = (value.substring(1)).substring(0, value.length() - 2);
				return builder.like(lowerCase,
						builder.lower(builder.literal(WILD_CARD_CHARACTER + replacedValue + WILD_CARD_CHARACTER)));
			} else if (value.startsWith("*")) {
				String replacedValue = value.substring(1);
				return builder.like(lowerCase, builder.lower(builder.literal(WILD_CARD_CHARACTER + replacedValue)));
			} else {
				return builder.like(lowerCase,
						builder.lower(builder.literal(WILD_CARD_CHARACTER + value + WILD_CARD_CHARACTER)));
			}
		}
		if (FilterTypeEnum.EQUALS.name().equalsIgnoreCase(filterType)) {
			return buildPredicate(builder, root, columnName, value);
		}
		if (FilterTypeEnum.STARTSWITH.name().equalsIgnoreCase(filterType)) {
			if (value.endsWith("*")) {
				value = value.substring(0, value.length() - 1);
			}
			Expression<String> lowerCase = builder.lower(root.get(columnName));
			return builder.like(lowerCase, builder.lower(builder.literal(value + WILD_CARD_CHARACTER)));
		}
		if (FilterTypeEnum.BETWEEN.name().equalsIgnoreCase(filterType)) {
			return setBetweenValue(builder, root, filter);
		}
		return null;
	}

	/**
	 * Method to add sorting statement in criteria query
	 * 
	 * @param builder       used to construct criteria query
	 * @param root          root type in the from clause,always refers entity
	 * @param criteriaQuery query in which sorting to be added
	 * @param sortFilter    by the query to be sorted
	 */
	private <E> void sortQuery(CriteriaBuilder builder, Root<E> root, CriteriaQuery<E> criteriaQuery,
			List<SearchSort> sortFilter) {
		if (sortFilter != null && !sortFilter.isEmpty()) {
			List<Order> orders = sortFilter.stream().filter(this::validateSort).map(i -> {
				Path<Object> path = null;
				try {
					path = root.get(i.getSortField());
				} catch (IllegalArgumentException | IllegalStateException e) {
					throw new RequestException(MasterdataSearchErrorCode.INVALID_SORT_FIELD.getErrorCode(), String
							.format(MasterdataSearchErrorCode.INVALID_SORT_FIELD.getErrorMessage(), i.getSortField()));
				}
				if (path != null) {
					if (OrderEnum.asc.name().equalsIgnoreCase(i.getSortType()))
						return builder.asc(root.get(i.getSortField()));
					else if (OrderEnum.desc.name().equalsIgnoreCase(i.getSortType()))
						return builder.desc(root.get(i.getSortField()));
					else {
						throw new RequestException(MasterdataSearchErrorCode.INVALID_SORT_TYPE.getErrorCode(),
								String.format(MasterdataSearchErrorCode.INVALID_SORT_TYPE.getErrorMessage(),
										i.getSortType()));
					}
				}
				return null;

			}).filter(Objects::nonNull).collect(Collectors.toList());
			criteriaQuery.orderBy(orders);
		}
	}

	/**
	 * Method to add pagination in criteria query
	 * 
	 * @param query to be added with pagination
	 * @param page  contains the pagination details
	 */
	private void paginationQuery(Query query, Pagination page) {
		if (page != null) {
			if (page.getPageStart() < 0 || page.getPageFetch() < 1) {
				throw new RequestException(MasterdataSearchErrorCode.INVALID_PAGINATION_VALUE.getErrorCode(),
						String.format(MasterdataSearchErrorCode.INVALID_PAGINATION_VALUE.getErrorMessage(),
								page.getPageStart(), page.getPageFetch()),
						null);
			} else {
				query.setFirstResult(page.getPageStart() * page.getPageFetch());
				query.setMaxResults(page.getPageFetch());
			}
		}
	}

	/**
	 * Method to add the Language Code in the criteria query
	 * 
	 * @param builder  used to construct the criteria query
	 * @param root     root type in the from clause,always refers entity
	 * @param langCode language code
	 * @return {@link Predicate}
	 */
	private <E> Predicate setLangCode(CriteriaBuilder builder, Root<E> root, String langCode) {
		if (langCode != null && !langCode.isEmpty() && !langCode.equalsIgnoreCase("all")) {
			Path<Object> langCodePath = root.get(LANGCODE_COLUMN_NAME);
			if (langCodePath != null) {
				return builder.equal(langCodePath, langCode);
			}
		}
		return null;
	}

	/**
	 * Method to handle type safe between {@link Predicate}
	 * 
	 * @param builder use to construct the criteria query
	 * @param root    type in the from clause,always refers entity
	 * @param filter  search filter with the between type.
	 * @return {@link Predicate}
	 */
	private <E> Predicate setBetweenValue(CriteriaBuilder builder, Root<E> root, SearchFilter filter) {
		try {
			String columnName = filter.getColumnName();
			Path<Object> path = root.get(columnName);
			Class<? extends Object> type = path.getJavaType();
			String fieldType = type.getTypeName();
			String toValue = filter.getToValue();
			String fromValue = filter.getFromValue();
			if (LocalDateTime.class.getName().equals(fieldType)) {
				return builder.between(root.get(columnName), DateUtils.parseToLocalDateTime(fromValue),
						DateUtils.convertUTCToLocalDateTime(toValue));
			}
			if (LocalDate.class.getName().equals(fieldType)) {
				return builder.between(root.get(columnName), LocalDate.parse(fromValue), LocalDate.parse(toValue));
			}
			if (Long.class.getName().equals(fieldType)) {
				return builder.between(root.get(columnName), Long.parseLong(fromValue), Long.parseLong(toValue));
			}
			if (Integer.class.getName().equals(fieldType)) {
				return builder.between(root.get(columnName), Integer.parseInt(fromValue), Integer.parseInt(toValue));
			}
			if (Float.class.getName().equals(fieldType)) {
				return builder.between(root.get(columnName), Float.parseFloat(fromValue), Float.parseFloat(toValue));
			}
			if (Double.class.getName().equals(fieldType)) {
				return builder.between(root.get(columnName), Double.parseDouble(fromValue),
						Double.parseDouble(toValue));
			}
			if (String.class.getName().equals(fieldType)) {
				return builder.between(root.get(columnName), fromValue, toValue);
			}
		} catch (IllegalArgumentException | IllegalStateException | InvalidDataAccessApiUsageException e) {
			throw new RequestException(MasterdataSearchErrorCode.INVALID_COLUMN.getErrorCode(),
					String.format(MasterdataSearchErrorCode.INVALID_COLUMN.getErrorMessage(), filter.getColumnName()),
					null);
		}
		return null;
	}

	/**
	 * Method to cast the data into the column type data type
	 * 
	 * @param root   type in the from clause,always refers entity
	 * @param column name of the column
	 * @param value  value to be cast based on the column data type
	 * @return the value
	 */
	private <E> Object parseDataType(Root<E> root, String column, String value) {
		Path<Object> path = root.get(column);
		if (path != null) {
			Class<? extends Object> type = path.getJavaType();
			String fieldType = type.getTypeName();
			if (LocalDateTime.class.getName().equals(fieldType)) {
				return DateUtils.parseToLocalDateTime(value);
			}
			if (LocalDate.class.getName().equals(fieldType)) {
				return LocalDate.parse(value);
			}
			if (Long.class.getName().equals(fieldType)) {
				return Long.parseLong(value);
			}
			if (Integer.class.getName().equals(fieldType)) {
				return Integer.parseInt(value);
			}
			if (Float.class.getName().equals(fieldType)) {
				return Float.parseFloat(value);
			}
			if (Double.class.getName().equals(fieldType)) {
				return Double.parseDouble(value);
			}
			if (Boolean.class.getName().equals(fieldType)) {
				return Boolean.valueOf(value);
			}
			if (Short.class.getName().equals(fieldType)) {
				return Short.valueOf(value);
			}
		}
		return value;
	}

	/**
	 * Method to create the predicate
	 * 
	 * @param builder used to construct criteria query
	 * @param root    type in the from clause,always refers entity
	 * @param column  name of the column
	 * @param value   column value
	 * @return {@link Predicate}
	 */
	private <E> Predicate buildPredicate(CriteriaBuilder builder, Root<E> root, String column, String value) {
		Predicate predicate = null;
		Path<Object> path = root.get(column);
		if (path != null) {
			Class<? extends Object> type = path.getJavaType();
			String fieldType = type.getTypeName();
			if (LocalDateTime.class.getName().equals(fieldType)) {
				LocalDateTime start = DateUtils.parseToLocalDateTime(value);
				predicate = builder.between(root.get(column), start, start.plusNanos(1000000l));
			} else if (String.class.getName().equals(fieldType)) {
				predicate = builder.equal(builder.lower(root.get(column)), builder.lower(builder.literal(value)));
			} else {
				predicate = builder.equal(root.get(column), parseDataType(root, column, value));
			}
		}
		return predicate;
	}

	/**
	 * Validate the filter column and values
	 * 
	 * @param filter search filter to be validated
	 * @return true if valid false otherwise
	 */
	private boolean validateFilters(SearchFilter filter) {
		if (filter != null) {
			if (filter.getColumnName() != null && !filter.getColumnName().trim().isEmpty()) {
				return FilterTypes(filter);
			} else {
				throw new RequestException(MasterdataSearchErrorCode.MISSING_FILTER_COLUMN.getErrorCode(),
						MasterdataSearchErrorCode.MISSING_FILTER_COLUMN.getErrorMessage());
			}
		}
		return false;
	}

	private boolean FilterTypes(SearchFilter filter) {
		if (filter.getType() != null && !filter.getType().trim().isEmpty()) {
			if (validateFilter(filter)) {
				return true;
			}
		} else {
			throw new RequestException(MasterdataSearchErrorCode.FILTER_TYPE_NOT_AVAILABLE.getErrorCode(),
					String.format(MasterdataSearchErrorCode.FILTER_TYPE_NOT_AVAILABLE.getErrorMessage(),
							filter.getColumnName()));
		}
		return false;
	}

	/**
	 * Method to validate the individual filter
	 * 
	 * @param filter input filter to be validated
	 * @return true if valid false otherwise
	 */
	private boolean validateFilter(SearchFilter filter) {
		boolean flag = false;
		if (FilterTypeEnum.EQUALS.name().equalsIgnoreCase(filter.getType())
				&& filter.getColumnName().equalsIgnoreCase(IS_ACTIVE_COLUMN_NAME)) {
			String value = filter.getValue();
			if (value != null && !value.trim().isEmpty()
					&& (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"))) {
				flag = true;
			} else {
				throw new RequestException(MasterdataSearchErrorCode.INVALID_VALUE.getErrorCode(),
						MasterdataSearchErrorCode.INVALID_VALUE.getErrorMessage());
			}

		} else if (!FilterTypeEnum.BETWEEN.name().equalsIgnoreCase(filter.getType())) {
			String value = filter.getValue();
			if (value != null && !value.trim().isEmpty()) {
				flag = true;
			} else {
				throw new RequestException(MasterdataSearchErrorCode.INVALID_VALUE.getErrorCode(),
						MasterdataSearchErrorCode.INVALID_VALUE.getErrorMessage());
			}
		} else {
			String fromValue = filter.getFromValue();
			String toValue = filter.getToValue();
			if (fromValue != null && !fromValue.trim().isEmpty() && toValue != null && !toValue.trim().isEmpty()) {
				flag = true;
			} else {
				throw new RequestException(MasterdataSearchErrorCode.INVALID_BETWEEN_VALUES.getErrorCode(),
						String.format(MasterdataSearchErrorCode.INVALID_BETWEEN_VALUES.getErrorMessage(),
								filter.getColumnName()));
			}
		}
		return flag;
	}

	/**
	 * Method to validate the Sort Filter
	 * 
	 * @param sort sort filter to be validated
	 * @return true if valid false otherwise
	 */
	private boolean validateSort(SearchSort sort) {
		if (sort != null) {
			String field = sort.getSortField();
			String type = sort.getSortType();
			if (field != null && !field.isEmpty() && type != null && !type.isEmpty()) {
				return true;
			} else {
				throw new RequestException(MasterdataSearchErrorCode.INVALID_SORT_INPUT.getErrorCode(),
						MasterdataSearchErrorCode.INVALID_SORT_INPUT.getErrorMessage());
			}
		}
		return false;
	}

	public Page<Machine> nativeMachineQuerySearch(SearchDto searchDto, String typeName, List<Zone> zones,
			boolean isAssigned) {
		List<String> zoneCodes = new ArrayList<>();
		zones.stream().forEach(zone -> {
			zoneCodes.add(zone.getCode());
		});
		StringBuilder nativeQuery = null;
		if (isAssigned) {
			nativeQuery = new StringBuilder().append("SELECT * FROM master.machine_master m where m.id IN");
		} else {
			nativeQuery = new StringBuilder().append("SELECT * FROM master.machine_master m where m.id NOT IN");

		}

		if (searchDto.getLanguageCode().equals("all")) {

			nativeQuery.append(
					"(select  rcm.machine_id from master.reg_center_machine rcm ) and m.mspec_id in(select id from master.machine_spec ms , master.machine_type mt where ms.mtyp_code= mt.code and mt.name=:typeName) AND m.zone_code in (:zoneCode)");
		} else {
			nativeQuery.append(
					"(select  rcm.machine_id from master.reg_center_machine rcm ) and m.lang_code=:langCode and m.mspec_id in(select id from master.machine_spec ms , master.machine_type mt where ms.mtyp_code= mt.code and mt.name=:typeName and ms.lang_code=:langCode and ms.lang_code=mt.lang_code) AND m.zone_code in (:zoneCode)");
		}

		Iterator<SearchFilter> searchIterator = searchDto.getFilters().iterator();
		while (searchIterator.hasNext()) {
			SearchFilter searchFilter = searchIterator.next();
			String columnName = getColumnName(searchFilter.getColumnName(), Machine.class);
			nativeQuery.append(" and m." + columnName + "=:" + searchFilter.getColumnName());
		}

		Query query = entityManager.createNativeQuery(nativeQuery.toString(), Machine.class);
		if (!searchDto.getLanguageCode().equals("all")) {
			query.setParameter(LANGCODE_COLUMN_NAME, searchDto.getLanguageCode());
		}
		setMachineQueryParams(query, searchDto.getFilters());
		query.setParameter(TYPE_NAME, typeName);
		query.setParameter("zoneCode", zoneCodes);

		List<Machine> result = query.getResultList();
		return new PageImpl<>(result,
				PageRequest.of(searchDto.getPagination().getPageStart(), searchDto.getPagination().getPageFetch()),
				query.getResultList().size());

	}

	public Page<Device> nativeDeviceQuerySearch(SearchDto searchDto, String typeName, List<Zone> zones,
			boolean isAssigned) {
		List<String> zoneCodes = new ArrayList<>();
		zones.stream().forEach(zone -> {
			zoneCodes.add(zone.getCode());
		});
		StringBuilder nativeQuery = null;
		if (isAssigned) {
			nativeQuery = new StringBuilder().append("SELECT * FROM master.device_master m where m.id IN");
		} else {
			nativeQuery = new StringBuilder().append("SELECT * FROM master.device_master m where m.id NOT IN");

		}

		if (searchDto.getLanguageCode().equals("all")) {
			nativeQuery.append(
					"(select  distinct rcm.device_id from master.reg_center_device rcm ) and  m.dspec_id in(select id from master.device_spec ms , master.device_type mt where ms.dtyp_code= mt.code and mt.name=:typeName) AND m.zone_code in (:zoneCode)");
		} else {
			nativeQuery.append(
					"(select  distinct rcm.device_id from master.reg_center_device rcm ) and m.lang_code=:langCode and m.dspec_id in(select id from master.device_spec ms , master.device_type mt where ms.dtyp_code= mt.code and mt.name=:typeName and ms.lang_code=:langCode and ms.lang_code=mt.lang_code) AND m.zone_code in (:zoneCode)");
		}
		Iterator<SearchFilter> searchIterator = searchDto.getFilters().iterator();
		while (searchIterator.hasNext()) {
			SearchFilter searchFilter = searchIterator.next();
			String columnName = getColumnName(searchFilter.getColumnName(), Device.class);
			nativeQuery.append(" and m." + columnName + "=:" + searchFilter.getColumnName());
		}

		nativeQuery.append(" OFFSET " + searchDto.getPagination().getPageStart() + " ROWS FETCH NEXT "
				+ searchDto.getPagination().getPageFetch() + " ROWS ONLY");

		Query query = entityManager.createNativeQuery(nativeQuery.toString(), Device.class);

		setDeviceQueryParams(query, searchDto.getFilters());
		if (!searchDto.getLanguageCode().equals("all")) {
			query.setParameter(LANGCODE_COLUMN_NAME, searchDto.getLanguageCode());
		}
		query.setParameter(TYPE_NAME, typeName);
		query.setParameter("zoneCode", zoneCodes);
		List<Device> result = query.getResultList();

		return new PageImpl<>(result,
				PageRequest.of(searchDto.getPagination().getPageStart(), searchDto.getPagination().getPageFetch()),
				query.getResultList().size());

	}

	private void setDeviceQueryParams(Query query, List<SearchFilter> list) {
		Iterator<SearchFilter> searchIter = list.iterator();
		while (searchIter.hasNext()) {
			SearchFilter searchFilter = searchIter.next();
			switch (searchFilter.getColumnName()) {
			case DEVICE_NAME:
				query.setParameter(DEVICE_NAME, searchFilter.getValue());
				break;
			case "isActive":
				query.setParameter("isActive", Boolean.valueOf(searchFilter.getValue()));
				break;
			case MAC_ADDRESS:
				query.setParameter(MAC_ADDRESS, searchFilter.getValue());
				break;
			case SERIAL_NUMBER:
				query.setParameter(SERIAL_NUMBER, searchFilter.getValue());
				break;
			case "deviceSpecId":
				query.setParameter("deviceSpecId", searchFilter.getValue());
				break;
			default:
				break;
			}
		}
	}

	private void setMachineQueryParams(Query query, List<SearchFilter> list) {
		Iterator<SearchFilter> searchIter = list.iterator();
		while (searchIter.hasNext()) {
			SearchFilter searchFilter = searchIter.next();
			switch (searchFilter.getColumnName()) {
			case "name":
				query.setParameter("name", searchFilter.getValue());
				break;
			case "isActive":
				query.setParameter("isActive", Boolean.valueOf(searchFilter.getValue()));
				break;
			case MAC_ADDRESS:
				query.setParameter(MAC_ADDRESS, searchFilter.getValue());
				break;
			case SERIAL_NUMBER:
				query.setParameter(SERIAL_NUMBER, searchFilter.getValue());
				break;
			case MACHINE_SPEC_ID:
				query.setParameter(MACHINE_SPEC_ID, searchFilter.getValue());
				break;
			default:
				break;
			}
		}

	}

	private <E extends BaseEntity> String getColumnName(String fieldName, Class<E> entity) {
		String columnName = null;

		for (Field field : entity.getDeclaredFields()) {
			if (field.isAnnotationPresent(Column.class)) {
				String entityColumnName = field.getAnnotation(Column.class).name();
				if (fieldName.equals(field.getName())) {
					columnName = entityColumnName;
					break;
				}
			}
		}

		if (columnName == null) {

			for (Field field : entity.getSuperclass().getDeclaredFields()) {
				if (field.isAnnotationPresent(Column.class)) {
					String entityColumnName = field.getAnnotation(Column.class).name();
					if (fieldName.equals(field.getName())) {
						columnName = entityColumnName;
						break;
					}
				}
			}
		}
		return columnName;
	}
}
