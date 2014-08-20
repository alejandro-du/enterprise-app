package enterpriseapp.ui.crud;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Table;
import com.vaadin.ui.TableFieldFactory;
import com.vaadin.ui.themes.Reindeer;

import enterpriseapp.Utils;
import enterpriseapp.hibernate.CustomHbnContainer.EntityItem;
import enterpriseapp.hibernate.DefaultHbnContainer;
import enterpriseapp.hibernate.annotation.CrudField;
import enterpriseapp.hibernate.annotation.Downloadable;
import enterpriseapp.hibernate.dto.Dto;
import enterpriseapp.ui.Constants;

/**
 * A CRUD Table used by CrudComponent.
 * 
 * @author Alejandro Duarte
 *
 * @param <T> CRUD Entity type.
 */
public class CrudTable<T extends Dto> extends Table implements FieldContainer {

	private static final long serialVersionUID = 1L;
	
	protected Class<T> type;
	protected Container container;
	protected Button newButton = new Button(Constants.uiAdd);
	protected Button deleteButton = new Button(Constants.uiDelete);
	protected HashMap<Item, HashMap<Object, Field>> fields;
	protected PropertyFormatter propertyFormatter;
	
	/**
	 * @param type Entity type.
	 * @param container Container to use.
	 * @param fieldFactory TableFieldFactory to use in editable mode.
	 */
	public CrudTable(Class<T> type, Container container, final TableFieldFactory fieldFactory) {
		this.type = type;
		this.container = container;
		propertyFormatter = new DefaultPropertyFormatter();
		
		setTableFieldFactory(new TableFieldFactory() {
			private static final long serialVersionUID = 1L;
			@Override
			public Field createField(Container container, Object itemId, Object propertyId, Component uiContext) {
				Field field = getField(propertyId, getItem(itemId));
				
				if(field == null) {
					field = fieldFactory.createField(container, itemId, propertyId, uiContext);
				}
				
				return field;
			}
		});
		
		setBuffered(false);
		setMultiSelect(true);
		setContainerDataSource(container);
		configureColumns();
		updateTable();
		initLayout();
	}
	
	/**
	 * Inits the component layout.
	 */
	public void initLayout() {
		newButton.setStyleName(Reindeer.BUTTON_SMALL);
		deleteButton.setStyleName(Reindeer.BUTTON_SMALL);
		setSizeFull();
		setSelectable(true);
		setImmediate(true);
		setColumnCollapsingAllowed(true);
		setColumnReorderingAllowed(true);
	}
	
	/**
	 * Configures visible columns.
	 */
	public void configureColumns() {
		Object[] allColumns = getVisibleColumns();
		
		ArrayList<Object> collapsedColumns = new ArrayList<Object>();
		
		for(Object column : allColumns) {
			if(isColumnCollapsed(column)) {
				collapsedColumns.add(column);
			}
		}
		
		setVisibleColumns(allColumns);
		
		for(Object column : collapsedColumns) {
			setColumnCollapsed(column, true);
		}
	}
	
	/**
	 * Updates table's content.
	 */
	@SuppressWarnings("unchecked")
	public void updateTable() {
		if(DefaultHbnContainer.class.isAssignableFrom(container.getClass())) {
			((DefaultHbnContainer<T>) container).refresh();
		}
	}
	
