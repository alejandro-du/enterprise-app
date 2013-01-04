package enterpriseapp.ui.crud;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.exception.ConstraintViolationException;
import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.BeanItem;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.ShortcutAction;
import com.vaadin.terminal.UserError;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

import enterpriseapp.Utils;
import enterpriseapp.hibernate.CustomHbnContainer.EntityItem;
import enterpriseapp.hibernate.dto.Dto;
import enterpriseapp.hibernate.exception.CrudException;
import enterpriseapp.ui.Constants;

/**
 * This class listen to CRUD events and performs actions on CrudComponent.
 * 
 * @author Alejandro Duarte
 *
 * @param <T> Entity class.
 */
public class CrudListener<T extends Dto> implements ValueChangeListener, ItemClickListener, ClickListener, Handler {

	private static final long serialVersionUID = 1L;
	
	public final Action ACTION_REFRESH = new Action(Constants.uiRefresh);
	public final Action ACTION_EXPORT_TO_EXCEL = new Action(Constants.uiExportToExcel);
	public final ShortcutAction ACTION_IMPORT_FROM_CLIPBOARD = new ShortcutAction(Constants.uiImportFromClipboard + "", ShortcutAction.KeyCode.V, new int[] {ShortcutAction.ModifierKey.CTRL});
	public final Action ACTION_SHOW_COUNT = new Action(Constants.uiShowCount);
	
	protected CrudComponent<T> crudComponent;
	
