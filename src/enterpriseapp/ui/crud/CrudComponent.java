package enterpriseapp.ui.crud;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.BeanItem;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.AbstractSplitPanel;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.DateField;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.ChameleonTheme;
import com.vaadin.ui.themes.Reindeer;

import enterpriseapp.Utils;
import enterpriseapp.hibernate.ContainerFactory;
import enterpriseapp.hibernate.DefaultHbnContainer;
import enterpriseapp.hibernate.dto.Dto;
import enterpriseapp.ui.Constants;

/**
 * Full CRUD Vaadin component for Entity classes.
 * You can instanciate directly or use CrudBuilder.
 * 
 * @author Alejandro Duarte
 *
 * @param <T> Entity type.
 */
public class CrudComponent<T extends Dto> extends CustomComponent {

	private static final long serialVersionUID = 1L;
	
	private static Logger logger = LoggerFactory.getLogger(CrudComponent.class);

	protected final Class<T> type;
	protected CrudTable<T> table;
	protected CrudForm<T> form;
	protected Container container;
	protected VerticalLayout filterLayout;
	protected CrudListener<T> listener;
	protected boolean isHbnContainer;
	protected VerticalLayout tableLayout;
	protected VerticalLayout formLayout;
	protected HorizontalLayout navigatorLayout;
	protected AbstractSplitPanel split;
	protected int filterLayoutRows;
	
	/**
	 * Creates a new instance using a default configuration.
	 * @param type Entity type.
	 */
	public CrudComponent(Class<T> type) {
		this(type, null, null, null, null, true, true, true, true, true, false, false, false, 0);
	}
	
	/**
	 * Creates a new instance using the specified DefaultFieldFactory.
	 * @param type Entity type.
	 * @param fieldFactory DefaultFieldFactory to use.
	 */
	public CrudComponent(Class<T> type, DefaultFieldFactory fieldFactory) {
		this(type, null, fieldFactory, null, null, true, true, true, true, true, false, false, false, 0);
	}
	
	/**
	 * Creates a new instance using the specified Container.
	 * @param type Entity type.
	 * @param container Container to use.
	 */
	public CrudComponent(Class<T> type, Container container) {
		this(type, container, null, null, null, true, true, true, true, true, false, false, false, 0);
	}
	
	/**
	 * Creates a new instance using the specified DefaultFieldFactory and Container.
	 * @param type Entity type.
	 * @param fieldFactory DefaultFieldFactory to use.
	 * @param container Container to use.
	 */
	public CrudComponent(Class<T> type, DefaultFieldFactory fieldFactory, Container container) {
		this(type, container, fieldFactory, null, null, true, true, true, true, true, false, false, false, 0);
	}
	
	/**
	 * Creates a new instance using the specified DefaultFieldFactory and vertical layout configuration.
	 * @param type Entity type.
	 * @param fieldFactory FieldFactory to use.
	 * @param verticalLayout true to use a vertical split (table top, form below), false to use horizontal
	 * split (table on the left, form on the right).
	 */
	public CrudComponent(Class<T> type, DefaultFieldFactory fieldFactory, boolean verticalLayout) {
		this(type, null, fieldFactory, null, null, true, true, true, true, true, false, false, verticalLayout, 0);
	}
	
