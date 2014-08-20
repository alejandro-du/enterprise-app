package enterpriseapp.hibernate;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.BasicDynaClass;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.hibernate.internal.CriteriaImpl.Subcriteria;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.Type;

import com.vaadin.data.Item;
import com.vaadin.data.hbnutil.ContainerFilter;

import enterpriseapp.Utils;
import enterpriseapp.hibernate.annotation.CrudTable;
import enterpriseapp.ui.Constants;

/**
 * Container based on HbnContainer add-on (https://vaadin.com/directory#addon/hbncontainer).
 * 
 * @author Alejandro Duarte
 *
 * @param <T> Entity (Dto) type.
 */
@SuppressWarnings("unchecked")
public class DefaultHbnContainer<T> extends CustomHbnContainer<T> {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor.
	 * @param clazz Entity class.
	 */
	public DefaultHbnContainer(Class<T> clazz) {
		super(clazz, Db.getCurrentSession().getSessionFactory());
	}
	
	/**
	 * @return a new Entity instance.
	 */
	public T newInstance() {
		T newInstance = null;
		
		try {
			newInstance = entityType.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		
		return newInstance;
	}
	
	/**
	 * Returns the Entity with the given id.
	 * @param id Entity's id to load.
	 * @return the Entity with the given id or null if not found.
	 */
	public T getEntity(Serializable id) {
		return (T) sessionFactory.getCurrentSession().get(entityType, id);
	}
	
	/**
	 * @return the number of Entities in this container.
	 */
	public long count() {
		return (Long) singleSpecialQuery("select count(id) from " + entityType.getSimpleName());
	}
	
	@Override
	public boolean removeItem(Object itemId) throws UnsupportedOperationException {
		boolean result = super.removeItem(itemId);
		sessionFactory.getCurrentSession().getTransaction().commit();
		sessionFactory.getCurrentSession().beginTransaction();
		return result;
	}
	
	/**
	 * Removes all items in the container.
	 */
	public boolean removeAllItems() throws UnsupportedOperationException {
		update("delete from " + entityType.getSimpleName());
		return true;
	}
	
	/**
	 * Adds a new Entity to the container and returns an EntityItem.
	 * @param itemId Entity to add.
	 * @return A new EntityItem containing the added Entity.
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Item addItem(Object itemId) throws UnsupportedOperationException {
		T dto = (T) saveOrUpdateEntity((T) itemId);
		return new EntityItem((Serializable) dto);
	}
	
	/**
	 * Override this to add custom behavior before an Entity is saved or updated.
	 * @param entity the Entity being saved or updated.
	 */
	public void beforeSaveOrUpdate(T entity) {
	}
	
	/**
	 * Override this to add custom behavior after an Entity is saved or updated.
	 * @param entity the Entity being saved or updated.
	 */
	public void afterSaveOrUpdate(T entity) {
	}
	
	/**
	 * Saves or update the given Entity.
	 * @param entity Entity to save or update.
	 * @return id for the saved Entity.
	 */
	public Serializable saveOrUpdateEntity(T entity) {
		beforeSaveOrUpdate(entity);
		
		entity = (T) sessionFactory.getCurrentSession().merge(entity);
		sessionFactory.getCurrentSession().saveOrUpdate(entity);
		
		clearInternalCache();
		fireItemSetChange();
		
		afterSaveOrUpdate(entity);
		return (Serializable) getIdForPojo(entity);
	}
	
	/**
	 * Saves the given Entity.
	 * @param entity Entity to save.
	 * @return id for the saved Entity.
	 */
	@Override
	public Serializable saveEntity(T entity) {
		sessionFactory.getCurrentSession().save(entity);
		
		clearInternalCache();
		fireItemSetChange();
		
		return (Serializable) getIdForPojo(entity);
	}
	
	/**
	 * Gets all the Entities in the container.
	 * @return all Entities in the container.
	 */
	public List<T> listAll() {
		Criteria crit = getCriteria();
		return crit.list();
	}
	
	/**
	 * Executes the given query.
	 * @param query query to execute.
	 * @return Entity returned by the query (null if no result found).
	 */
	protected T singleQuery(String query) {
		return singleQuery(query, null);
	}

	/**
	 * Executes the given query with the given parameters.
	 * @param query query to execute.
	 * @param params query parameters.
	 * @return Entity returned by the query (null if no result found).
	 */
	protected T singleQuery(String query, Object[] params) {
		List<T> list = query(query, params);
		if(list != null && !list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}
	
	/**
	 * Executes the given query with the given parameters by name.
	 * @param query query to execute.
	 * @param paramNames parameters' names in the query.
	 * @param params query parameters.
	 * @return Entity returned by the query (null if no result found).
	 */
	protected T singleQuery(String query, String[] paramNames, Object[] params) {
		return query(query, paramNames, params).get(0);
	}
	
	/**
	 * Executes the given query.
	 * @param query query to execute.
	 * @return List of Entities returned by the query).
	 */
	protected List<T> query(String query) {
		return query(query, null);
	}
	
	/**
	 * Executes the given query with the given parameters.
	 * @param query query to execute.
	 * @param params query parameters.
	 * @return List of Entities returned by the query.
	 */
	protected List<T> query(final String query, final Object[] params) {
		
		Query q = sessionFactory.getCurrentSession().createQuery(query);
		
		if(params != null) {
			for(int i = 0; i < params.length; i++) {
				q.setParameter(i, params[i]);
			}
		}
		
		List<T> list = q.list(); 
		
		return list;
	}

	/**
	 * Executes the given query with the given parameters by name.
	 * @param query query to execute.
	 * @param paramNames parameters' names in the query.
	 * @param params query parameters.
	 * @return List of Entities returned by the query.
	 */
	protected List<T> query(final String query, final String[] paramNames, final Object[] params) {
		return query(query, paramNames, params, null, null);
	}

	/**
	 * Executes the given query with the given parameters by name.
	 * @param query query to execute.
	 * @param paramNames parameters' names in the query.
	 * @param params query parameters.
	 * @param collectionParamNames parameters' names for parameters of type Collection.
	 * @param collectionParams query parameters of type Collection.
	 * @return List of Entities returned by the query.
	 */
	protected List<T> query(final String query, final String[] paramNames, final Object[] params, final String[] collectionParamNames, Collection<?>[] collectionParams) {
		return query(query, paramNames, params, collectionParamNames, collectionParams, null, null);
	}

	/**
	 * Executes the given query with the given parameters by name.
	 * @param query query to execute.
	 * @param paramNames parameters' names in the query.
	 * @param params query parameters.
	 * @param collectionParamNames parameters' names for parameters of type Collection.
	 * @param collectionParams query parameters of type Collection.
	 * @param maxResults max results to return.
	 * @param firstResult first result to return.
	 * @return List of Entities returned by the query.
	 */
	protected List<T> query(final String query, final String[] paramNames, final Object[] params, final String[] collectionParamNames, Collection<?>[] collectionParams, Integer maxResults, Integer firstResult) {
		Query q = getQuery(query, paramNames, params, collectionParamNames, collectionParams, maxResults, firstResult);
		
		List<T> list = q.list();
		
		return list;
	}
	
	/**
	 * Executes the given query.
	 * @param query query to execute.
	 * @return result of the query (type defined by the query select clause).
	 */
	protected Object singleSpecialQuery(final String query) {
		List<?> result = specialQuery(query);
		return result != null && result.size() > 0 ? result.get(0) : null;
	}
	
	/**
	 * Executes the given query with the given parameters.
	 * @param query query to execute.
	 * @param params query parameters.
	 * @return result of the query (type defined by the query select clause).
	 */
	protected Object singleSpecialQuery(final String query, final Object[] params) {
		List<?> result = specialQuery(query, params);
		return result != null && result.size() > 0 ? result.get(0) : null;
	}
	
	/**
	 * Executes the given query.
	 * @param query query to execute.
	 * @return List of results of the query (type defined by the query select clause).
	 */
	protected List<?> specialQuery(final String query) {
		return specialQuery(query, null);
	}
	
	/**
	 * Executes the given query.
	 * @param query query to execute.
	 * @param params query parameters.
	 * @return List of results of the query (type defined by the query select clause).
	 */
	@SuppressWarnings("rawtypes")
	protected List specialQuery(final String query, final Object[] params) {
		Query q = sessionFactory.getCurrentSession().createQuery(query);
		
		if(params != null) {
			for(int i = 0; i < params.length; i++) {
				q.setParameter(i, params[i]);
			}
		}
		
		List list = q.list(); 
		
		return list;
	}
	
	/**
	 * Executes the given query.
	 * @param query query to execute.
	 * @param paramNames parameters' names in the query.
	 * @param params query parameters.
	 * @return List of results of the query (type defined by the query select clause).
	 */
	@SuppressWarnings("rawtypes")
	protected List specialQuery(final String query, final String[] paramNames, final Object[] params) {
		Query q = sessionFactory.getCurrentSession().createQuery(query);
		
		if(params != null) {
			for(int i = 0; i < params.length; i++) {
				q.setParameter(paramNames[i], params[i]);
			}
		}
		
		List list = q.list(); 
		
		return list;
	}
	
	/**
	 * Executes the given query.
	 * @param query query to execute.
	 * @param paramNames parameters' names in the query.
	 * @param params query parameters.
	 * @param collectionParamNames parameters' names for parameters of type Collection.
	 * @param collectionParams query parameters of type Collection.
	 * @return List of results of the query (type defined by the query select clause).
	 */
	@SuppressWarnings("rawtypes")
	protected List specialQuery(final String query, final String[] paramNames, final Object[] params, final String[] collectionParamNames, Collection<?>[] collectionParams) {
		return specialQuery(query, paramNames, params, collectionParamNames, collectionParams, null, null);
	}
	
	/**
	 * Executes the given query.
	 * @param query query to execute.
	 * @param paramNames parameters' names in the query.
	 * @param params query parameters.
	 * @param collectionParamNames parameters' names for parameters of type Collection.
	 * @param collectionParams query parameters of type Collection.
	 * @param maxResults max results to return.
	 * @param firstResult first result to return.
	 * @return List of results of the query (type defined by the query select clause).
	 */
	@SuppressWarnings("rawtypes")
	protected List specialQuery(final String query, final String[] paramNames, final Object[] params, final String[] collectionParamNames, Collection<?>[] collectionParams, Integer maxResults, Integer firstResult) {
		Query q = getQuery(query, paramNames, params, collectionParamNames, collectionParams, maxResults, firstResult);
		
		List list = q.list(); 
		
		return list;
	}
	
	/**
	 * Executes the given query.
	 * @param query query to execute.
	 */
	public void update(final String query) {
		getQuery(query, null, null, null, null, null, null).executeUpdate();
	}
	
	/**
	 * Executes the given query.
	 * @param query query to execute.
	 * @param paramNames parameters' names in the query.
	 * @param params query parameters.
	 */
	public void update(final String query, final Object[] params) {
		getQuery(query, null, params, null, null, null, null).executeUpdate();
	}
	
	/**
	 * Executes the given query.
	 * @param query query to execute.
	 * @param paramNames parameters' names in the query.
	 * @param params query parameters.
	 */
	public void update(final String query, final String[] paramNames, final Object[] params) {
		getQuery(query, paramNames, params, null, null, null, null).executeUpdate();
	}
	
	/**
	 * Executes the given query.
	 * @param query query to execute.
	 * @param paramNames parameters' names in the query.
	 * @param params query parameters.
	 * @param collectionParamNames parameters' names for parameters of type Collection.
	 * @param collectionParams query parameters of type Collection.
	 */
	public void update(final String query, final String[] paramNames, final Object[] params, final String[] collectionParamNames, Collection<?>[] collectionParams) {
		getQuery(query, paramNames, params, collectionParamNames, collectionParams, null, null).executeUpdate();
	}
	
	/**
	 * Constructs a Query object.
	 * @param query query text.
	 * @param paramNames parameters' names in the query (if null, positional binding is used).
	 * @param params query parameters.
	 * @param collectionParamNames parameters' names for parameters of type Collection.
	 * @param collectionParams query parameters of type Collection.
	 * @param maxResults max results to return.
	 * @param firstResult first result to return.
	 * @return Query object.
	 */
	public Query getQuery(final String query, final String[] paramNames, final Object[] params, final String[] collectionParamNames, Collection<?>[] collectionParams, Integer maxResults, Integer firstResult) {
		Query q = sessionFactory.getCurrentSession().createQuery(query);
		
		if(maxResults != null) {
			q.setMaxResults(maxResults);
		}
		
		if(firstResult != null) {
			q.setFirstResult(firstResult);
		}
		
		if(paramNames != null) {
			if(params != null) {
				for(int i = 0; i < params.length; i++) {
					q.setParameter(paramNames[i], params[i]);
				}
			}
		} else {
			if(params != null) {
				for(int i = 0; i < params.length; i++) {
					q.setParameter(i, params[i]);
				}
			}
		}
		
		if(collectionParams != null) {
			for(int i = 0; i < collectionParams.length; i++) {
				q.setParameterList(collectionParamNames[i], collectionParams[i]);
			}
		}
		
		return q;
	}
	
	/**
	 * Converts the special query result to a DynaBean collection. This method is useful to create reports from
	 * special queries.
	 * @param result result returned by "specialQuery" methods.
	 * @param properties properties for the beans to create.
	 * @param classes property types.
	 * @return a collection of dynamic beans.
	 */
	public Collection<?> parseSpecialQueryResult(List<?> result, String[] properties, Class<?>[] classes) {
		ArrayList<DynaBean> data = new ArrayList<DynaBean>();
		
		for(Object row : result) {
			if(Object[].class.isAssignableFrom(row.getClass())) {
				data.add(objectArrayToBean((Object[]) row, properties, classes));
			} else {
				data.add(objectArrayToBean(new Object[] {row}, properties, classes));
			}
		}
		
		return data;
	}
	
	/**
	 * Constructs a DynaBean using the values and properties specified. 
	 * @param values property values.
	 * @param properties properties to add to the bean.
	 * @param classes properties types.
	 * @return DynBean with the values and properties specified.
	 */
	protected DynaBean objectArrayToBean(Object[] values, String[] properties, Class<?>[] classes) {
		DynaBean dynaBean = null;
		
		try {
			DynaProperty[] columnsDynaProperties = getColumnsDynaProperties(properties, classes);
			BasicDynaClass clazz = new BasicDynaClass(this.getClass().getSimpleName(), BasicDynaBean.class, columnsDynaProperties);
			dynaBean = clazz.newInstance();
			
			for(int i = 0; i < columnsDynaProperties.length; i++) {
				dynaBean.set(columnsDynaProperties[i].getName(), values[i]);
			}
			
		} catch (Exception e) {
			throw new RuntimeException("Error creating dynamic bean", e);
		}
		
		return dynaBean;
	}
	
	protected DynaProperty[] getColumnsDynaProperties(String[] properties, Class<?>[] classes) {
		DynaProperty[] dynaProperties = new DynaProperty[properties.length];
		
		for(int i = 0; i < properties.length; i++) {
			dynaProperties[i] = new DynaProperty(properties[i], classes[i]);
		}
		
		return dynaProperties;
	}

	protected ClassMetadata getClassMetadata() {
		if (classMetadata == null) {
			classMetadata = sessionFactory.getClassMetadata(entityType);
		}
		return classMetadata;
	}
	
	/**
	 * @return a Criteria object with restrictions accordingly to current filters.
	 */
	@Override
	public Criteria getBaseCriteria() {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(entityType);
		ArrayList<Subcriteria> subcriterias = new ArrayList<Subcriteria>();
		
		if (filters != null) {
			for (ContainerFilter f : filters) {
				StringContainerFilter filter = (StringContainerFilter) f;
				
				String propertyString = filter.getPropertyId().toString();
				String[] properties = propertyString.split("\\.");
				String property = properties[properties.length - 1];
				Type propertyType = getClassMetadata().getPropertyType(properties[0]);
				
				if(properties.length > 1) {
					for(int i = 0; i < properties.length - 1; i++) {
						
						boolean subcriteriaFound = false;
						
						for(Subcriteria sc : subcriterias) {
							if(sc.getPath().equals(properties[i])) {
								criteria = sc;
								subcriteriaFound = true;
								break;
							}
						}
						
						if(!subcriteriaFound) {
							criteria = criteria.createCriteria(properties[i]);
							subcriterias.add((Subcriteria) criteria);
						}
						
						propertyType = sessionFactory.getCurrentSession().getSessionFactory().getClassMetadata(propertyType.getReturnedClass()).getPropertyType(properties[i + 1]);
					}
				}
				
				Class<?> returnedClass = propertyType.getReturnedClass();
				
				if(propertyType.isCollectionType()) {
					try {
						Field field = entityType.getDeclaredField(property);
						ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
						returnedClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];
						
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
					
					
				}
				
				if(propertyType.isAssociationType()) {
					CrudTable crudTableAnnotation = (CrudTable) returnedClass.getAnnotation(CrudTable.class);
					
					if(crudTableAnnotation != null) {
						boolean subcriteriaFound = false;
						
						for(Subcriteria sc : subcriterias) {
							if(sc.getPath().equals(property)) {
								criteria = sc;
								subcriteriaFound = true;
								break;
							}
						}
						
						if(!subcriteriaFound) {
							criteria = criteria.createCriteria(property);
							subcriterias.add((Subcriteria) criteria);
						}
						
						property = crudTableAnnotation.filteringPropertyName();
						propertyType = sessionFactory.getCurrentSession().getSessionFactory().getClassMetadata(returnedClass).getPropertyType(property);
						
					} else {
						throw new RuntimeException("Entity class " + getClassMetadata().getEntityName() + " doesn't declare a filtering property name (no CrudTable annotation present).");
					}
				}
				
				StringContainerFilter sf = new StringContainerFilter(property, filter.filterString, filter.filterString2, filter.ignoreCase, filter.onlyMatchPrefix);
				Criterion criterion = getCustomRestriction(sf, propertyType.getReturnedClass());
				
				if(criterion != null) {
					criteria.add(criterion);
				}
				
				while(criteria.getClass().equals(Subcriteria.class)) {
					criteria = ((Subcriteria) criteria).getParent();
				}
				
			}
		}
		
		return criteria;
	}
	
	/**
	 * Returns a Restriction accordingly to the given Filter.
	 * @param Filter to construct the Restriction object.
	 * @param clazz property class.
	 * @return a Restriction object for the given Filter.
	 */
	public Criterion getCustomRestriction(StringContainerFilter filter, Class<?> clazz) {
		
		Criterion criterion = null;
		
		if(clazz.equals(String.class)) {
			if (filter.ignoreCase) {
				criterion = Restrictions.ilike(filter.getPropertyId().toString(), filter.filterString, filter.onlyMatchPrefix ? MatchMode.START : MatchMode.ANYWHERE);
			} else {
				criterion = Restrictions.like(filter.getPropertyId().toString(), filter.filterString, filter.onlyMatchPrefix ? MatchMode.START : MatchMode.ANYWHERE);
			}
		} else if(clazz.equals(Integer.class)) {
			try {
				criterion = Restrictions.eq(filter.getPropertyId().toString(), new Integer(filter.filterString));
			} catch (NumberFormatException e) {
			}
		} else if(clazz.equals(Long.class)) {
			try {
				criterion = Restrictions.eq(filter.getPropertyId().toString(), new Long(filter.filterString));
			} catch (NumberFormatException e) {
			}
			
		} else if(clazz.equals(Double.class)) {
			try {
				criterion = Restrictions.eq(filter.getPropertyId().toString(), new Double(filter.filterString));
			} catch (NumberFormatException e) {
			}
			
		} else if(clazz.equals(Float.class)) {
			try {
				criterion = Restrictions.eq(filter.getPropertyId().toString(), new Double(filter.filterString));
			} catch (NumberFormatException e) {
			}
			
		} else if(clazz.equals(BigDecimal.class)) {
			try {
				criterion = Restrictions.eq(filter.getPropertyId().toString(), new BigDecimal(filter.filterString));
			} catch (NumberFormatException e) {
			}
			
		} else if(clazz.equals(Date.class)) {
			try {
				Date date1;
				Date date2;
				
				if(filter.filterString.isEmpty()) {
					date1 = new Date(Long.MIN_VALUE);
				} else {
					int length = filter.filterString.length() < Utils.getDateTimeFormatPattern().length() ? filter.filterString.length() : Utils.getDateTimeFormatPattern().length();
					date1 = new SimpleDateFormat(Utils.getDateTimeFormatPattern().substring(0, length)).parse(filter.filterString);
				}
				if(filter.filterString2.isEmpty()) {
					date2 = Utils.getMaxDate();
				} else {
					int length = filter.filterString2.length() < Utils.getDateTimeFormatPattern().length() ? filter.filterString2.length() : Utils.getDateTimeFormatPattern().length();
					date2 = new SimpleDateFormat(Utils.getDateTimeFormatPattern().substring(0, length)).parse(filter.filterString2);
				}
				criterion = Restrictions.between(filter.getPropertyId().toString(), date1, date2);
			} catch (ParseException e) {
				Date date1 = new Date(1);
				Date date2 = new Date(2);
				// this should reject all results
				criterion = Restrictions.between(filter.getPropertyId().toString(), date2 , date1); 
			}
		} else if(clazz.equals(Boolean.class)) {
			if(filter.filterString.equalsIgnoreCase(Constants.uiYes) || filter.filterString.equalsIgnoreCase(Constants.uiYes.substring(0, 1)) || filter.filterString.equals("1")) {
				criterion = Restrictions.eq(filter.getPropertyId().toString(), true);
				
			} else if (filter.filterString.equalsIgnoreCase(Constants.uiNo) || filter.filterString.equalsIgnoreCase(Constants.uiNo.substring(0, 1)) || filter.filterString.equals("0")) {
				criterion = Restrictions.eq(filter.getPropertyId().toString(), false);
				
			} else {
				// this should reject all results
				criterion = Restrictions.and(Restrictions.eq(filter.getPropertyId().toString(), true), Restrictions.eq(filter.getPropertyId().toString(), false));
			}
			
		}
		
		return criterion;
	}
	
	public void refresh() {
		clearInternalCache();
		fireItemSetChange();
	}
	
}
