package enterpriseapp.ui.crud;

import java.util.Collection;
import java.util.HashSet;

import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;

import enterpriseapp.hibernate.dto.Dto;

public class EntityTable<T extends Dto> extends CustomField<Collection<T>> {

	private static final long serialVersionUID = 1L;
	
	protected EmbeddedCrudComponent<T> crudComponent;
	protected final Class<T> type;
	protected Collection<T> set;
	protected BeanContainer<Long, T> container;
	
	@SuppressWarnings("unchecked")
	public EntityTable(final Class<T> type, Collection<T> set, EmbeddedCrudComponent<T> crudComponent) {
		this.type = type;
		this.set = set;
		this.crudComponent = crudComponent;
		
		container = (BeanContainer<Long, T>) crudComponent.getContainer();
		container.setBeanIdProperty("id");
		container.addAll(set);
		
		if(set == null) {
			this.set = new HashSet<T>();
		}
		
		crudComponent.getTable().setPageLength(0);
		crudComponent.getTable().setColumnCollapsed("id", true);
		crudComponent.getTable().setValidationVisible(false);
		crudComponent.getTableLayout().setMargin(false);
	}

	@Override
	protected Component initContent() {
		return crudComponent;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<Collection<T>> getType() {
		return (Class<Collection<T>>) set.getClass();
	}
	
	@Override
	public void setInternalValue(Collection<T> newValue) {
		super.setInternalValue(newValue);
		container.removeAllItems();
		container.addAll(newValue);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Collection<T> getInternalValue() {
		set.clear();
		
		for(Object itemId : container.getItemIds()) {
			T bean = null;
			
			if(Dto.class.isAssignableFrom(itemId.getClass())) {
				bean = (T) itemId;
			} else {
				bean = container.getItem(itemId).getBean();
			}
			
			if(bean != null) {
				set.add(bean);
			}
		}
		
		return set;
	}
	
	@Override
	public void setReadOnly(boolean readOnly) {
		super.setReadOnly(readOnly);
		crudComponent.getTable().removeAllFields();
		crudComponent.getTable().setEditable(!readOnly);
		crudComponent.getTable().setReadOnly(readOnly);
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
