package enterpriseapp.ui.crud;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;


import com.vaadin.data.Property;

import enterpriseapp.Utils;
import enterpriseapp.hibernate.annotation.CrudField;
import enterpriseapp.hibernate.dto.Dto;
import enterpriseapp.ui.Constants;

/**
 * A default PropertyFormatter for CrudTable.
 * 
 * @author Alejandro Duarte.
 *
 */
public class DefaultPropertyFormatter implements PropertyFormatter, Serializable {

	private static final long serialVersionUID = 1L;

	@Override
	public String formatPropertyValue(Object rowId, Object colId, Property property, Object bean, Object propertyObject, Class<?> returnType, CrudField crudFieldAnnotation, CrudTable<?> crudTable) {
				
		if(Dto.class.isAssignableFrom(returnType)) {
			enterpriseapp.hibernate.annotation.CrudTable crudTableAnnotation = returnType.getAnnotation(enterpriseapp.hibernate.annotation.CrudTable.class);
			String stringRep = "";
			
			if(crudTableAnnotation != null) {
				stringRep = propertyObject.toString();
				
			} else {
				throw new RuntimeException("Entity class " + returnType.getName() + " doesn't declare a string representation (no CrudTable annotation present).");
			}
			
			return stringRep;
			
		} else if(returnType.equals(boolean.class) || returnType.equals(Boolean.class)) {
			if(property.getValue().equals(true)) {
				return Constants.uiYes;
			} else {
				return Constants.uiNo;
			}
		} else if(returnType.equals(Date.class)) {
			return Utils.dateToString((Date) property.getValue(), Utils.getDateFormatPattern());
			
		} else if(Collection.class.isAssignableFrom(returnType)) {
			return propertyObject.toString().replace("[", "").replace("]", "");
		}
		
		if(crudFieldAnnotation != null && crudFieldAnnotation.isPassword()) {
			String password = property.getValue().toString();
			String stringRep = "";
			
			if(password != null) {
				for(int i = 0; i < password.length(); i++) {
					stringRep += "*";
				}
			}
			
			return stringRep;
		}
				
		return null;
	}

}
