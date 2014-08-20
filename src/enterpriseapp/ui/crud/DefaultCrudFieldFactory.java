package enterpriseapp.ui.crud;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.JoinColumn;

import org.hibernate.annotations.Type;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.validator.DoubleValidator;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.InlineDateField;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.Upload.FinishedEvent;

import enterpriseapp.EnterpriseApplication;
import enterpriseapp.Utils;
import enterpriseapp.hibernate.ContainerFactory;
import enterpriseapp.hibernate.CustomHbnContainer.EntityItem;
import enterpriseapp.hibernate.DefaultHbnContainer;
import enterpriseapp.hibernate.annotation.CrudField;
import enterpriseapp.hibernate.annotation.CrudTable;
import enterpriseapp.hibernate.annotation.Downloadable;
import enterpriseapp.hibernate.dto.Dto;
import enterpriseapp.ui.Constants;
import enterpriseapp.ui.DownloadField;
import enterpriseapp.ui.validator.LongValidator;

/**
 * 
 * A default FieldFactory for CrudComponents.
 * 
 * @author Alejandro Duarte
 *
 */
public class DefaultCrudFieldFactory extends DefaultFieldFactory {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Override this to create your custom fields.
	 * @param bean current bean (Entity/Dto) for which you want to build the field.
	 * @param item current item for which you want to build the field.
	 * @param pid property for which you want to build the field.
	 * @param uiContext the component where the field is presented.
	 * @param propertyType the property type for which you want to build the field.
	 * @return null if no custom Field is created.
	 */
	public Field createCustomField(Object bean, Item item, String pid, Component uiContext, Class<?> propertyType) {
		return null;
	}
	
