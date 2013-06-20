package enterpriseapp.hibernate;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.hibernate.Criteria;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.impl.CriteriaImpl.Subcriteria;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.ComponentType;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.data.util.filter.UnsupportedFilterException;

/**
 * Lazy, almost full-featured, general purpose Hibernate entity container. Makes
 * lots of queries, but shouldn't consume too much memory.
 * <p>
 * HbnContainer is developed and tested with session-per-request pattern, but
 * should work with other session handling mechanisms too. To abstract away
 * session handling, user must provide an implementation of SessionManager
 * interface, via HbnContainer fetches reference to Session instance whenever it
 * needs it. Session returned by HbnContainer is expected to be open.
 * 
 * <p>
 * In in its constructor it will only need entity class type (Pojo) and a
 * SessionManager.
 * <p>
 * HbnContainer also expects that identifiers are auto generated. This matters
 * only if HbnContainer is used to create new entities.
 * <p>
 * Note, container caches size, firstId, lastId to be much faster with large
 * datasets. TO DO: make this caching optional, actually should trust on
 * Hibernates and DB engines query caches.
 * 
 * <p>
 * See http://vaadin.com/wiki/-/wiki/Main/Using%20Hibernate%20with%20Vaadin?
 * p_r_p_185834411_title=Using%20Hibernate%20with%20Vaadin for a working example
 * application.
 * 
 * @author Matti Tahvonen (IT Mill)
 * @author Henri Sara (IT Mill)
 * @author Daniel Bell (itree.com.au, bugfixes, support for embedded composite
 *         keys, ability to add non Hibernate-mapped properties)
 * @author Marc Englund (IT Mill, weak item cache to conserve memory/return same
 *         item instance) Update item to reference updated pojo.
 * @author Pavel Micka updateEntity method
 * @author Alejandro Duarte Just changes on visibility of some methods (from private to protected) to allow overriding.
 */
