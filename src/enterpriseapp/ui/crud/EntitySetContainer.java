package enterpriseapp.ui.crud;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanContainer;

import enterpriseapp.hibernate.dto.Dto;

/**
 * Container of Dto objects.
 * 
 * @author Alejandro Duarte.
 *
 * @param <T> Dto concrete class.
 */
public class EntitySetContainer<T extends Dto> extends BeanContainer<Dto, T> {

	private static final long serialVersionUID = 1L;
	
	protected final Class<T> type;
	
	public EntitySetContainer(Class<T> type) {
		super(type);
		this.type = type;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Item addItem(Object itemId) {
		Dto dto = null;
		
		try {
			if(itemId == null) {
				dto = (Dto) type.newInstance();
			} else {
				dto = (Dto) itemId;
			}
			
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		
		return addItem(dto, (T) dto);
	}
}
