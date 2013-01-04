package enterpriseapp.ui.crud;

import com.vaadin.ui.DefaultFieldFactory;

import enterpriseapp.hibernate.dto.Dto;

/**
 * A CrudComponent to use inside CrudComponent's forms. You can annotate your Dto with "@CrudField" and
 * specify embedded=true parameter in your -ToMany properties to automatically use this component in your
 * CRUDs.
 * 
 * @author Alejandro Duarte
 *
 * @param <T> Entity type.
 */
public class EmbeddedCrudComponent<T extends Dto> extends CrudComponent<T> {
	
	private static final long serialVersionUID = 1L;

	/**
	 * @param type Entity type.
	 */
	public EmbeddedCrudComponent(Class<T> type) {
		this(type, null, new EntitySetContainer<T>(type));
	}
	
	/**
	 * @param type Entity type.
	 * @param container EntitySetContainer to use.
	 */
	public EmbeddedCrudComponent(Class<T> type, EntitySetContainer<T> container) {
		this(type, null, container);
	}
	
	/**
	 * @param type Entity type.
	 * @param fieldFactory DefaultFieldFactory to use.
	 * @param container EntitySetContainer to use.
	 */
	public EmbeddedCrudComponent(Class<T> type, DefaultFieldFactory fieldFactory, EntitySetContainer<T> container) {
		super(type, container, fieldFactory, null, null, false, true, true, true, true, true, true, false, 0);
	}

}
