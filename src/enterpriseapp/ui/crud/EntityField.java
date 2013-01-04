package enterpriseapp.ui.crud;

import com.vaadin.ui.Button;
import com.vaadin.ui.Select;

import enterpriseapp.hibernate.DefaultHbnContainer;

/**
 * A field which allows selection of a single Entity/Dto value.
 * 
 * @author Alejandro Duarte.
 *
 */
public class EntityField extends EntitySetField implements Button.ClickListener {

	private static final long serialVersionUID = 1L;
	
	/**
	 * @param type Entity/Dto type.
	 * @param container container to set for the field.
	 */
	public EntityField(Class<?> type, DefaultHbnContainer<?> container) {
		super(type, container);
	}
	
	@Override
	protected void initSelectComponent() {
		select = new Select();
	}
	
}
