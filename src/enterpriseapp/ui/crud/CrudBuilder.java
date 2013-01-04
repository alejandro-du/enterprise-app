package enterpriseapp.ui.crud;


import com.vaadin.data.Container;
import com.vaadin.ui.DefaultFieldFactory;

import enterpriseapp.hibernate.dto.Dto;

/**
 * Helper class to create instances of CrudComponent.
 * 
 * @author Alejandro Duarte
 *
 * @param <T> Entity type.
 */
public class CrudBuilder<T extends Dto> {
	
	private Class<T> type;
	private Container container;
	private DefaultFieldFactory fieldFactory;
	private CrudTable<?> crudTable;
	private CrudForm<?> crudForm;
	private boolean showForm = true;
	private boolean showTable = true;
	private boolean showNewButton = true;
	private boolean showUpdateButton = true;
	private boolean showDeleteButton = true;
	private boolean editableTable = false;
	private boolean showTableButtons = false;
	private boolean verticalLayout = false;
	private int filtersPerRow = 0;
	
	/**
	 * @param type Entity type (class).
	 */
	public CrudBuilder(Class<T> type) {
		this.type = type;
	}
	
	/**
	 * @return a new CrudComponent with the current configuration.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public CrudComponent<T> build() {
		return new CrudComponent(
			type,
			container,
			fieldFactory,
			crudTable,
			crudForm,
			showForm,
			showTable,
			showNewButton,
			showUpdateButton,
			showDeleteButton,
			editableTable,
			showTableButtons,
			verticalLayout,
			filtersPerRow
		);
	}

	/**
	 * Use a custom container.
	 * @param container Container to use.
	 * @return
	 */
	public CrudBuilder<T> setContainer(Container container) {
		this.container = container;
		return this;
	}

	/**
	 * Use a custom FieldFactory.
	 * @param fieldFactory FieldFactory to use.
	 * @return
	 */
	public CrudBuilder<T> setFieldFactory(DefaultFieldFactory fieldFactory) {
		this.fieldFactory = fieldFactory;
		return this;
	}

	/**
	 * Use a custom CrudTable.
	 * @param crudTable CrudTable to use.
	 * @return
	 */
	public CrudBuilder<T> setCrudTable(CrudTable<?> crudTable) {
		this.crudTable = crudTable;
		return this;
	}

	/**
	 * Use a custom CrudForm.
	 * @param crudForm CrudForm to use.
	 * @return
	 */
	public CrudBuilder<T> setCrudForm(CrudForm<?> crudForm) {
		this.crudForm = crudForm;
		return this;
	}

	/**
	 * Show or hide CRUD form. If you want your CRUD to be readOnly, set crud.readOnly(true) in your crud instance.
	 * @param showForm
	 * @return
	 */
	public CrudBuilder<T> setShowForm(boolean showForm) {
		this.showForm = showForm;
		return this;
	}

	/**
	 * Show or hide CRUD table.
	 * @param showTable
	 * @return
	 */
	public CrudBuilder<T> setShowTable(boolean showTable) {
		this.showTable = showTable;
		return this;
	}

	/**
	 * Show or hide new button.
	 * @param showNewButton
	 * @return
	 */
	public CrudBuilder<T> setShowNewButton(boolean showNewButton) {
		this.showNewButton = showNewButton;
		return this;
	}

	/**
	 * Show or hide update button.
	 * @param showUpdateButton
	 * @return
	 */
	public CrudBuilder<T> setShowUpdateButton(boolean showUpdateButton) {
		this.showUpdateButton = showUpdateButton;
		return this;
	}

	/**
	 * Show or hide delete button.
	 * @param showDeleteButton
	 * @return
	 */
	public CrudBuilder<T> setShowDeleteButton(boolean showDeleteButton) {
		this.showDeleteButton = showDeleteButton;
		return this;
	}

	/**
	 * Make the CRUD table editable/no editable. If you want to make the table editable, it's very likely that you will need
	 * to use a custom Container or a custom CrudTable / EntityTable. Just making the table editable, normally won't allow
	 * you to add new entities to a DefaultHbnContainer unless all of your entity's fields are nullable.
	 * @param editableTable
	 * @return
	 */
	public CrudBuilder<T> setEditableTable(boolean editableTable) {
		this.editableTable = editableTable;
		return this;
	}

	/**
	 * Show or hide table buttons (when using an editable table).
	 * @param showTableButtons
	 * @return
	 */
	public CrudBuilder<T> setShowTableButtons(boolean showTableButtons) {
		this.showTableButtons = showTableButtons;
		return this;
	}

	/**
	 * Use a vertical / horizontal split layout.
	 * @param verticalLayout
	 * @return
	 */
	public CrudBuilder<T> setVerticalLayout(boolean verticalLayout) {
		this.verticalLayout = verticalLayout;
		return this;
	}

	/**
	 * Number of filters to show per row in the layout.
	 * @param filtersPerRow
	 * @return
	 */
	public CrudBuilder<T> setFiltersPerRow(int filtersPerRow) {
		this.filtersPerRow = filtersPerRow;
		return this;
	}

}