	/**
	 * Creates a new instance specifying full creation options.
	 * @param type Entity type. 
	 * @param container Container to use.
	 * @param fieldFactory DefaultFieldFactory to use.
	 * @param crudTable CrudTable to use.
	 * @param crudForm CrudForm to use.
	 * @param showForm Show or hide form.
	 * @param showTable Show or hide table.
	 * @param showNewButton Show or hide new button.
	 * @param showUpdateButton Show or hide update button.
	 * @param showDeleteButton Show or hide delete button.
	 * @param editableTable 
	 * Make the CRUD table editable/no editable. If you want to make the table editable, it's very likely that you will need
	 * to use a custom Container or a custom CrudTable / EntityTable. Just making the table editable, normally won't allow
	 * you to add new entities to a DefaultHbnContainer unless all of your entity's fields are nullable.
	 * @param showTableButtons Show or hide table buttons (when using an editable table).
	 * @param verticalLayout Use vertical / horizontal split layout.
	 * @param filtersPerRow Number of filters to show per row in the layout.
	 */
	public CrudComponent(
			Class<T> type,
			Container container,
			DefaultFieldFactory fieldFactory,
			CrudTable<T> crudTable,
			CrudForm<T> crudForm,
			boolean showForm,
			boolean showTable,
			boolean showNewButton,
			boolean showUpdateButton,
			boolean showDeleteButton,
			boolean editableTable,
			boolean showTableButtons,
			boolean verticalLayout,
			int filtersPerRow
	) {
		this.type = type;
		
		this.filterLayoutRows = filtersPerRow;
		
		if(container == null) {
			container = ContainerFactory.getInstance().getContainer(type);
		}
		
		this.container = container;
		isHbnContainer = container instanceof DefaultHbnContainer;
		
		filterLayout = new VerticalLayout();
		
		if(fieldFactory == null) {
			fieldFactory = new DefaultCrudFieldFactory();
		}
		
		if(crudTable == null) {
			table = new CrudTable<T>(type, container, fieldFactory);
		} else {
			table = crudTable;
		}
		
		if(crudForm == null) {
			form = new CrudForm<T>(type, fieldFactory);
		} else {
			form = crudForm;
		}
		
		listener = new CrudListener<T>(this);
		
		table.addListener((ValueChangeListener) listener);
		table.addListener((ItemClickListener) listener);
		table.addActionHandler(listener);
		
		form.newButton.addListener((ClickListener) listener);
		form.updateButton.addListener((ClickListener) listener);
		form.deleteButton.addListener((ClickListener) listener);
		form.saveButton.addListener((ClickListener) listener);
		form.cancelButton.addListener((ClickListener) listener);
		form.firstButton.addListener((ClickListener) listener);
		form.previousButton.addListener((ClickListener) listener);
		form.nextButton.addListener((ClickListener) listener);
		form.lastButton.addListener((ClickListener) listener);
		
		table.newButton.addListener((ClickListener) listener);
		table.deleteButton.addListener((ClickListener) listener);
		
		initLayout(showForm, showTable, showNewButton, showUpdateButton, showDeleteButton, editableTable, showTableButtons, verticalLayout);
	}
	
