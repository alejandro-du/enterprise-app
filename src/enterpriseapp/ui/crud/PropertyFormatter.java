package enterpriseapp.ui.crud;


import com.vaadin.data.Property;

import enterpriseapp.hibernate.annotation.CrudField;

public interface PropertyFormatter {
	
	String formatPropertyValue(Object rowId, Object colId, Property property, Object bean, Object propertyObject, Class<?> returnType, CrudField crudFieldAnnotation, CrudTable<?> crudTable);

}
