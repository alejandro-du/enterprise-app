package enterpriseapp.ui.crud;

import java.util.List;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.themes.Reindeer;

import enterpriseapp.hibernate.ContainerFactory;
import enterpriseapp.hibernate.DefaultHbnContainer;
import enterpriseapp.ui.Constants;

/**
 * A field which allows selection of multiple Entity/Dto values.
 * 
 * @author Alejandro Duarte.
 *
 */
public class EntitySetField extends CustomField implements Button.ClickListener {

	private static final long serialVersionUID = 1L;
	
	protected final Class<?> type;
	protected HorizontalLayout layout;
	protected AbstractSelect select;
	protected Button refreshButton;
	protected final DefaultHbnContainer<?> container;
	
	/**
	 * @param type Entity/Dto type.
	 * @param container container to set for the field.
	 */
	public EntitySetField(Class<?> type, DefaultHbnContainer<?> container) {
		this.type = type;
		
		if(container == null) {
			this.container = ContainerFactory.getInstance().getContainer(type);
		} else {
			this.container = container;
		}
		
		initSelectComponent();
		select.setSizeFull();
		select.setImmediate(true);
		
		refreshButton = new Button(Constants.uiRefresh);
		refreshButton.setTabIndex(-1);
		refreshButton.setStyleName(Reindeer.BUTTON_SMALL);
		refreshButton.addListener((Button.ClickListener) this);
		
		layout = new HorizontalLayout();
		layout.setSizeFull();
		layout.setSpacing(true);
		layout.addComponent(select);
		layout.setExpandRatio(select, 1);
		layout.addComponent(refreshButton);
		
		update();
	}

	@Override
	protected Component initContent() {
		return layout;
	}
	
	protected void initSelectComponent() {
		select = new ListSelect();
		select.setMultiSelect(true);
		select.setNullSelectionAllowed(true);
	}
	
	@Override
	public void attach() {
		super.attach();
		if(CrudTable.class.isAssignableFrom(getParent().getClass())) {
			refreshButton.setVisible(false);
		}
	}
	
	/**
	 * Updates field items.
	 */
	public void update() {
		select.removeAllItems();
		List<?> list = container.listAll();
		
		for(Object o : list) {
			String stringRep = o.toString();
			
			select.addItem(o);
			select.setItemCaption(o, stringRep);
		}
	}
	
	@Override
	public Class<?> getType() {
		return type;
	}
	
	@Override
	public void setValue(Object newValue) {
		select.setValue(newValue);
	}
	
	@Override
	public Object getValue() {
		return select.getValue();
	}
	
	@Override
	public void setPropertyDataSource(Property newDataSource) {
		select.setPropertyDataSource(newDataSource);
	}
	
	@Override
	public boolean isValid() {
		return select.isValid();
	}

	@Override
	public void validate() throws Validator.InvalidValueException {
		select.validate();
	}
	
	@Override
	public void setReadOnly(boolean readOnly) {
		select.setReadOnly(readOnly);
		refreshButton.setVisible(!readOnly);
	}
	
    @Override
	public void buttonClick(ClickEvent event) {
		update();
	}
    
    @Override
    public boolean isRequired() {
    	return select.isRequired();
    }
    
    @Override
    public void setRequired(boolean required) {
    	select.setRequired(required);
    }
    
    @Override
    public String getRequiredError() {
    	return select.getRequiredError();
    }
    
    @Override
    public void setRequiredError(String requiredMessage) {
    	select.setRequiredError(requiredMessage);
    }
    
    @Override
    public void addListener(Property.ValueChangeListener listener) {
    	select.addListener(listener);
    }
    
    @Override
    public void addStyleName(String style) {
    	select.addStyleName(style);
    }
    
    @Override
    public void setStyleName(String style) {
    	select.setStyleName(style);
    }

    /**
     * @return layout where the field is presented.
     */
	public HorizontalLayout getLayout() {
		return layout;
	}

	/**
	 * @return select field.
	 */
	public AbstractSelect getSelect() {
		return select;
	}

}