	protected void initLayout(boolean showForm, boolean showTable, boolean showNewButton, boolean showUpdateButton, boolean showDeleteButton, boolean editableTable, boolean showTableButtons, boolean verticalLayout) {
		
		table.setEditable(editableTable);
		form.setSizeFull();
		
		if(!showNewButton) {
			form.hideNewButton();
		}
		if(!showUpdateButton) {
			form.hideUpdateButton();
		}
		if(!showDeleteButton) {
			form.hideDeleteButton();
		}
		
		Panel formPanel = new Panel();
		formPanel.addComponent(form);
		formPanel.setStyleName(Reindeer.LAYOUT_BLUE);
		
		form.firstButton.setStyleName(Reindeer.BUTTON_SMALL);
		form.previousButton.setStyleName(Reindeer.BUTTON_SMALL);
		form.nextButton.setStyleName(Reindeer.BUTTON_SMALL);
		form.lastButton.setStyleName(Reindeer.BUTTON_SMALL);
		
		navigatorLayout = new HorizontalLayout();
		navigatorLayout.addComponent(form.firstButton);
		navigatorLayout.addComponent(form.previousButton);
		navigatorLayout.addComponent(form.nextButton);
		navigatorLayout.addComponent(form.lastButton);
		
		formLayout = new VerticalLayout();
		formLayout.setMargin(true);
		formLayout.addComponent(navigatorLayout);
		formLayout.addComponent(formPanel);
		
		filterLayout.setWidth("100%");
		
		tableLayout = new VerticalLayout();
		tableLayout.setSizeFull();
		
		if(showForm) {
			
			if(showTable) {
				if(verticalLayout) {
					split = new VerticalSplitPanel();
					split.setSplitPosition(60);
				} else {
					split = new HorizontalSplitPanel();
					split.setSplitPosition(66);
				}
				
				setCompositionRoot(split);
				
				split.setFirstComponent(tableLayout);
				split.setSecondComponent(formLayout);
			} else {
				setCompositionRoot(formPanel);
			}
			
		} else {
			if(showTableButtons) {
				HorizontalLayout buttons = new HorizontalLayout();
				buttons.setStyleName(Reindeer.BUTTON_SMALL);
				
				if(showNewButton) {
					buttons.addComponent(table.newButton);
				}
				if(showDeleteButton) {
					buttons.addComponent(table.deleteButton);
				}
				
				tableLayout.addComponent(buttons);
			}
			
			setCompositionRoot(tableLayout);
		}
		
		tableLayout.addComponent(table);
		tableLayout.setExpandRatio(table, 1);
		tableLayout.setMargin(true);
		configureTableColumns();
		
		if(isHbnContainer) {
			tableLayout.addComponent(filterLayout);
			
			if(filterLayoutRows > 1) {
				filterLayout.setVisible(false);
				Button toggleFilterButton = new Button(Constants.uiShowFilter);
				toggleFilterButton.setStyleName(ChameleonTheme.BUTTON_LINK);
				toggleFilterButton.addListener(new ClickListener() {
					private static final long serialVersionUID = 1L;
					@Override
					public void buttonClick(ClickEvent event) {
						filterLayout.setVisible(!filterLayout.isVisible());
						event.getButton().setCaption(filterLayout.isVisible() ? Constants.uiHideFilter : Constants.uiShowFilter);
					}
				});
				
				tableLayout.addComponent(toggleFilterButton);
				tableLayout.setComponentAlignment(toggleFilterButton, Alignment.TOP_RIGHT);
			}
		}
	}
	
	/**
	 * Shows the user the total amount of entityes in the container.
	 */
	public void showCount() {
		getApplication().getMainWindow().showNotification("" + table.getContainerDataSource().size() + " " + Constants.uiMatchesFound);
	}
	
	/**
	 * Removes the speciyied BeanItem from the container.
	 * @param item
	 */
	@SuppressWarnings("unchecked")
	public void remove(BeanItem<T> item) {
		if(item != null) {
			getContainer().removeItem(item.getItemProperty("id").getValue());
			form.setItemDataSource(null);
		} else if(table.getValue() != null) {
			Set<T> set = (Set<T>) table.getValue();
			
			if(!set.isEmpty()) {
				for(Object id: set) {
					table.removeFields(table.getItem(id));
					getContainer().removeItem(id);
				}
			}
		}
		table.updateTable();
	}
	
	/**
	 * Saves (if doesn't exists) or updates (if exists) the specified entity.
	 * @param dto Dto to save or update.
	 */
	public void saveOrUpdate(T dto) {
		getContainer().addItem(dto);
		table.updateTable();
		HashSet<Object> set = new HashSet<Object>();
		set.add(((Dto) dto).getId());
		table.setValue(set);
	}
	
	/**
	 * Sets up the visible table columns.
	 */
	public void configureTableColumns() {
		generateIdColumn();
		
		table.setColumnWidth("id", getIdColumnWidth());
		
		ArrayList<Object> visibleColumns = getTableVisibleColumns();
		
		int currentFilter = 0;
		int currentRow = 0;
		int filtersPerRow;
		
		if(filterLayoutRows == 0) {
			filterLayoutRows = (int) Math.ceil((double) visibleColumns.size() / 10.0);
		}
		
		filtersPerRow = (int) Math.ceil((double) visibleColumns.size() / filterLayoutRows);
		
		for(Object column : visibleColumns) {
			addFilter(column, currentRow);
			currentFilter++;
			
			if(currentFilter >= filtersPerRow) {
				currentRow++;
				currentFilter = 0;
			}
		}
		
		table.setVisibleColumns(visibleColumns.toArray());
	}
	