	public boolean isVisibleColumn(Object column) {
		try {
			java.lang.reflect.Field declaredField = type.getDeclaredField((String) column);
			CrudField crudAnnotation = declaredField.getAnnotation(CrudField.class);
			Downloadable downloadableAnnotation = declaredField.getAnnotation(Downloadable.class);
			
			boolean expresslyShowColumn = crudAnnotation != null && crudAnnotation.showInTable();
			boolean expresslyHideColumn = crudAnnotation != null && !crudAnnotation.showInTable();
			boolean automaticallyHideColum = downloadableAnnotation != null;
			
			return !expresslyHideColumn && (expresslyShowColumn || !automaticallyHideColum);
			
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		newButton.setVisible(enabled);
		deleteButton.setVisible(enabled);
	}
	
	@Override
	public void setEditable(boolean editable) {
		super.setEditable(editable);
		newButton.setVisible(editable);
		deleteButton.setVisible(editable);
	}
	
	@Override
	public String getColumnHeader(Object propertyId) {
		String typeName = type.getSimpleName();
		String nameFromFile = Utils.getPropertyLabel(typeName, propertyId);
		return nameFromFile.isEmpty() ? super.getColumnHeader(propertyId) : nameFromFile;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public String formatPropertyValue(Object rowId, Object colId, Property property) {
		String value = null;
		
		if(property != null) {
			try {
				Object bean = null;
				
				if(container instanceof BeanContainer) {
					bean = ((BeanContainer) container).getItem(rowId).getBean();
				} else {
					bean = ((EntityItem.EntityProperty) property).getPojo();
				}
				
				String propertyName = colId.toString();
				
				Class<?> clazz = type.getDeclaredField(propertyName).getType();
				String capitalizedPropertyName = propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1, propertyName.length());
				String propertyGetterName;
				
				if(clazz == boolean.class || clazz == Boolean.class) {
					propertyGetterName = "is" + capitalizedPropertyName;
				} else {
					propertyGetterName = "get" + capitalizedPropertyName;
				}
				
				CrudField crudFieldAnnotation = type.getDeclaredField(propertyName).getAnnotation(CrudField.class);
				Method propertyGetter = type.getMethod(propertyGetterName, (Class<?>[]) null);
				Object propertyObject = propertyGetter.invoke(bean, (Object[]) null);
				
				if(propertyObject == null) {
					return "";
				}
				
				Class<?> returnType = propertyGetter.getReturnType();
				
				value = propertyFormatter.formatPropertyValue(rowId, colId, property, bean, propertyObject, returnType, crudFieldAnnotation, this);
				
			} catch (SecurityException e) {
				throw new RuntimeException(e);
			} catch (NoSuchMethodException e) {
				throw new RuntimeException(e);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (NoSuchFieldException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
		
		if(value == null) {
			value = super.formatPropertyValue(rowId, colId, property);
		}
		
		return value;
	}

	@Override
	public void addField(Field field, Object propertyId, Item item) {
		if(fields == null) {
			fields = new HashMap<Item, HashMap<Object, Field>>();
		}
		
		HashMap<Object, Field> rowMap = fields.get(item);
		
		if(rowMap == null) {
			rowMap = new HashMap<Object, Field>();
			fields.put(item, rowMap);
		}
		
		rowMap.remove(propertyId);
		rowMap.put(propertyId, field);
	}
	
	@Override
	public Field getField(Object propertyId, Item item) {
		Field field = null;
		
		if(fields != null) {
			HashMap<Object, Field> rowMap = fields.get(item);
			if(rowMap != null) {
				field = rowMap.get(propertyId);
			}
		}
		
		return field;
	}
	
	/**
	 * @return The table fields when editable mode is on.
	 */
	public Collection<Field> getFields() {
		ArrayList<Field> list = new ArrayList<Field>();
		
		if(fields != null) {
			for(HashMap<Object, Field> map : fields.values()) {
				list.addAll(map.values());
			}
		}
		
		return list;
	}
	
	/**
	 * Removes the fields for the specified item when editable mode is on.
	 * @param item
	 */
	public void removeFields(Item item) {
		if(fields != null) {
			fields.remove(item);
		}
	}
	
	/**
	 * Removes all fields when editable mode is on.
	 */
	public void removeAllFields() {
		if(fields != null) {
			fields.clear();
		}
	}
	
	@Override
	public boolean isValid() {
		try {
			validate();
		} catch (InvalidValueException e) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public void validate() throws Validator.InvalidValueException {
		super.validate();
		
		if(fields != null) {
			for(HashMap<Object, Field> map : fields.values()) {
				for(Field field : map.values()) {
					field.validate();
				}
			}
		}
	}

	/**
	 * @return CRUD Entity type.
	 */
	public Class<T> getType() {
		return type;
	}

	/**
	 * Sets a custom PropertyFormatter
	 * @param propertyFormatter PropertyFormatter to use.
	 */
	public void setPropertyFormatter(PropertyFormatter propertyFormatter) {
		this.propertyFormatter = propertyFormatter;
	}
	
}