@SuppressWarnings({"unchecked", "rawtypes", "deprecation"})
public class CustomHbnContainer<T> implements Container.Indexed, Container.Sortable, Container.ItemSetChangeNotifier,
		Container.Filterable {

	private static final int REFERENCE_CLEANUP_INTERVAL = 2000;
	private static Logger logger = LoggerFactory.getLogger(CustomHbnContainer.class);
	private static final long serialVersionUID = -6410337120924382057L;

	/**
	 * SessionManager interface is used by HbnContainer to get reference to
	 * Hibernates Session object.
	 */
	public interface SessionManager {
		/**
		 * @return a Hibernate Session with open transaction
		 */
		Session getSession();
	}

	/**
	 * Item wrappping a Hibernate mapped entity object. EntityItems are
	 * generally instantiated automatically by HbnContainer.
	 */
	@SuppressWarnings("hiding")
	public class EntityItem<T> implements Item {

		private static final long serialVersionUID = -2847179724504965599L;

		/**
		 * Reference to hibernate mapped entity that this Item wraps.
		 */
		protected T pojo;

		/**
		 * Instantiated properties of this EntityItem. May be either
		 * EntityItemProperty (hibernate field) or manually added container
		 * property (MethodProperty).
		 */
		protected Map<Object, Property> properties = new HashMap<Object, Property>();

		public EntityItem(Serializable id) {
			pojo = (T) sessionManager.getSession().get(type, id);
			// add non-hibernate mapped container properties
			for (String propertyId : addedProperties.keySet()) {
				addItemProperty(propertyId, new MethodProperty(pojo, propertyId));
			}
		}
		
		/**
		 * @return the wrapped entity object.
		 */
		public T getPojo() {
			return pojo;
		}

		public boolean addItemProperty(Object id, Property property) throws UnsupportedOperationException {
			properties.put(id, property);
			return true;
		}

		public Property getItemProperty(Object id) {
			Property p = properties.get(id);
			if (p == null) {
				p = new EntityItemProperty(id.toString());
				properties.put(id, p);
			}
			return p;
		}

		public Collection<?> getItemPropertyIds() {
			return getContainerPropertyIds();
		}

		public boolean removeItemProperty(Object id) throws UnsupportedOperationException {
			Property removed = properties.remove(id);
			return removed != null;
		}

		/**
		 * EntityItemProperty wraps one Hibernate controlled field of the pojo
		 * used by EntityItem. For common fields the field value is the same as
		 * Property value. For relation fields it is the identifier of related
		 * object or a collection of identifiers.
		 */
		public class EntityItemProperty implements Property, Property.ValueChangeNotifier {

			private static final long serialVersionUID = -4086774943938055297L;
			private final String propertyName;

			public EntityItemProperty(String propertyName) {
				this.propertyName = propertyName;
			}

			public EntityItem<T> getEntityItem() {
				return EntityItem.this;
			}

			public T getPojo() {
				return pojo;
			}

			/**
			 * A helper method to get raw type of this (Hibernate) property.
			 * 
			 * @return the raw type of field
			 */
			private Type getPropertyType() {
				return getClassMetadata().getPropertyType(propertyName);
			}

			private boolean propertyInEmbeddedKey() {
				// TO DO: a place for optimization, this is not needed to be done
				// for each separate property
				Type idType = getClassMetadata().getIdentifierType();
				if (idType.isComponentType()) {
					ComponentType idComponent = (ComponentType) idType;
					String[] idPropertyNames = idComponent.getPropertyNames();
					List<String> idPropertyNameList = Arrays.asList(idPropertyNames);
					if (idPropertyNameList.contains(propertyName)) {
						return true;
					} else {
						return false;
					}
				} else {
					return false;
				}
			}

			public Class<?> getType() {
				// TO DO: clean, optimize, review

				if (propertyInEmbeddedKey()) {
					ComponentType idType = (ComponentType) getClassMetadata().getIdentifierType();
					String[] propertyNames = idType.getPropertyNames();
					for (int i = 0; i < propertyNames.length; i++) {
						String name = propertyNames[i];
						if (name.equals(propertyName)) {
							try {
								String idName = getClassMetadata().getIdentifierPropertyName();
								Field idField = type.getDeclaredField(idName);
								Field propertyField = idField.getType().getDeclaredField(propertyName);
								return propertyField.getType();
							} catch (NoSuchFieldException ex) {
								throw new RuntimeException("Could not find the type of specified container property.",
										ex);
							}
						}
					}
				}

				Type propertyType = getPropertyType();
				if (propertyType.isCollectionType()) {
					/*
					 * For collection types the Property value is the same type
					 * of collection, but containing identifiers instead of the
					 * actual referenced objects.
					 */
					Class<?> returnedClass = propertyType.getReturnedClass();
					return returnedClass;
				} else if (propertyType.isAssociationType()) {
					/*
					 * For association the the property value type is the type
					 * of referenced types identifier. Use Hibernates
					 * ClassMetadata for referenced type and get the type of its
					 * identifier.
					 */
					// TO DO: clean, optimize, review, this could be optimized
					// among similar properties
					ClassMetadata classMetadata2 = sessionManager.getSession().getSessionFactory()
							.getClassMetadata(getClassMetadata().getPropertyType(propertyName).getReturnedClass());
					return classMetadata2.getIdentifierType().getReturnedClass();

				} else {
					/*
					 * For basic fields the Property type is the same as the
					 * type in entity class.
					 */
					return getClassMetadata().getPropertyType(propertyName).getReturnedClass();
				}
			}

			public Object getValue() {

				// TO DO: clean, optimize, review

				// Ensure we have an attached pojo
				if (!sessionManager.getSession().contains(pojo)) {
					pojo = (T) sessionManager.getSession().get(type, (Serializable) getIdForPojo(pojo));
				}

				if (propertyInEmbeddedKey()) {
					ComponentType idType = (ComponentType) getClassMetadata().getIdentifierType();
					String[] propertyNames = idType.getPropertyNames();
					for (int i = 0; i < propertyNames.length; i++) {
						String name = propertyNames[i];
						if (name.equals(propertyName)) {
							Object id = getClassMetadata().getIdentifier(pojo, EntityMode.POJO);
							return idType.getPropertyValue(id, i, EntityMode.POJO);
						}
					}
				}

				Type propertyType = getPropertyType();
				Object propertyValue = getClassMetadata().getPropertyValue(pojo, propertyName, EntityMode.POJO);

				if (!propertyType.isAssociationType()) {
					return propertyValue;
				} else if (propertyType.isCollectionType()) {
					if (propertyValue == null) {
						return null;
					}

					/*
					 * Build a HashSet of identifiers of entities stored in
					 * collection.
					 */
					HashSet<Serializable> identifiers = new HashSet<Serializable>();
					Collection<?> pojos = (Collection) propertyValue;

					for (Object object : pojos) {
						// here, object must be of an association type
						if (!sessionManager.getSession().contains(object)) {
							// ensure a fresh object if session contains the
							// object
							object = sessionManager.getSession().merge(object);
						}
						identifiers.add(sessionManager.getSession().getIdentifier(object));
					}
					return identifiers;
				} else {
					/*
					 * the return value will be the identifier of referenced
					 * object
					 */
					if (propertyValue == null) {
						return null;
					}
					Class<?> propertyTypeClass = propertyType.getReturnedClass();

					ClassMetadata classMetadata2 = sessionManager.getSession().getSessionFactory()
							.getClassMetadata(propertyTypeClass);

					Serializable identifier = classMetadata2.getIdentifier(propertyValue, EntityMode.POJO);
					return identifier;
				}
			}

			public boolean isReadOnly() {
				// TO DO:
				return false;
			}

			public void setReadOnly(boolean newStatus) {
				throw new UnsupportedOperationException();
			}

			public void setValue(Object newValue) throws ReadOnlyException, ConversionException {

				try {
					Object value;
					try {
						if (newValue == null || getType().isAssignableFrom(newValue.getClass())) {
							value = newValue;
						} else {
							// Gets the string constructor
							final Constructor<?> constr = getType().getConstructor(new Class[] { String.class });

							value = constr.newInstance(new Object[] { newValue.toString() });
						}

						// TO DO: same optimizations (caching introspection of
						// types) as in getType and getValue
						// could be done here.

						if (propertyInEmbeddedKey()) {
							ComponentType idType = (ComponentType) getClassMetadata().getIdentifierType();
							String[] propertyNames = idType.getPropertyNames();
							for (int i = 0; i < propertyNames.length; i++) {
								String name = propertyNames[i];
								if (name.equals(propertyName)) {
									Object id = getClassMetadata().getIdentifier(pojo, EntityMode.POJO);
									Object[] values = idType.getPropertyValues(id, EntityMode.POJO);
									values[i] = value;
									idType.setPropertyValues(id, values, EntityMode.POJO);
								}
							}
						} else {
							Type propertyType = getClassMetadata().getPropertyType(propertyName);
							if (propertyType.isCollectionType()) {
								/*
								 * Value is a collection of identifiers of
								 * referenced objects.
								 */
								// TO DO: figure out how to fetch mapped type
								// properly
								Field declaredField = type.getDeclaredField(propertyName);
								java.lang.reflect.Type genericType = declaredField.getGenericType();
								java.lang.reflect.Type[] actualTypeArguments = ((ParameterizedType) genericType)
										.getActualTypeArguments();

								java.lang.reflect.Type assosiatedType = actualTypeArguments[0];
								String typestring = assosiatedType.toString().substring(6);

								/*
								 * Reuse existing persistent collection if
								 * possible so Hibernate may optimize queries
								 * properly.
								 */
								Collection<Object> pojoCollection = (Collection<Object>) getClassMetadata()
										.getPropertyValue(pojo, propertyName, EntityMode.POJO);
								if (pojoCollection == null) {
									pojoCollection = new HashSet<Object>();
									getClassMetadata().setPropertyValue(pojo, propertyName, pojoCollection,
											EntityMode.POJO);
								}
								// copy existing set, so we can track which are
								// to be removed
								Collection<Object> orphans = new HashSet<Object>(pojoCollection);

								Collection<?> identifiers = (Collection<?>) value;
								Session session = sessionManager.getSession();
								// add missing objects
								for (Object id : identifiers) {
									Object object = session.get(typestring, (Serializable) id);
									if (!pojoCollection.contains(object)) {
										pojoCollection.add(object);
									} else {
										orphans.remove(object);
									}
								}
								// remove the ones that are no more supposed to
								// be in collection
								pojoCollection.removeAll(orphans);

							} else if (propertyType.isAssociationType()) {
								/*
								 * Property value is identifier, convert to the
								 * referenced type
								 */
								Class<?> referencedType = getClassMetadata().getPropertyType(propertyName)
										.getReturnedClass();
								Object object = sessionManager.getSession().get(referencedType, (Serializable) value);
								getClassMetadata().setPropertyValue(pojo, propertyName, object, EntityMode.POJO);
								// TO DO: check if these are needed
								sessionManager.getSession().merge(object);
								sessionManager.getSession().saveOrUpdate(pojo);

							} else {
								getClassMetadata().setPropertyValue(pojo, propertyName, value, EntityMode.POJO);
							}
						}
						// Persist (possibly) detached pojo
						T newPojo = (T) sessionManager.getSession().merge(pojo);
						pojo = newPojo;

						fireValueChange();

					} catch (final java.lang.Exception e) {
						logger.error("Error.", e);
						logger.error("Error.", e);
						throw new Property.ConversionException(e);
					}

				} catch (HibernateException e) {
					logger.error("Error.", e);
				}
			}

			@Override
			public String toString() {
				Object v = getValue();
				if (v != null) {
					return v.toString();
				} else {
					return null;
				}
			}

			private class HbnPropertyValueChangeEvent implements Property.ValueChangeEvent {
				private static final long serialVersionUID = 166764621324404579L;

				public Property getProperty() {
					return EntityItemProperty.this;
				}
			}

			private List<ValueChangeListener> valueChangeListeners;

			private void fireValueChange() {
				if (valueChangeListeners != null) {
					HbnPropertyValueChangeEvent event = new HbnPropertyValueChangeEvent();
					Object[] array = valueChangeListeners.toArray();
					for (int i = 0; i < array.length; i++) {
						((ValueChangeListener) array[i]).valueChange(event);
					}
				}
			}

			public void addListener(ValueChangeListener listener) {
				if (valueChangeListeners == null) {
					valueChangeListeners = new LinkedList<ValueChangeListener>();
				}
				if (!valueChangeListeners.contains(listener)) {
					valueChangeListeners.add(listener);
				}
			}

			public void removeListener(ValueChangeListener listener) {
				if (valueChangeListeners != null) {
					valueChangeListeners.remove(listener);
				}
			}

		}
	}

	private static final int ROW_BUF_SIZE = 100;
	private static final int ID_TO_INDEX_MAX_SIZE = 300;

	/** Entity class that will be listed in container */
	protected Class<T> type;
	protected final SessionManager sessionManager;
	private transient ClassMetadata classMetadata;

	/** internal flag used to temporarily invert order of listing */
	private boolean normalOrder = true;

	/** Row buffer of pojos, used to optimize query count when iterating forward */
	private List<T> ascRowBuffer;
	/**
	 * Row buffer of pojos, used to optimize query count when iterating backward
	 */

	private List<T> descRowBuffer;
	/** cached last item identifier */
	private Object lastId;
	/** cached first item identifier */
	private Object firstId;

	/**
	 * Row buffer of pojos, used to optimize query count when container is
	 * accessed with indexes
	 */
	private List<T> indexRowBuffer;

	/** Container wide index of the first entity in indexRowBuffer */
	private int indexRowBufferFirstIndex;

	/**
	 * Map from entity/item identifiers to index. Maps does not contain mapping
	 * for all identifiers in container, but only those that are recently
	 * loaded. Map gets cleanded during usage, to free memory.
	 */
	private final Map<Object, Integer> idToIndex = new LinkedHashMap<Object, Integer>();

	/**
	 * whether sorts are made ascending or descending, see
	 * {@link #orderPropertyIds}
	 */
	private boolean[] orderAscendings;
	/**
	 * Properties among container is sorted by. Used to implement
	 * Container.Sortable
	 */
	private Object[] orderPropertyIds;

	/** Cached size of container. Used to optimize query count. */
	private Integer size;

	private LinkedList<ItemSetChangeListener> itemSetChangeListeners;

	/**
	 * Contains current filters which has been applied to this container. Used
	 * to implement Container.Filterable.
	 */
	protected HashSet<ContainerFilter> filters;

	/** Caches weak references to items, in order to conserve memory. */
	private transient HashMap<Object, WeakReference<EntityItem<T>>> itemCache;

	/** A map of added javabean property names to their respective types */
	private final Map<String, Class<?>> addedProperties = new HashMap<String, Class<?>>();

	/**
	 * counter for items loaded by container, used to implement cleanup of
	 * weakreferences
	 */
	private int loadCount;

	private HashMap<Object, WeakReference<EntityItem<T>>> getItemCache() {
		if(itemCache == null) {
			itemCache = new HashMap<Object, WeakReference<EntityItem<T>>>();
		}
		
		return itemCache;
	}

	/**
	 * Creates a new instance of HbnContainer, listing all object of given type
	 * from database.
	 * 
	 * @param entityType
	 *            Entity class to be listed in container.
	 * @param sessionMgr
	 *            interface via Hibernate session is fetched
	 */
	public CustomHbnContainer(Class<T> entityType, SessionManager sessionMgr) {
		type = entityType;
		sessionManager = sessionMgr;
	}

	/**
	 * HbnContainer automatically adds all fields that are mapped by Hibernate
	 * to DB. With this method one can add a javabean property to the container
	 * that is contained on pojo but not hibernate-mapped.
	 * 
	 * @see Container#addContainerProperty(Object, Class, Object)
	 */
	public boolean addContainerProperty(Object propertyId, Class<?> type, Object defaultValue)
			throws UnsupportedOperationException {
		boolean propertyExists = true;
		try {
			new MethodProperty(this.type.newInstance(), propertyId.toString());
		} catch (InstantiationException ex) {
			ex.printStackTrace();
			propertyExists = false;
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
			propertyExists = false;
		}
		addedProperties.put(propertyId.toString(), type);
		return propertyExists;
	}

	/**
	 * HbnContainer specific method to persist newly created entity.
	 * 
	 * @param entity
	 *            the unsaved entity object
	 * @return the identifier for newly persisted entity
	 */
	public Serializable saveEntity(T entity) {
		// insert into DB
		sessionManager.getSession().save(entity);
		clearInternalCache();
		fireItemSetChange();
		return (Serializable) getIdForPojo(entity);
	}

	/**
	 * HbnContainer specific method to update entity.
	 * 
	 * @param entity
	 *            to update
	 * @return the identifier of the updated entity
	 */
	public Serializable updateEntity(T entity) {
		// update DB
		sessionManager.getSession().save(entity);

		EntityItem<T> item = null;
		Serializable itemId = (Serializable) getIdForPojo(entity);

		if (getItemCache() != null) {
			// refresh itemCache
			WeakReference<EntityItem<T>> weakReference = getItemCache().get(itemId);
			if (weakReference != null) {
				item = weakReference.get();
				if (item != null) { // may be already collected, but not cleaned
					item.pojo = entity;
				}
			}
		}

		if (item != null) { // if it was in cache, it might be rendered
			// fire change events on this item, properties might have changed
			for (Object id : item.getItemPropertyIds()) {
				Property p = item.getItemProperty(id);
				if (p instanceof EntityItem.EntityItemProperty) {
					EntityItem.EntityItemProperty ep = (EntityItem.EntityItemProperty) p;
					ep.fireValueChange();
				}
			}
		}

		return itemId;
	}

	public Object addItem() throws UnsupportedOperationException {
		Object o;
		try {
			// create a new instance of entity type
			o = type.newInstance();
			// insert into DB
			sessionManager.getSession().save(o);
			// we need to clear internal caches of HbnContainer
			clearInternalCache();
			// notify listeners that a new item has been added, will cause eg.
			// Table refresh
			fireItemSetChange();
			return getIdForPojo(o);
		} catch (InstantiationException e) {
			logger.error("Error.", e);
			return null;
		} catch (IllegalAccessException e) {
			logger.error("Error.", e);
			return null;
		}
	}

	public Item addItem(Object itemId) throws UnsupportedOperationException {
		// Expecting autogenerated identifiers
		throw new UnsupportedOperationException();
	}

	public boolean containsId(Object itemId) {
		// test if entity can be found with given id
		try {
			return (sessionManager.getSession().get(type, (Serializable) itemId) != null);
		} catch (Exception e) {
			// this should not happen if used correctly
			logger.error("Error.", e);
			return false;
		}
	}

	public Property getContainerProperty(Object itemId, Object propertyId) {
		return getItem(itemId).getItemProperty(propertyId);
	}

	public Collection getContainerPropertyIds() {
		Collection propertyIds = getSortableContainerPropertyIds();
		propertyIds.addAll(addedProperties.keySet());
		return propertyIds;
	}

	private Collection<String> getEmbeddedKeyPropertyIds() {
		ArrayList<String> embeddedKeyPropertyIds = new ArrayList<String>();
		Type identifierType = getClassMetadata().getIdentifierType();
		if (identifierType.isComponentType()) {
			ComponentType idComponent = (ComponentType) identifierType;
			String[] propertyNameArray = idComponent.getPropertyNames();
			if (propertyNameArray != null) {
				List<String> propertyNames = Arrays.asList(propertyNameArray);
				embeddedKeyPropertyIds.addAll(propertyNames);
			}
		}
		return embeddedKeyPropertyIds;
	}

	/**
	 * Helper method to get the Hibernate ClassMetadata for listed entity type.
	 * Method lazyly caches the metadata, optimizing the lookup a bit (instead
	 * of fetching it continuously from Session).
	 * 
	 * @return Hibernates ClassMetadata for the listed entity type
	 */
	protected ClassMetadata getClassMetadata() {
		if (classMetadata == null) {
			classMetadata = sessionManager.getSession().getSessionFactory().getClassMetadata(type);
		}
		return classMetadata;
	}

	public EntityItem<T> getItem(Object itemId) {
		EntityItem<T> item = null;
		if (itemId != null) {
			item = loadItem((Serializable) itemId);
		}
		return item;
	}

	/**
	 * This method is used to fetch Items by id. Override this if you need
	 * customized EntityItems.
	 * 
	 * @param itemId
	 * @return
	 */
	protected EntityItem<T> loadItem(Serializable itemId) {
		// clean up refQue if there are some items to clean up
		cleanCache();

		EntityItem<T> item;
		// Search the itemCache if the entityitem is already loaded
		WeakReference<EntityItem<T>> weakReference = getItemCache().get(itemId);
		if (weakReference != null) {
			item = weakReference.get();
			// still check if weakreference still contained the item (may be
			// carbagecollected, but not cleand from cache map
			if (item != null) {
				// return the previously instantiated entityitem
				return item;
			}
		}

		item = new EntityItem<T>(itemId);
		getItemCache().put(itemId, new WeakReference<EntityItem<T>>(item));
		return item;
	}

	/**
	 * Cleans the itemCache of collected item references. This method run every
	 * now and then by {@link #loadItem(Serializable)}, but may be run manually
	 * too.
	 * 
	 * <p>
	 * TO DO: figure out if this is the best possible way to free the memory
	 * consumed by (empty) weak references and open this mechanism for extension
	 * 
	 */
	private void cleanCache() {
		if (++loadCount % REFERENCE_CLEANUP_INTERVAL == 0) {
			Set<Entry<Object, WeakReference<EntityItem<T>>>> entries = getItemCache().entrySet();
			for (Iterator<Entry<Object, WeakReference<EntityItem<T>>>> iterator = entries.iterator(); iterator
					.hasNext();) {
				Entry<Object, WeakReference<EntityItem<T>>> entry = iterator.next();
				if (entry.getValue().get() == null) {
					// if the referenced entityitem is carbage collected, remove
					// the weak reference itself
					iterator.remove();
				}
			}
		}
	}

	public Collection<?> getItemIds() {
		/*
		 * Create an optimized query to return only identifiers. Note that this
		 * method does not scale well for large database. At least Table is
		 * optimized so that it does not call this method.
		 */
		Criteria crit = getCriteria();
		
		while(crit.getClass().equals(Subcriteria.class)) {
			crit = ((Subcriteria) crit).getParent();
		}
		crit.setProjection(Projections.id());
		
		return crit.list();
	}

	public Class<?> getType(Object propertyId) {
		/*
		 * This method does pretty much the same thing as
		 * EntityItemProperty#getType()
		 * 
		 * TO DO: refactor to use same code, will also fix incomplete
		 * implementation of this method (for assosiation types). Not critical
		 * as componets don't really rely on this methods.
		 */
		if (addedProperties.keySet().contains(propertyId)) {
			return addedProperties.get(propertyId);
		} else if (propertyInEmbeddedKey(propertyId)) {
			ComponentType idType = (ComponentType) getClassMetadata().getIdentifierType();
			String[] propertyNames = idType.getPropertyNames();
			for (int i = 0; i < propertyNames.length; i++) {
				String name = propertyNames[i];
				if (name.equals(propertyId)) {
					String idName = getClassMetadata().getIdentifierPropertyName();
					try {
						Field idField = type.getDeclaredField(idName);
						Field propertyField = idField.getType().getDeclaredField((String) propertyId);
						return propertyField.getType();
					} catch (NoSuchFieldException ex) {
						throw new RuntimeException("Could not find the type of specified container property.", ex);
					}
				}
			}
		}
		Type propertyType = getClassMetadata().getPropertyType(propertyId.toString());
		return propertyType.getReturnedClass();
	}

	/**
	 * TO DO: combine with the very same method from EntityItemProperty
	 * 
	 * @param propertyId
	 * @return true if property is part of embedded key of entity
	 */
	protected boolean propertyInEmbeddedKey(Object propertyId) {
		Type idType = getClassMetadata().getIdentifierType();
		if (idType.isComponentType()) {
			ComponentType idComponent = (ComponentType) idType;
			String[] idPropertyNames = idComponent.getPropertyNames();
			List<String> idPropertyNameList = Arrays.asList(idPropertyNames);
			if (idPropertyNameList.contains(propertyId)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public boolean removeAllItems() throws UnsupportedOperationException {
		// TO DO:
		return false;
	}

	/* Remove a container property added with addContainerProperty() */
	public boolean removeContainerProperty(Object propertyId) throws UnsupportedOperationException {
		boolean propertyExisted = false;
		Class<?> removed = addedProperties.remove(propertyId);
		if (removed != null) {
			propertyExisted = true;
		}
		return propertyExisted;
	}

	public boolean removeItem(Object itemId) throws UnsupportedOperationException {
		if(itemId != null) {
			Object p = sessionManager.getSession().load(type, (Serializable) itemId);
			// remove row from db
			sessionManager.getSession().delete(p);
			// clear internal caches and notify listeners that item has been removed
			clearInternalCache();
			fireItemSetChange();
		}
		
		return true;
	}

	public void addListener(ItemSetChangeListener listener) {
		if (itemSetChangeListeners == null) {
			itemSetChangeListeners = new LinkedList<ItemSetChangeListener>();
		}
		itemSetChangeListeners.add(listener);
	}

	public void removeListener(ItemSetChangeListener listener) {
		if (itemSetChangeListeners != null) {
			itemSetChangeListeners.remove(listener);
		}

	}

	protected void fireItemSetChange() {
		if (itemSetChangeListeners != null) {
			final Object[] l = itemSetChangeListeners.toArray();
			final Container.ItemSetChangeEvent event = new Container.ItemSetChangeEvent() {
				private static final long serialVersionUID = -3002746333251784195L;

				public Container getContainer() {
					return CustomHbnContainer.this;
				}
			};
			for (int i = 0; i < l.length; i++) {
				((ItemSetChangeListener) l[i]).containerItemSetChange(event);
			}
		}
	}

	public int size() {
		if (size == null) {
			/*
			 * If cached size does not exist, query from database
			 */
			size = ((Number) getBaseCriteria().setProjection(Projections.rowCount()).uniqueResult()).intValue();
		}
		return size.intValue();
	}

	public Object addItemAfter(Object previousItemId) throws UnsupportedOperationException {
		// can't implement properly for database backed container like this
		throw new UnsupportedOperationException();
	}

	public Item addItemAfter(Object previousItemId, Object newItemId) throws UnsupportedOperationException {
		// can't implement properly for database backed container like this
		throw new UnsupportedOperationException();
	}

	/**
	 * Gets a base listing using current orders etc.
	 * 
	 * @return criteria with current Order criterias added
	 */
	protected Criteria getCriteria() {
		Criteria criteria = getBaseCriteria();
			List<Order> orders = getOrder(!normalOrder);
			for (Order order : orders) {
				criteria.addOrder(order);
			}
		return criteria;
	}

	/**
	 * Return the ordering criteria in the order in which they should be
	 * applied. The composed order must be stable and must include
	 * {@link #getNaturalOrder(boolean)} at the end.
	 * 
	 * @param flipOrder
	 *            reverse the order if true
	 * @return List<Order> orders to apply, first item has the highest priority
	 */
	protected final List<Order> getOrder(boolean flipOrder) {
		List<Order> orders = new ArrayList<Order>();

		// standard sort order set by the user by property
		orders.addAll(getDefaultOrder(flipOrder));

		// natural order
		orders.add(getNaturalOrder(flipOrder));

		return orders;
	}

	/**
	 * Returns the ordering to use for the container contents. The default
	 * implementation provides the {@link Container.Sortable} functionality.
	 * 
	 * Can be overridden to customize item sort order.
	 * 
	 * @param flipOrder
	 *            reverse the order if true
	 * @return List<Order> orders to apply, first item has the highest priority
	 */
	protected List<Order> getDefaultOrder(boolean flipOrder) {
		List<Order> orders = new ArrayList<Order>();
		if (orderPropertyIds != null) {
			for (int i = 0; i < orderPropertyIds.length; i++) {
				String orderPropertyId = orderPropertyIds[i].toString();
				if (propertyInEmbeddedKey(orderPropertyId)) {
					String idName = getClassMetadata().getIdentifierPropertyName();
					orderPropertyId = idName + "." + orderPropertyId;
				}
				boolean a = flipOrder ? !orderAscendings[i] : orderAscendings[i];
				if (a) {
					orders.add(Order.asc(orderPropertyId));
				} else {
					orders.add(Order.desc(orderPropertyId));
				}
			}
		}
		return orders;
	}

	/**
	 * This method is meant to be called by HbnContainer itself. It will create
	 * the base criteria for entity class and add possible restrictions to
	 * query. This method is protected so developers can add their own custom
	 * criterias.
	 * 
	 * @return
	 */
	protected Criteria getBaseCriteria() {
		Criteria criteria = sessionManager.getSession().createCriteria(type);
		// if container is filtered via Container.Filterable API
		if (filters != null) {
			for (ContainerFilter filter : filters) {
				// convert ContainerFilters to hibernate Restriction Criterias
				String filterPropertyName = filter.propertyId.toString();
				if (propertyInEmbeddedKey(filterPropertyName)) {
					String idName = getClassMetadata().getIdentifierPropertyName();
					filterPropertyName = idName + "." + filterPropertyName;
				}
				
				if (filter.ignoreCase) {
					criteria = criteria.add(Restrictions.ilike(filterPropertyName, filter.filterString, filter.onlyMatchPrefix ? MatchMode.START : MatchMode.ANYWHERE));
				} else {
					criteria = criteria.add(Restrictions.like(filterPropertyName, filter.filterString, filter.onlyMatchPrefix ? MatchMode.START : MatchMode.ANYWHERE));
				}
			}
		}
		return criteria;
	}

	/**
	 * Natural order is the order in which the database is sorted if container
	 * has no other ordering set. Natural order is always added as least
	 * significant order to queries. This is needed to keep items stable order
	 * across queries.
	 * <p>
	 * The default implementation sorts entities by identifier column.
	 * 
	 * @param flipOrder
	 * @return
	 */
	protected Order getNaturalOrder(boolean flipOrder) {
		if (flipOrder) {
			return Order.desc(getIdPropertyName());
		} else {
			return Order.asc(getIdPropertyName());
		}
	}

	public Object firstItemId() {
		if (firstId == null) {
			// firstId was not cached, query the first item from db
			firstId = firstItemId(true);
		}
		return firstId;
	}

	/**
	 * Internanal helper method to implement {@link #firstItemId()} and
	 * {@link #lastItemId()}.
	 */
	protected Object firstItemId(boolean byPassCache) {
		if (byPassCache) {
			T first = (T) getCriteria().setMaxResults(1).setCacheable(true).uniqueResult();
			Object id = getIdForPojo(first);
			idToIndex.put(id, normalOrder ? 0 : size() - 1);
			return id;
		} else {
			return firstItemId();
		}
	}

	/**
	 * Helper method to detect identifier of given entity object.
	 * 
	 * @param pojo
	 *            the entity object which identifier is to be resolved
	 * @return the identifier if the given Hibernate entity object
	 */
	protected Object getIdForPojo(Object pojo) {
		return getClassMetadata().getIdentifier(pojo, EntityMode.POJO);
	}

	public boolean isFirstId(Object itemId) {
		return itemId.equals(firstItemId());
	}

	public boolean isLastId(Object itemId) {
		return itemId == null || itemId.equals(lastItemId());
	}

	public Object lastItemId() {
		if (lastId == null) {
			normalOrder = !normalOrder;
			lastId = firstItemId(true);
			normalOrder = !normalOrder;
		}
		return lastId;
	}

	/*
	 * Simple method, but lot's of code :-)
	 * 
	 * Rather complicated logic is needed to avoid:
	 * 
	 * 1. large number of db queries
	 * 
	 * 2. scrolling through whole query result
	 * 
	 * This way this container can be used with large data sets.
	 */
	public Object nextItemId(Object itemId) {
		// TO DO: should not call if know that next exists based on cache, would
		// optimize one query in some situations
		/*if (isLastId(itemId)) {
			return null;
		}*/

		EntityItem<T> item = new EntityItem<T>((Serializable) itemId);

		// check if next itemId is in current buffer
		List<T> buffer = getRowBuffer();
		try {
			int curBufIndex = buffer.indexOf(item.getPojo());
			if (curBufIndex != -1) {
				T object = buffer.get(curBufIndex + 1);
				return getIdForPojo(object);
			}
		} catch (Exception e) {
			// not in buffer
		}

		// itemId was not in buffer
		// build query with current order and limiting result set with the
		// reference row. Then first result is next item.

		int currentIndex = indexOfId(itemId);
		int firstIndex = normalOrder ? currentIndex + 1 : size() - currentIndex - 1;

		Criteria crit = getCriteria();
		crit = crit.setFirstResult(firstIndex).setMaxResults(ROW_BUF_SIZE);
		List<T> newBuffer = crit.list();
		if (newBuffer.size() > 0) {
			// save buffer to optimize query count
			setRowBuffer(newBuffer, firstIndex);
			T nextPojo = newBuffer.get(0);
			return getIdForPojo(nextPojo);
		} else {
			return null;
		}
	}

	/**
	 * RowBuffer stores a list of entity items to avoid excessive number of DB
	 * queries.
	 * 
	 * @return
	 */
	private List<T> getRowBuffer() {
		if (normalOrder) {
			return ascRowBuffer;
		} else {
			return descRowBuffer;
		}
	}

	/**
	 * RowBuffer stores some pojos to avoid excessive number of DB queries.
	 * 
	 * Also updates the idToIndex map.
	 */
	private void setRowBuffer(List<T> list, int firstIndex) {
		if (normalOrder) {
			ascRowBuffer = list;
			for (int i = 0; i < list.size(); ++i) {
				idToIndex.put(getIdForPojo(list.get(i)), firstIndex + i);
			}
		} else {
			descRowBuffer = list;
			int lastIndex = size() - 1;
			for (int i = 0; i < list.size(); ++i) {
				idToIndex.put(getIdForPojo(list.get(i)), lastIndex - firstIndex - i);
			}
		}
	}

	/**
	 * @return column name of identifier property
	 */
	private String getIdPropertyName() {
		return getClassMetadata().getIdentifierPropertyName();
	}

	public Object prevItemId(Object itemId) {
		// temp flip order and use nextItemId implementation
		normalOrder = !normalOrder;
		Object prev = nextItemId(itemId);
		normalOrder = !normalOrder;
		return prev;
	}

	// Container.Indexed

	/**
	 * Not supported in HbnContainer. Indexing/order is controlled by
	 * underlaying database.
	 */
	public Object addItemAt(int index) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported in HbnContainer. Indexing/order is controlled by
	 * underlaying database.
	 */
	public Item addItemAt(int index, Object newItemId) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	public Object getIdByIndex(int index) {
		if (indexRowBuffer == null) {
			resetIndexRowBuffer(index);
		}
		int indexInCache = index - indexRowBufferFirstIndex;
		if (!(indexInCache >= 0 && indexInCache < indexRowBuffer.size())) {
			/*
			 * If requested index is not currently in cache, reset it starting
			 * from queried index.
			 */
			resetIndexRowBuffer(index);
			indexInCache = 0;
		}
		T pojo = indexRowBuffer.get(indexInCache);
		Object id = getIdForPojo(pojo);
		idToIndex.put(id, new Integer(index));
		if (idToIndex.size() > ID_TO_INDEX_MAX_SIZE) {
			// clear one from beginning, if ID_TO_INDEX_MAX_SIZE is total of all
			// caches, only detached indexes should get removed
			idToIndex.remove(idToIndex.keySet().iterator().next());
		}
		return id;
	}

	/**
	 * Helper method to query new set of entity items to cache from given index.
	 * 
	 * @param index
	 *            the index of first entity object ot be included in query
	 */
	private void resetIndexRowBuffer(int index) {
		indexRowBufferFirstIndex = index;
		indexRowBuffer = getCriteria().setFirstResult(index).setMaxResults(ROW_BUF_SIZE).list();
	}

	/*
	 * Note! Expects that getIdByIndex is called for this itemId. When used with
	 * Table, this shouldn't be a problem.
	 * 
	 * TO DO: make workaround for this. Too bad it is going to be a very slow
	 * operation (request all IDs from DB in correct order and find the ID in
	 * the results).
	 */
	public int indexOfId(Object itemId) {
		Integer index = idToIndex.get(itemId);
		return index;
	}

	// Container.Sortable methods

	public Collection<String> getSortableContainerPropertyIds() {
		// use Hibernates metadata helper to determine property names
		String[] propertyNames = getClassMetadata().getPropertyNames();
		LinkedList<String> propertyIds = new LinkedList<String>();
		propertyIds.addAll(Arrays.asList(propertyNames));
		propertyIds.addAll(getEmbeddedKeyPropertyIds());
		return propertyIds;
	}

	public void sort(Object[] propertyId, boolean[] ascending) {
		// we do not actually sort anything here, just clearing cache will do
		// the thing lazily.
		clearInternalCache();
		orderPropertyIds = propertyId;
		orderAscendings = ascending;
	}

	/**
	 * Helper method to clear all cache fields.
	 */
	protected void clearInternalCache() {
		idToIndex.clear();
		indexRowBuffer = null;
		ascRowBuffer = null;
		descRowBuffer = null;
		firstId = null;
		lastId = null;
		size = null;
	}

	/**
	 * Helper class to contain filtering data from Container.Filterable
	 */
	class ContainerFilter {

		protected final Object propertyId;
		protected final String filterString;
		protected final boolean onlyMatchPrefix;
		protected final boolean ignoreCase;
		protected final String filterString2;

		public ContainerFilter(Object propertyId, String filterString, String filterString2, boolean ignoreCase, boolean onlyMatchPrefix) {
			this.propertyId = propertyId;
			this.ignoreCase = ignoreCase;
			this.filterString2 = filterString2;
			this.filterString = ignoreCase ? filterString.toLowerCase() : filterString;
			this.onlyMatchPrefix = onlyMatchPrefix;
		}

	}

	public void addContainerFilter(Object propertyId, String filterString, boolean ignoreCase, boolean onlyMatchPrefix) {
		addContainerFilter(propertyId, filterString, null, ignoreCase, onlyMatchPrefix);
	}
	
	
	/**
	 * Adds container filter for hibernate mapped property. For property not
	 * mapped by Hibernate, {@link UnsupportedOperationException} is thrown.
	 * 
	 * @see Container.Filterable#addContainerFilter(Object, String, boolean,
	 *      boolean)
	 */
	public void addContainerFilter(Object propertyId, String filterString, String filterString2, boolean ignoreCase, boolean onlyMatchPrefix) {
		if (addedProperties.containsKey(propertyId)) {
			throw new UnsupportedOperationException(
					"HbnContainer does not support filterig properties not mapped by Hibernate");
		} else {
			if (filters == null) {
				filters = new HashSet<ContainerFilter>();
			}
			ContainerFilter f = new ContainerFilter(propertyId, filterString, filterString2, ignoreCase, onlyMatchPrefix);
			filters.add(f);
			clearInternalCache();
			fireItemSetChange();
		}
	}

	public void removeAllContainerFilters() {
		if (filters != null) {
			filters = null;
			clearInternalCache();
			fireItemSetChange();
		}
	}

	public void removeContainerFilters(Object propertyId) {
		if (filters != null) {
			for (Iterator<ContainerFilter> iterator = filters.iterator(); iterator.hasNext();) {
				ContainerFilter f = iterator.next();
				if (f.propertyId.equals(propertyId)) {
					iterator.remove();
				}
			}
			clearInternalCache();
			fireItemSetChange();
		}
	}

	@Override
	public void addContainerFilter(Filter filter) throws UnsupportedFilterException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeContainerFilter(Filter filter) {
		throw new UnsupportedOperationException();
	}

}