	/**
	 * Generates the ID column
	 */
	protected void generateIdColumn() {
		table.addGeneratedColumn("id", new ColumnGenerator() {
			private static final long serialVersionUID = 1L;
			@Override
			public Component generateCell(Table source, Object itemId, Object columnId) {
				Label label = new Label(itemId.toString());
				return label;
			}
		});
	}

	/**
	 * @return visible table columns accordingly to the CRUD configuration.
	 */
	public ArrayList<Object> getTableVisibleColumns() {
		Object[] allColumns = Utils.getVisibleTableProperties(type);
		
		if(allColumns == null) {
			allColumns = table.getVisibleColumns();
		}
		
		ArrayList<Object> visibleColumns = new ArrayList<Object>();
		
		for(Object column : allColumns) {
			if(table.isVisibleColumn(column)) {
				visibleColumns.add(column);
			}
			
		}
		return visibleColumns;
	}
	
	/**
	 * @return visible form fields accordingly to the CRUD configuration.
	 */
	public ArrayList<Object> getFormVisibleFields() {
		Object[] allFields = Utils.getVisibleFormProperties(type);
		
		if(allFields == null) {
			allFields = container.getContainerPropertyIds().toArray();
		}
		
		ArrayList<Object> visibleFields = new ArrayList<Object>();
		
		for(Object c : allFields) {
			if(!c.equals("id") || !form.isAutogeneratedId()) {
				visibleFields.add(c);
			}
		}
		
		return visibleFields;
	}
	
	/**
	 * Override this to provide a custom "id" table column width.
	 * @return the width of the "id" table column.
	 */
	public int getIdColumnWidth() {
		return -1;
	}

