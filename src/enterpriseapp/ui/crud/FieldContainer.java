package enterpriseapp.ui.crud;

import com.vaadin.data.Item;
import com.vaadin.ui.Field;

/**
 * Allows getting and adding fields into a component.
 * 
 * @author Alejandro Duarte.
 *
 */
public interface FieldContainer {
	
	/**
	 * Adds a new field.
	 * @param field field to add.
	 * @param propertyId proeprty name.
	 * @param item item to which the field belongs.
	 */
	void addField(Field field, Object propertyId, Item item);
	
	/**
	 * Returns a field.
	 * @param propertyId property name for the field to get.
	 * @param item item to which the field belongs.
	 * @return the field for the specified property and item.
	 */
	Field getField(Object propertyId, Item item);

}
