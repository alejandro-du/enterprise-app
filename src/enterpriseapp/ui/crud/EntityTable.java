package enterpriseapp.ui.crud;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.vaadin.addon.customfield.CustomField;

import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.BeanContainer;

import enterpriseapp.hibernate.dto.Dto;

public class EntityTable<T extends Dto> extends CustomField {

	private static final long serialVersionUID = 1L;
	
	protected EmbeddedCrudComponent<T> crudComponent;
	protected final Class<T> type;
	protected BeanContainer<Long, T> container;
	
	@SuppressWarnings("unchecked")
	public EntityTable(final Class<T> type, Collection<T> set, EmbeddedCrudComponent<T> crudComponent) {
		this.type = type;
		this.crudComponent = crudComponent;
		
		container = (BeanContainer<Long, T>) crudComponent.getContainer();
		container.setBeanIdProperty("id");
		
		if(set != null) {
			container.addAll(set);
		}
		
		crudComponent.getTable().setPageLength(0);
		crudComponent.getTable().setColumnCollapsed("id", true);
		crudComponent.getTable().setValidationVisible(false);
		crudComponent.getTableLayout().setMargin(false);
		
		setCompositionRoot(crudComponent);
	}

	@Override
	public Class<?> getType() {
		return type;
	}
	
	@Override
	public void setReadOnly(boolean readOnly) {
		super.setReadOnly(readOnly);
		crudComponent.getTable().removeAllFields();
		crudComponent.getTable().setEditable(!readOnly);
		crudComponent.getTable().setReadOnly(readOnly);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object getValue() {
		Set<T> set = new HashSet<T>();
		
		for(Object itemId : container.getItemIds()) {
			Dto dto = null;
			
			if(Dto.class.isAssignableFrom(itemId.getClass())) {
				dto = (Dto) itemId;
			} else {
				dto = (Dto) container.getItem(itemId).getBean();
			}
			
			
			if(dto != null) {
				set.add((T) dto);
			}
		}
		
		return set;
	}
	
    @Override
	protected boolean isEmpty() {
        return (getValue() == null || ((Collection<?>) getValue()).isEmpty());
    }
    
	@Override
	public boolean isValid() {
		try {
			validate();
		} catch(InvalidValueException e) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public void validate() throws Validator.InvalidValueException {
		try {
			setComponentError(null);
			super.validate();
			crudComponent.getTable().validate();
		} catch(InvalidValueException e) {
			throw new InvalidValueException(this.getCaption() + " - " + e.getMessage());
		}
	}

	public CrudComponent<T> getCrudComponent() {
		return crudComponent;
	}

	public BeanContainer<Long, T> getContainer() {
		return container;
	}
	
}