	/**
	 * Adds a new filter to the CRUD.
	 * @param column column to add the filter for.
	 * @param row filter layout row to add the filter fields in.
	 */
	public void addFilter(final Object column, int row) {
		
		VerticalLayout fieldFilterLayout = new VerticalLayout();
		HorizontalLayout fieldsLayout = new HorizontalLayout();
		HorizontalLayout optionsLayout = new HorizontalLayout();
		final TextField tf = new TextField();
		final TextField tf2 = new TextField();
		final CheckBox caseSensitiveCheckBox = new CheckBox(Constants.uiCaseSensitive, false);
		final CheckBox onlyMatchPrefixCheckBox = new CheckBox(Constants.uiOnlyMatchPrefix, false);
		
		caseSensitiveCheckBox.setImmediate(true);
		onlyMatchPrefixCheckBox.setImmediate(true);
		caseSensitiveCheckBox.setTabIndex(-1);
		onlyMatchPrefixCheckBox.setTabIndex(-1);
		
		String prompt = table.getColumnHeader(column);
		tf.setInputPrompt(prompt.substring(0, 1).toUpperCase() + prompt.substring(1, prompt.length()));
		tf.setWidth("100%");
		tf.setImmediate(true);
		tf.setNullRepresentation("");
		tf.setValue("");
		
		fieldsLayout.addComponent(tf);
		fieldsLayout.setWidth("100%");
		
		fieldFilterLayout.addComponent(fieldsLayout);
		fieldFilterLayout.addComponent(optionsLayout);
		fieldFilterLayout.setWidth("100%");
		
		HorizontalLayout horizontalLayout = null;
		
		if(row < filterLayout.getComponentCount()) {
			horizontalLayout = (HorizontalLayout) filterLayout.getComponent(row);
		}
		
		if(horizontalLayout == null) {
			horizontalLayout = new HorizontalLayout();
			horizontalLayout.setWidth("100%");
			filterLayout.addComponent(horizontalLayout);
		}
		
		horizontalLayout.addComponent(fieldFilterLayout);
		horizontalLayout.setExpandRatio(fieldFilterLayout, 1);
		
		ValueChangeListener valueChangeListener = new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				filter(column, tf, tf2, caseSensitiveCheckBox, onlyMatchPrefixCheckBox);
			}
		};
		
		Class<?> columnType = table.getContainerDataSource().getType(column);
		
		if(columnType.equals(Date.class)) {
			tf.setInputPrompt(table.getColumnHeader(column) + " (" + Constants.uiStarting + ")");
			tf2.setInputPrompt(table.getColumnHeader(column) + " (" + Constants.uiEnding + ")");
			tf2.setWidth("100%");
			tf2.setImmediate(true);
			tf2.setNullRepresentation("");
			tf2.setValue(null);
			fieldsLayout.addComponent(tf2);
			fieldsLayout.setExpandRatio(tf, 1f);
			fieldsLayout.setExpandRatio(tf2, 1f);
			tf2.addListener(valueChangeListener);
		} else if(String.class.isAssignableFrom(columnType) || Dto.class.isAssignableFrom(columnType)) {
			optionsLayout.addComponent(caseSensitiveCheckBox);
			optionsLayout.addComponent(onlyMatchPrefixCheckBox);
			optionsLayout.setSizeUndefined();
		}
		
		tf.addListener(valueChangeListener);
		caseSensitiveCheckBox.addListener(valueChangeListener);
		onlyMatchPrefixCheckBox.addListener(valueChangeListener);
	}

	/**
	 * Performs filtering.
	 * @param column Column to be filtered
	 * @param tf first TextField in the filter 
	 * @param tf2 second TextField in the filter (for dates)
	 * @param caseSensitiveCheckBox
	 * @param onlyMatchPrefixCheckBox
	 */
	@SuppressWarnings("unchecked")
	public void filter(Object column, TextField tf, TextField tf2, CheckBox caseSensitiveCheckBox, CheckBox onlyMatchPrefixCheckBox) {
		DefaultHbnContainer<T> container = ((DefaultHbnContainer<T>) table.getContainerDataSource());
		container.removeContainerFilters(column);
		
		if(tf != null && tf.toString() != null && !tf.toString().isEmpty() && !tf.toString().equals(column.toString())) {
			boolean ignoreCase = ! (Boolean) caseSensitiveCheckBox.getValue();
			boolean onlyMatchPrefix = (Boolean) onlyMatchPrefixCheckBox.getValue();
			container.addContainerFilter(column, tf.getValue().toString(), tf2.getValue() == null ? "" : tf2.getValue().toString(), ignoreCase, onlyMatchPrefix);
		}
		
		showCount();
	}
	
	/**
	 * Creates and shows the ImportFromClipboardWindow.
	 */
	public void showImportFromClipboardWindow() {
		final ImportFromClipboardWindow importWindow = new ImportFromClipboardWindow(type.getSimpleName(), getImportPropertiesLabel(type.getSimpleName()));
		importWindow.setModal(true);
		getApplication().getMainWindow().addWindow(importWindow);
		
		importWindow.addListener(new Window.CloseListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void windowClose(CloseEvent e) {
				importFromClipboard(importWindow.getClipboardContent(), true);
			}
		});
	}

	/**
	 * Exports shown tabla data to Excel.
	 */
	public void exportToExcel() {
		CrudExcelExport excelExport = new CrudExcelExport(table);
		excelExport.excludeCollapsedColumns();
		excelExport.setDisplayTotals(false);
		excelExport.setReportTitle(type.getSimpleName());
		excelExport.setExportFileName(type.getSimpleName().toLowerCase() + "-" + Utils.getCurrentTimeAndDate());
		excelExport.export();
	}
	
	/**
	 * Override this to provide your custom available import properties.
	 * @return the properties which the user can specify when importing data.
	 */
	public List<Object> getImportProperties() {
		return getFormVisibleFields();
	}
	
	/**
	 * Override this to provide your custom available import properties list.
	 * @return the properties which the user can specify when importing data.
	 */
	public int getShownImportPropertiesCount(String typeName) {
		return getImportPropertiesLabel(typeName).split(",").length;
	}
	
	/**
	 * Override this to provide your custom label with the available import properties.
	 * @return the help string which will to show in the ImportFromClipboardWindow.
	 */
	public String getImportPropertiesLabel(String typeName) {
		ArrayList<Object> formVisibleFields = getFormVisibleFields();
		String columnsString = "";
		
		listener.formNewButtonClicked();
		
		for(Object property : formVisibleFields) {
			Field field = form.getField(property);
			
			if(EntityTable.class.isAssignableFrom(field.getClass())) {
				EntityTable<?> entityTable = (EntityTable<?>) field;
				List<Object> entityTableImportProperties = entityTable.getCrudComponent().getImportProperties();
				String tableEntityTypeName = entityTable.getType().getSimpleName();
				
				for(Object p : entityTableImportProperties) {
					columnsString += Utils.getPropertyLabel(tableEntityTypeName, p) + ", ";
				}
				
			} else {
				columnsString += Utils.getPropertyLabel(typeName, property) + ", ";
			}
		}
		
		listener.cancelButtonClicked();
		
		return columnsString.substring(0, columnsString.length() - 2);
	}
	
	/**
	 * Override this to provide your custom values to import.
	 * @param property Property to get the value for.
	 * @param field Corresponding form field.
	 * @param stringValue Value introduced by the user.
	 * @param lineNumber Line numer being imported.
	 * @param line Line being imported.
	 * @return Value to import.
	 */
	public Object getValueToImport(Object property, Field field, String stringValue, int lineNumber, String line) {
		Object value = null;
		
		try {
			if(field instanceof DateField) {
				value = Utils.stringToDate(stringValue);
				
			} else if(EntityField.class.isAssignableFrom(field.getClass())) {
					
				EntityField entityField = (EntityField) field;
				value = entityField.getType().newInstance();
				
				if(stringValue != null && !stringValue.isEmpty()) {
					Class<?> idType = entityField.getType().getDeclaredField("id").getType();
					
					if(Long.class.isAssignableFrom(idType)) {
						entityField.getType().getDeclaredMethod("setId", Object.class).invoke(value, Long.parseLong(stringValue));
					} else {
						entityField.getType().getDeclaredMethod("setId", Object.class).invoke(value, stringValue);
					}
				}
				
			} else if(field.getClass().isAssignableFrom(EntitySetField.class)) {
				EntitySetField entitySetField = (EntitySetField) field;
				Object entity = entitySetField.getType().newInstance();
				
				if(stringValue != null && !stringValue.isEmpty()) {
					Class<?> idType = entitySetField.getType().getDeclaredField("id").getType();
					
					if(Long.class.isAssignableFrom(idType)) {
						entitySetField.getType().getDeclaredMethod("setId", Object.class).invoke(entity, Long.parseLong(stringValue));
					} else {
						entitySetField.getType().getDeclaredMethod("setId", Object.class).invoke(entity, stringValue);
					}
					
					HashSet<Object> set = new HashSet<Object>();
					set.add(entity);
					value = set;
				}
				
			} else if(Boolean.class.isAssignableFrom(field.getPropertyDataSource().getType())) {
				if(stringValue != null) {
					stringValue = Utils.toAscii(stringValue.toLowerCase());
					if(stringValue.equals(Utils.toAscii(Constants.uiYes.toLowerCase())) || stringValue.equals("1") || stringValue.equals("true")) {
						value = true;
					}
					else if(stringValue.equals(Constants.uiNo.toLowerCase()) || stringValue.equals("0") || stringValue.equals("false")) {
						value = false;
					}
				}
			} else {
				Constructor<?> constructor = field.getPropertyDataSource().getType().getConstructor(String.class);
				value = constructor.newInstance(stringValue);
			}
			
		} catch(Exception e) {
			logger.error("Error getting value to import." , e);
			value = stringValue;
		}
		
		return value;
	}
	
	/**
	 * Imports data from string.
	 * @param clipboardContent One register per line. Tab delimited values for properties.
	 */
	public void importFromClipboard(String clipboardContent, boolean showNotification) {
		if(clipboardContent == null || clipboardContent.isEmpty() || table.isReadOnly()) {
			return;
		}
		
		int shownImportPropertiesCount = getShownImportPropertiesCount(type.getSimpleName());
		List<Object> properties = getImportProperties();
		int lineNumber = 0;
		
		for(String line : clipboardContent.split("\n")) {
			try {
				line = line.trim();
				lineNumber++;
				
				if(line != null && !line.isEmpty()) {
					String[] values = line.split("\t");
					
					if(values.length != shownImportPropertiesCount) {
						getWindow().showNotification(Constants.uiError, Constants.uiImportFailedWrongColumnCount + " " + lineNumber + " (" + line + ").", Notification.TYPE_ERROR_MESSAGE);
						return;
					}
					
					listener.formNewButtonClicked();
					
					int iValue = 0;
					for(Object property : properties) {
						Field field = form.getField(property);
						
						if(EntityTable.class.isAssignableFrom(field.getClass())) {
							EntityTable<?> entityTable = (EntityTable<?>) field;
							List<Object> entityTableImportProperties = entityTable.getCrudComponent().getImportProperties();
							String entityTableClipBoardTable = "";
							
							for(int i = 0; i < entityTableImportProperties.size(); i++) {
								entityTableClipBoardTable += values[iValue] + "\t";
								iValue++;
							}
							
							entityTable.getCrudComponent().importFromClipboard(entityTableClipBoardTable, false);
							
						} else {
							String stringValue = values[iValue].trim();
							
							if(!stringValue.isEmpty()) {
								field.setValue(getValueToImport(property, field, stringValue, lineNumber, line));
							}
							
							iValue++;
						}
					}
					
					form.validate();
					
					if(!listener.formSaveButtonClicked(false)) {
						throw new InvalidValueException(form.getErrorMessage().toString());
					}
					
					listener.cancelButtonClicked();
				}
			} catch (InvalidValueException e) {
				getWindow().showNotification(Constants.uiError, Constants.uiImportFailed + " " + lineNumber + " (" + line + ")" + (e.getMessage() == null ? "." : ": " + e.getMessage()), Notification.TYPE_ERROR_MESSAGE);
				return;
			} catch (Exception e) {
				logger.error("Error processing value to import." , e);
				getWindow().showNotification(Constants.uiError, Constants.uiImportFailed + " " + lineNumber + " (" + line + ").", Notification.TYPE_ERROR_MESSAGE);
				return;
			}
		}
		
		if(showNotification) {
			getApplication().getMainWindow().showNotification(Constants.uiSaved);
		}
	}
	
	/**
	 * @return the container used by the CrudComponent.
	 */
	public Container getContainer() {
		return container;
	}
	
	/**
	 * Returns a new Container instance, using the ContainerFactory.
	 * @return a new instance of the container.
	 */
	public Container getNewInstanceOfContainer() {
		if(isHbnContainer) {
			return ContainerFactory.getInstance().getContainer(type);
		}
		
		return container;
	}
	
	/**
	 * @return CrudTable being used.
	 */
	public CrudTable<T> getTable() {
		return table;
	}
	
	/**
	 * @return CrudForm being used.
	 */
	public CrudForm<T> getForm() {
		return form;
	}

	/**
	 * @return Layout where the table has been added.
	 */
	public VerticalLayout getTableLayout() {
		return tableLayout;
	}

	/**
	 * @return Layout where the form has been added.
	 */
	public VerticalLayout getFormLayout() {
		return formLayout;
	}
	
	/**
	 * @return CRUD split panel.
	 */
	public AbstractSplitPanel getSplit() {
		return split;
	}

}