	/**
	 * @param crudComponent CrudComponent to listen to.
	 */
	public CrudListener(CrudComponent<T> crudComponent) {
		this.crudComponent = crudComponent;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void valueChange(ValueChangeEvent event) {
		Set<T> set = (Set<T>) crudComponent.table.getValue();
		
		if(set.size() == 0) {
			crudComponent.form.setItemDataSource(null);
		} else if(set.size() == 1) {
			Item item = crudComponent.getNewInstanceOfContainer().getItem(set.iterator().next());
			
			if(item instanceof EntityItem) {
				EntityItem entityItem = (EntityItem) item;
				T pojo = (T) entityItem.getPojo();
				item = new BeanItem<T>(pojo);
			}
			
			crudComponent.form.setItemDataSource(item);
			crudComponent.form.setReadOnly(true);
			
		} else {
			crudComponent.form.setItemDataSource(null);
			crudComponent.form.setReadOnly(true);
			crudComponent.form.deleteButton.setVisible(true);
		}
		
		crudComponent.table.refreshRowCache();
	}
	
	@Override
	public void itemClick(ItemClickEvent event) {
		if(event.isDoubleClick() && !crudComponent.isReadOnly()) {
			crudComponent.table.setValue(event.getItemId());
			formUpdateButtonClicked();
		}
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		if(event.getButton().equals(crudComponent.form.newButton)) {
			formNewButtonClicked();
			
		} else if(event.getButton().equals(crudComponent.table.newButton)) {
			tableNewButtonClicked();
			
		} else if(event.getButton().equals(crudComponent.form.updateButton)) {
			formUpdateButtonClicked();
			
		} else if(event.getButton().equals(crudComponent.form.deleteButton)) {
			formDeleteButtonClicked();
			
		} else if(event.getButton().equals(crudComponent.table.deleteButton)) {
			tableDeleteButtonClicked();
			
		} else if(event.getButton().equals(crudComponent.form.saveButton)) {
			formSaveButtonClicked(true);
			
		} else if(event.getButton().equals(crudComponent.form.cancelButton)) {
			cancelButtonClicked();
			
		} else if(event.getButton().equals(crudComponent.form.firstButton)) {
			firstButtonClicked();
			
		} else if(event.getButton().equals(crudComponent.form.previousButton)) {
			previousButtonClicked();
			
		} else if(event.getButton().equals(crudComponent.form.nextButton)) {
			nextButtonClicked();
			
		} else if(event.getButton().equals(crudComponent.form.lastButton)) {
			lastButtonClicked();
			
		}
	}
	
	@Override
	public Action[] getActions(Object target, Object sender) {
		
		Action[] actions;
		
		if(crudComponent.isReadOnly()) {
			actions = new Action[] {
				ACTION_REFRESH,
				ACTION_EXPORT_TO_EXCEL,
				ACTION_SHOW_COUNT
			};
		} else {
			actions = new Action[] {
				ACTION_REFRESH,
				ACTION_IMPORT_FROM_CLIPBOARD,
				ACTION_EXPORT_TO_EXCEL,
				ACTION_SHOW_COUNT
			};
		}
		
		return actions;
	}

	@Override
	public void handleAction(Action action, Object sender, Object target) {
		if(action == ACTION_REFRESH) {
			crudComponent.table.updateTable();
		} else if(action == ACTION_EXPORT_TO_EXCEL) {
			crudComponent.exportToExcel();
		} else if(action == ACTION_IMPORT_FROM_CLIPBOARD) {
			if(!crudComponent.getTable().isReadOnly()) {
				crudComponent.showImportFromClipboardWindow();
			}
		} else if(action == ACTION_SHOW_COUNT) {
			crudComponent.showCount();
		}
	}

	/**
	 * Called when Form's "new" button is clicked. 
	 */
	public void formNewButtonClicked() {
		try {
			crudComponent.table.setValue(null);
			crudComponent.form.setItemDataSource(new BeanItem<T>(crudComponent.type.newInstance()));
			crudComponent.form.setReadOnly(false);
			
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Called when Table's "new" button is clicked. 
	 */
	public void tableNewButtonClicked() {
		crudComponent.getContainer().addItem(null);
	}
	
	/**
	 * Called when the Form's "delete" button is clicked.
	 */
	public void formDeleteButtonClicked() {
		Utils.yesNoDialog(crudComponent, Constants.uiConfirmDeletion, new ConfirmDialog.Listener() {
			
			public void onClose(ConfirmDialog dialog) {
				try {
					if(dialog.isConfirmed()) {
						crudComponent.remove(crudComponent.form.getItemDataSource());
						crudComponent.getApplication().getMainWindow().showNotification(Constants.uiDeleted);
						cancelButtonClicked();
					}
				} catch(ConstraintViolationException e) {
					crudComponent.form.setComponentError(new UserError(Constants.uiConstraintViolationErrorOnDelete));
				} catch(CrudException e) {
					crudComponent.form.setComponentError(new UserError(e.getMessage()));
				}
			}
			
		});
	}
	
	/**
	 * Called when Table's "delete" button is clicked. 
	 */
	public void tableDeleteButtonClicked() {
		crudComponent.remove(null);
	}
	
	/**
	 * Called when the Form's "save" button is clicked.
	 * @param showNotification if true, a notification will be shown to the user.
	 * @return true if the value is saved.
	 */
	public boolean formSaveButtonClicked(boolean showNotification) {
		try {
			crudComponent.form.setComponentError(null);
			crudComponent.form.commit();
			crudComponent.saveOrUpdate((T) crudComponent.form.getItemDataSource().getBean());
			crudComponent.form.setReadOnly(true);
			
			if(showNotification) {
				crudComponent.getApplication().getMainWindow().showNotification(Constants.uiSaved);
			}
		} catch(InvalidValueException e) {
			return false;
		} catch(ConstraintViolationException e) {
			crudComponent.form.setComponentError(new UserError(Constants.uiConstraintViolationErrorOnSave));
			return false;
		} catch(CrudException e) {
			crudComponent.form.setComponentError(new UserError(e.getMessage()));
			return false;
		}
		return true;
	}
	
	/**
	 * Called when Form's "cancel" button is clicked. 
	 */
	public void cancelButtonClicked() {
		crudComponent.form.discard();
		if(crudComponent.form != null && crudComponent.form.getItemDataSource() != null) {
			T bean = crudComponent.form.getItemDataSource().getBean();
			
			if(bean == null || bean.getId() == null) {
				crudComponent.form.setItemDataSource(null);
			}
		}
		
		crudComponent.form.setReadOnly(true);
	}
	
	public void firstButtonClicked() {
		HashSet<Object> value = new HashSet<Object>();
		value.add(crudComponent.table.firstItemId());
		crudComponent.table.setValue(value);
	}
	
	@SuppressWarnings("rawtypes")
	public void previousButtonClicked() {
		Set set = (Set) crudComponent.table.getValue();
		
		if(set != null && !set.isEmpty()) {
			Object id = set.iterator().next();
			Object prevItemId = crudComponent.table.prevItemId(id);
			prevItemId = crudComponent.table.prevItemId(id); // needed, wierd bug
			
			if(prevItemId != null) {
				HashSet<Object> value = new HashSet<Object>();
				value.add(prevItemId);
				crudComponent.table.setValue(value);
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	public void nextButtonClicked() {
		Set set = (Set) crudComponent.table.getValue();
		
		if(set != null && !set.isEmpty()) {
			Object id = set.iterator().next();
			Object nextItemId = crudComponent.table.nextItemId(id);
			nextItemId = crudComponent.table.nextItemId(id); // needed, wierd bug
			
			if(nextItemId != null) {
				HashSet<Object> value = new HashSet<Object>();
				value.add(nextItemId);
				crudComponent.table.setValue(value);
			}
		}
	}
	
	public void lastButtonClicked() {
		HashSet<Object> value = new HashSet<Object>();
		value.add(crudComponent.table.lastItemId());
		crudComponent.table.setValue(value);
	}
	
	/**
	 * Called when Form's "update" button is clicked. 
	 */
	public void formUpdateButtonClicked() {
		if(!crudComponent.getForm().hideUpdateButton) {
			crudComponent.form.setReadOnly(false);
		}
	}
	
}