	/**
	 * Configures the specified Field (adds caption, validators, etc.).
	 * @param field Field to configure.
	 * @param bean bean to which the field belongs.
	 * @param item item to which the field belongs.
	 * @param pid field property.
	 * @param uiContext the component where the field is presented.
	 * @param propertyType the field property type.
	 * @param crudFieldAnnotation CrudFieldAnnotation annotation (if present).
	 * @param columnAnnotation ColumnAnnotation annotation (if present).
	 * @param joinColumnAnnotation ColumnAnnotation annotation (if present).
	 * @param typeAnnotation TypeAnnotation (if present)
	 */
	public void configureField(Field field, Object bean, Item item, String pid, Component uiContext, Class<?> propertyType, CrudField crudFieldAnnotation, Column columnAnnotation, JoinColumn joinColumnAnnotation, Type typeAnnotation) {
		field.setCaption(getFieldCaption(pid, bean.getClass()));
		checkRequired(field, crudFieldAnnotation, columnAnnotation, joinColumnAnnotation);
		checkLength(field, columnAnnotation, typeAnnotation);
		checkNullRepresentation(field);
		addValidators(field, bean, item, pid, uiContext, propertyType, crudFieldAnnotation);
		field.setWidth("100%");
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Field createField(Item item, Object propertyId, Component uiContext) {
		Field field = null;
		
		if(uiContext == null) {
			return null;
		}
		
		try {
			String pid = propertyId.toString();
			BeanItem beanItem = (BeanItem) item;
			Object bean = beanItem.getBean();
			Class<?> propertyType = bean.getClass().getDeclaredField(propertyId.toString()).getType();
			
			java.lang.reflect.Field declaredField = bean.getClass().getDeclaredField(pid);
			CrudField crudFieldAnnotation = declaredField.getAnnotation(CrudField.class);
			Column columnAnnotation = declaredField.getAnnotation(Column.class);
			JoinColumn joinColumnAnnotation = declaredField.getAnnotation(JoinColumn.class);
			Downloadable downloadableAnnotation = declaredField.getAnnotation(Downloadable.class);
			Type typeAnnotation = declaredField.getAnnotation(Type.class);
			
			List<Object> visibleProperties = null;
			boolean propertiesDefined = false;
			
			if(CrudForm.class.isAssignableFrom(uiContext.getClass())) {
				Object[] visibleFormProperties = Utils.getVisibleFormProperties(bean.getClass());
				
				if(visibleFormProperties != null) {
					visibleProperties = Arrays.asList(visibleFormProperties);
					propertiesDefined = true;
				}
			} else if(enterpriseapp.ui.crud.CrudTable.class.isAssignableFrom(uiContext.getClass())) {
				Object[] visibleTableProperties = Utils.getVisibleTableProperties(bean.getClass());
				
				if(visibleTableProperties != null) {
					visibleProperties = Arrays.asList(visibleTableProperties);
					propertiesDefined = true;
				}
			}
			
			if(propertiesDefined) {
				if(visibleProperties == null || visibleProperties.isEmpty()) {
					return null;
				}
				
				if(!visibleProperties.contains(propertyId)) {
					return null;
				}
			}
			
			field = createCustomField(beanItem.getBean(), item, pid, uiContext, propertyType);
			
			if(field == null) {
				field = createEmbeddedTableField(propertyId, bean, propertyType, crudFieldAnnotation);
			}
			
			if(field == null) {
				field = createEntityField(propertyId, bean, propertyType);
			}
			
			if(field == null) {
				field = createDateField(pid, propertyType);
			}
			
			if(field == null) {
				field = createPasswordField(pid, crudFieldAnnotation);
			}
			
			if(field == null) {
				field = createDownloadableField(bean, pid, uiContext, downloadableAnnotation);
			}
			
			if(field == null) {
				field = super.createField(item, propertyId, uiContext);
			}
			
			configureField(field, bean, item, pid, uiContext, propertyType, crudFieldAnnotation, columnAnnotation, joinColumnAnnotation, typeAnnotation);
			
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		
		return field;
	}
	
	/**
	 * Creates a field.
	 * @param container Container to use for getting the item.
	 * @param itemId item id (Entity/Dto).
	 * @param propertyId property name.
	 * @param uiContext the component where the field is presented.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Field createField(Container container, Object itemId, Object propertyId, Component uiContext) {
		Item item = container.getItem(itemId);
		
		if(item instanceof EntityItem) {
			EntityItem entityItem = (EntityItem) item;
			item = new BeanItem(entityItem.getPojo());
		}
		
		Field field = createField(item, propertyId, uiContext);
		field.setBuffered(true);
		
		FieldContainer fieldContainer = (FieldContainer) uiContext;
		fieldContainer.addField(field, propertyId, item);
		
		return field;
    }
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Field createEmbeddedTableField(Object propertyId, Object bean, Class<?> propertyType, CrudField crudFieldAnnotation) {
		Field field = null;
		
		if(crudFieldAnnotation != null && crudFieldAnnotation.embedded()) {
			try {
				Dto dto = (Dto) bean;
				
				if(dto.getId() != null) {
					bean = (Dto) ContainerFactory.getInstance().getContainer(bean.getClass()).getEntity((Serializable) dto.getId());
				}
				
				String getterName;
				
				if(propertyType == boolean.class || propertyType == Boolean.class) {
					getterName = "is" + propertyId.toString().substring(0, 1).toUpperCase() + propertyId.toString().substring(1, propertyId.toString().length());
				} else {
					getterName = "get" + propertyId.toString().substring(0, 1).toUpperCase() + propertyId.toString().substring(1, propertyId.toString().length());
				}

				Class<?> clazz = (Class)((ParameterizedType)(bean).getClass().getDeclaredField(propertyId.toString()).getGenericType()).getActualTypeArguments()[0];
				Collection set = (Collection) bean.getClass().getMethod(getterName).invoke(bean);
				field = new EntityTable(clazz, set, new EmbeddedCrudComponent(clazz));
				
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		return field;
	}
	
	/**
	 * Creates a field to specify an Entity value.
	 * @param propertyId property name.
	 * @param bean bean to which the field belongs.
	 * @param propertyType property type.
	 * @param container container to use to get the Entities (Dtos).
	 * @return a new field to specify an Entity value.
	 */
	@SuppressWarnings("rawtypes")
	public Field createEntityField(Object propertyId, Object bean, Class<?> propertyType, DefaultHbnContainer<?> container) {
		Field field = null;
		
		try {
			if(propertyType.equals(Set.class) || propertyType.equals(List.class)) {
				Class<?> clazz = (Class)((ParameterizedType)(bean).getClass().getDeclaredField(propertyId.toString()).getGenericType()).getActualTypeArguments()[0];
				CrudTable crudTableAnnotation = clazz.getAnnotation(CrudTable.class);
				
				if(crudTableAnnotation == null) {
					throw new RuntimeException("Entity class " + clazz.getName() + " doesn't declare a filteringPropertyName (no CrudTable annotation present).");
				}
				
				EntitySetField entitySetField = new EntitySetField(clazz, container);
				field = entitySetField;
				
			} else if(Dto.class.isAssignableFrom(propertyType)) {
				CrudTable crudTableAnnotation = propertyType.getAnnotation(CrudTable.class);
				
				if(crudTableAnnotation == null) {
					throw new RuntimeException("Entity class " + propertyType.getName() + " doesn't declare a string representation (no CrudTable annotation present).");
				}
				
				EntityField entityField = new EntityField(propertyType, container);
				field = entityField;
			}
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
		
		return field;
	}
	
	/**
	 * Creates a field to specify an Entity value.
	 * @param propertyId property name.
	 * @param bean bean to which the field belongs.
	 * @param propertyType property type.
	 * @return a new field to specify an Entity value.
	 */
	public Field createEntityField(Object propertyId, Object bean, Class<?> propertyType) {
		return createEntityField(propertyId, bean, propertyType, null);
	}
	
	/**
	 * Creates a date field.
	 * @param pid property name.
	 * @param propertyType property type.
	 * @return a new field to select a date.
	 */
	public Field createDateField(String pid, Class<?> propertyType) {
		Field field = null;
		if(Date.class.isAssignableFrom(propertyType)) {
			DateField dateField = new DateField();
			dateField.setResolution(DateField.RESOLUTION_DAY);
			dateField.setDateFormat(Utils.getDateFormatPattern());
			field = dateField;
		}
        
        return field;
	}
	
	/**
	 * Creates an inline date field.
	 * @param pid property name.
	 * @param propertyType property type.
	 * @return a new field to select a date.
	 */
	public Field createInlineDateField(String pid, Class<?> propertyType) {
		Field field = null;
		if(Date.class.isAssignableFrom(propertyType)) {
			InlineDateField dateField = new InlineDateField();
			dateField.setResolution(InlineDateField.RESOLUTION_DAY);
			dateField.setDateFormat(Utils.getDateFormatPattern());
			field = dateField;
		}
        
        return field;
	}
	
	/**
	 * Checks if the CrudField annotation is present and have "password=true" to create a password field.
	 * @param pid property name.
	 * @param crudFieldAnnotation CrudField annotation if present.
	 * @return a new password field if crudFieldAnnotation is present and is configured to be a password, null otherwise.
	 */
	public Field createPasswordField(String pid, CrudField crudFieldAnnotation) {
		Field field = null;
		
		if(crudFieldAnnotation != null && crudFieldAnnotation.isPassword()) {
			field = new PasswordField();
		}
		
		return field;
	}

	/**
	 * Creates a field to download/upload a file.
	 * @param bean bean to which the field belongs.
	 * @param pid property name.
	 * @param uiContext the component where the field is presented.
	 * @param downloadableAnnotation
	 * @return a new download/upload field for the specified property.
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	public Field createDownloadableField(final Object bean, final String pid, Component uiContext, final Downloadable downloadableAnnotation) throws SecurityException, NoSuchMethodException {
		DownloadField field = null;
		
		if(downloadableAnnotation != null) {
			String capitalizedFileNameField = downloadableAnnotation.propertyFileName().substring(0, 1).toUpperCase() + downloadableAnnotation.propertyFileName().substring(1, downloadableAnnotation.propertyFileName().length());
			
			final Method setFileNameMethod = bean.getClass().getMethod("set" + capitalizedFileNameField, String.class);
			final Method getFileNameMethod = bean.getClass().getMethod("get" + capitalizedFileNameField, (Class<?>[]) null);
			
			field = new DownloadField(EnterpriseApplication.getInstance()) {
				private static final long serialVersionUID = 1L;
				
				@Override
				public void uploadFinishedEvent(FinishedEvent event) {
					super.uploadFinishedEvent(event);
					try {
						setFileNameMethod.invoke(bean, event.getFilename());
						
					} catch (IllegalArgumentException e) {
						throw new RuntimeException(e);
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					} catch (InvocationTargetException e) {
						throw new RuntimeException(e);
					}
				}
				
				@Override
				public String getFileName() {
					String name = "file";
					try {
						name = getFileNameMethod.invoke(bean, (Object[]) null) + downloadableAnnotation.fileNameSufix();
						
					} catch (IllegalArgumentException e) {
						throw new RuntimeException(e);
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					} catch (InvocationTargetException e) {
						throw new RuntimeException(e);
					}
					
					return name;
				}
			};
		}
		
		return field;
	}
	
	/**
	 * Adds the necessary validators to the specified field.
	 * @param field field wich the validators will be added.
	 * @param bean bean to which the field belongs.
	 * @param item item id (Entity/Dto).
	 * @param pid property name.
	 * @param uiContext the component where the field is presented.
	 * @param propertyType property type.
	 * @param crudFieldAnnotation CrudField annotation if present.
	 */
	public void addValidators(Field field, Object bean, Item item, String pid, Component uiContext, Class<?> propertyType, CrudField crudFieldAnnotation) {
		if(crudFieldAnnotation != null && crudFieldAnnotation.isEmail()) {
			field.addValidator(new EmailValidator(Constants.uiInvalidEmail));
		}
		
		if(Integer.class.isAssignableFrom(propertyType)) {
			field.addValidator(new IntegerValidator(Constants.uiInvalidIntegerValue));
			
		} else if(Long.class.isAssignableFrom(propertyType)) {
			field.addValidator(new LongValidator(Constants.uiInvalidLongValue));
			
		} else if(Double.class.isAssignableFrom(propertyType) || BigDecimal.class.isAssignableFrom(propertyType)) {
			field.addValidator(new DoubleValidator(Constants.uiInvalidDoubleValue));
		}
	}
	
	/**
	 * Returns the required error for the specified field.
	 * @param field field to configure.
	 * @return error string for the specified field.
	 */
	public String getRequiredError(Field field) {
		return Constants.uiRequiredField + ": "  + field.getCaption() + ".";
	}
	
	/**
	 * Returns the field caption for the specified property. Gets the error string from properties file.
	 * @param propertyId property name.
	 * @param type property type.
	 * @return field caption.
	 */
	public static String getFieldCaption(Object propertyId, Class<?> type) {
		String typeName = type.getSimpleName();
		String nameFromFile = Utils.getPropertyLabel(typeName, propertyId);
		return nameFromFile.isEmpty() ? DefaultFieldFactory.createCaptionByPropertyId(propertyId) : nameFromFile;
	}
	
	/**
	 * If the field is required, sets the required error string.
	 * @param field field to configure.
	 * @param columnAnnotation JPA Column annotation, if present.
	 * @param joinColumnAnnotation JoinColumn annotation, if present.
	 */
	public void checkRequired(Field field, CrudField crudFieldAnnotation, Column columnAnnotation, JoinColumn joinColumnAnnotation) {
		if(crudFieldAnnotation != null && crudFieldAnnotation.forceRequired()) {
			field.setRequired(true);
			
		} else if((columnAnnotation != null && !columnAnnotation.nullable()) || (joinColumnAnnotation != null && !joinColumnAnnotation.nullable())) {
			field.setRequired(true);
		}
		
		if(field.isRequired()) {
			field.setRequiredError(getRequiredError(field));
		}
	}
	
	/**
	 * If the Column annotation has a "length" attribute, adds a validator to check for the maximum allowed length.
	 * @param field
	 * @param columnAnnotation
	 */
	public void checkLength(Field field, Column columnAnnotation, Type typeAnnotation) {
		if(typeAnnotation != null && !"text".equals(typeAnnotation.type()))
		if(AbstractTextField.class.isAssignableFrom(field.getClass())) {
			if(columnAnnotation != null) {
				field.addValidator(new StringLengthValidator(Constants.uiMaxLengthExceeded(columnAnnotation.length()), 0, columnAnnotation.length(), true));
			}
		}
	}
	
	/**
	 * Configures the null representation for the field, if necessary.
	 * @param field field to configure.
	 */
	public void checkNullRepresentation(Field field) {
		if(AbstractTextField.class.isAssignableFrom(field.getClass())) {
			((AbstractTextField) field).setNullRepresentation("");
			((AbstractTextField) field).setNullSettingAllowed(true);
			((AbstractTextField) field).setImmediate(true);
		}
	}
	
}
