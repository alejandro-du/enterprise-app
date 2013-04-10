package enterpriseapp.ui.reports;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.Query;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.transform.AliasToEntityMapResultTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutAction.ModifierKey;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Select;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Window.Notification;

import enterpriseapp.Utils;
import enterpriseapp.hibernate.Db;
import enterpriseapp.ui.Constants;

/**
 * A UI component to test HQL queries.
 * 
 * @author Alejandro Duarte
 *
 */
public class HqlQueryBrowser extends CustomComponent implements ClickListener, ItemClickListener, Handler {

	private static final long serialVersionUID = 1L;
	
	private static Logger logger = LoggerFactory.getLogger(HqlQueryBrowser.class);
	
	public final Action ACTION_EXPORT_TO_EXCEL = new Action(Constants.uiExportToExcel);
	
	protected HorizontalSplitPanel layout = new HorizontalSplitPanel();
	
	protected VerticalLayout leftLayout = new VerticalLayout();
	protected Tree tree = new Tree();
	
	protected VerticalSplitPanel rightLayout = new VerticalSplitPanel();
	protected HorizontalSplitPanel querySplit = new HorizontalSplitPanel();
	protected VerticalLayout queryLayout = new VerticalLayout();
	protected HorizontalLayout queryActionsLayout = new HorizontalLayout();
	protected Label maxResultsLabel = new Label(Constants.uiMaxResults);
	protected TextField maxResultsTextField = new TextField();
	protected Button executeQueryButton = new Button(Constants.uiExecute);
	protected Button clearQueryButton = new Button(Constants.uiClear);
	protected Label queryLabel = new Label(Constants.uiHqlQuery + ":");
	protected TextArea queryTextArea = new TextArea();
	
	protected VerticalLayout paramsLayout = new VerticalLayout();
	protected Label paramsLabel = new Label(Constants.uiParameters);
	protected VerticalLayout paramsFieldsLayout = new VerticalLayout();
	protected HorizontalLayout paramsActionsLayout = new HorizontalLayout();
	protected Button addParamButton = new Button(Constants.uiAdd);
	protected Button deleteParamButton = new Button(Constants.uiDelete);
	protected ArrayList<TextField> paramsValues = new ArrayList<TextField>();
	protected ArrayList<Select> paramsTypes = new ArrayList<Select>();
	
	protected VerticalLayout tableLayout = new VerticalLayout();
	protected Table table;
	
	protected Resource databaseIcon;
	protected Resource entityIcon;
	protected Resource columnIcon;
	
	public HqlQueryBrowser() {
		this(null, null, null);
	}
	
	public HqlQueryBrowser(Resource databaseIcon, Resource entityIcon, Resource columnIcon) {
		this.databaseIcon = databaseIcon;
		this.entityIcon = entityIcon;
		this.columnIcon = columnIcon;
		
		leftLayout.setMargin(true);
		leftLayout.addComponent(tree);
		
		tree.addItem(0);
		tree.setItemCaption(0, Constants.dbPersistenceUnit());
		tree.expandItem(0);
		tree.setItemIcon(0, databaseIcon);
		
		Map<String, ClassMetadata> classMetadata = Db.getAllClassMetadata();
		int entityId = 1;
		int propertyId = -1;
		
		for(Entry<String, ClassMetadata> entry : classMetadata.entrySet()) {
			tree.addItem(entityId);
			final String entity = getEntityName(entry);
			tree.setItemCaption(entityId, entity);
			tree.setParent(entityId, 0);
			tree.addListener((ItemClickListener) this);
			tree.setItemIcon(entityId, entityIcon);
			
			tree.addItem(propertyId);
			tree.setItemCaption(propertyId, getIdName(entry));
			tree.setParent(propertyId, entityId);
			tree.setChildrenAllowed(propertyId, false);
			tree.setItemIcon(propertyId, columnIcon);
			
			propertyId--;
			
			for(String propertyName : entry.getValue().getPropertyNames()) {
				tree.addItem(propertyId);
				tree.setItemCaption(propertyId, getPropertyName(entry, propertyName));
				tree.setParent(propertyId, entityId);
				tree.setChildrenAllowed(propertyId, false);
				tree.setItemIcon(propertyId, columnIcon);
				
				propertyId--;
			}
			
			entityId++;
		}
		
		queryTextArea.setSizeFull();
		queryTextArea.focus();
		
		executeQueryButton.addListener(this);
		clearQueryButton.addListener(this);
		addParamButton.addListener(this);
		deleteParamButton.addListener(this);
		
		executeQueryButton.setClickShortcut(KeyCode.ENTER, ModifierKey.CTRL);
		executeQueryButton.setDescription("Ctrl+Enter");
		
		maxResultsTextField.setValue("100");
		maxResultsTextField.setWidth("50px");
		
		queryActionsLayout.setSpacing(true);
		queryActionsLayout.addComponent(executeQueryButton);
		queryActionsLayout.addComponent(clearQueryButton);
		queryActionsLayout.addComponent(maxResultsLabel);
		queryActionsLayout.addComponent(maxResultsTextField);
		queryActionsLayout.addComponent(queryLabel);
		queryActionsLayout.setComponentAlignment(maxResultsLabel, Alignment.BOTTOM_LEFT);
		queryActionsLayout.setComponentAlignment(queryLabel, Alignment.BOTTOM_LEFT);
		
		queryLayout.setSizeFull();
		queryLayout.setMargin(true);
		queryLayout.setSpacing(true);
		queryLayout.addComponent(queryActionsLayout);
		queryLayout.addComponent(queryTextArea);
		queryLayout.setExpandRatio(queryTextArea, 1);
		
		paramsFieldsLayout.setSpacing(true);
		
		paramsActionsLayout.setSpacing(true);
		paramsActionsLayout.addComponent(addParamButton);
		paramsActionsLayout.addComponent(deleteParamButton);
		paramsActionsLayout.setComponentAlignment(addParamButton, Alignment.BOTTOM_RIGHT);
		paramsActionsLayout.setComponentAlignment(deleteParamButton, Alignment.BOTTOM_RIGHT);
		
		paramsLayout.setMargin(true);
		paramsLayout.setSpacing(true);
		paramsLayout.addComponent(paramsLabel);
		paramsLayout.addComponent(paramsFieldsLayout);
		paramsLayout.addComponent(paramsActionsLayout);
		paramsLayout.setExpandRatio(paramsFieldsLayout, 1);
		paramsLayout.setComponentAlignment(paramsActionsLayout, Alignment.BOTTOM_RIGHT);
		
		querySplit.setSplitPosition(60);
		querySplit.setFirstComponent(queryLayout);
		querySplit.setSecondComponent(paramsLayout);
		
		tableLayout.setSizeFull();
		
		rightLayout.setSplitPosition(40);		
		rightLayout.setFirstComponent(querySplit);
		rightLayout.setSecondComponent(tableLayout);
		
		layout.setSplitPosition(20);
		layout.setFirstComponent(leftLayout);
		layout.setSecondComponent(rightLayout);
		
		setCompositionRoot(layout);
	}

	private String getIdName(Entry<String, ClassMetadata> entry) {
		return entry.getValue().getIdentifierPropertyName() + " (" + entry.getValue().getIdentifierType().getReturnedClass().getSimpleName() + ")";
	}

	private String getPropertyName(Entry<String, ClassMetadata> entry, String propertyName) {
		return propertyName + " (" + entry.getValue().getPropertyType(propertyName).getReturnedClass().getSimpleName() + ")";
	}

	private String getEntityName(Entry<String, ClassMetadata> entry) {
		String name = null;
		
		try {
			name = Class.forName(entry.getValue().getEntityName()).getSimpleName();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return name;
	}
	
	protected void executeQuery() {
		try {
			if(queryTextArea.getValue() != null && !queryTextArea.getValue().toString().trim().isEmpty()) {
				Db.beginTransaction();
				
				Query query = Db.getCurrentSession().createQuery(queryTextArea.getValue().toString());
				query.setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE);
				setQueryParams(query);
				
				if(!maxResultsTextField.getValue().toString().isEmpty()) {
					query.setMaxResults(new Integer(maxResultsTextField.getValue().toString()));
				}
				
				showResult(query.list());
				
				Db.commitTransaction();
			}
			
		} catch(Exception e) {
			logger.debug("Error executing query", e);
			getApplication().getMainWindow().showNotification(Constants.uiError, e.getMessage(), Notification.TYPE_ERROR_MESSAGE);
		}
	}
	
	protected void setQueryParams(Query query) {
		int componentCount = paramsFieldsLayout.getComponentCount();
		
		for(int i = 0; i < componentCount; i++) {
			Field fieldType = paramsTypes.get(i);
			Field fieldValue = paramsValues.get(i);
			
			if(fieldType.getValue() == null) {
				throw new IllegalStateException(Constants.uiInvalidHqlParameterType); 	
			}
			
			if(fieldType.getValue().equals(BigDecimal.class)) {
				query.setBigDecimal(i, new BigDecimal(fieldValue.getValue().toString()));
				
			} else if(fieldType.getValue().equals(Boolean.class)) {
				query.setBoolean(i, new Boolean(fieldValue.getValue().toString()));
					
			} else if(fieldType.getValue().equals(Double.class)) {
				query.setDouble(i, new Double(fieldValue.getValue().toString()));
				
			} else if(fieldType.getValue().equals(Float.class)) {
				query.setFloat(i, new Float(fieldValue.getValue().toString()));
				
			} else if(fieldType.getValue().equals(Integer.class)) {
				query.setInteger(i, new Integer(fieldValue.getValue().toString()));
				
			} else if(fieldType.getValue().equals(Long.class)) {
				query.setLong(i, new Long(fieldValue.getValue().toString()));
				
			} else if(fieldType.getValue().equals(String.class)) {
				query.setString(i, fieldValue.getValue().toString());
				
			}
		}
	}

	protected void showResult(List<?> list) {
		resetTable();
		
		for(Object row : list) {
			addRow((Map<?, ?>) row);
		}
	}
	
	protected void addRow(Map<?, ?> row) {
		Object[] cells = new Object[row.size()];
		int i = row.entrySet().size() - 1;
		
		for(Entry<?, ?> entry : row.entrySet()) {
			table.addContainerProperty(entry.getKey(), Object.class, null);
			cells[i] = entry.getValue();
			i--;
		}
		
		table.addItem(cells, row);
	}
	
	protected void resetTable() {
		table = new Table();
		table.setSizeFull();
		table.setSelectable(true);
		table.addActionHandler(this);
		table.setColumnReorderingAllowed(true);
		table.setColumnCollapsingAllowed(true);
		tableLayout.removeAllComponents();
		tableLayout.addComponent(table);
	}
	
	protected void addParamField() {
		TextField textField = new TextField();
		textField.setWidth("100%");
		
		Select select = new Select();
		select.addItem(BigDecimal.class);
		select.addItem(Boolean.class);
		select.addItem(Double.class);
		select.addItem(Float.class);
		select.addItem(Integer.class);
		select.addItem(Long.class);
		select.addItem(String.class);
		
		HorizontalLayout fieldRowLayout = new HorizontalLayout();
		fieldRowLayout.setWidth("100%");
		fieldRowLayout.setSpacing(true);
		fieldRowLayout.addComponent(textField);
		fieldRowLayout.addComponent(select);
		fieldRowLayout.setExpandRatio(textField, 1);
		
		paramsFieldsLayout.addComponent(fieldRowLayout);
		paramsValues.add(textField);
		paramsTypes.add(select);
	}
	
	protected void deleteLastParamField() {
		int componentCount = paramsFieldsLayout.getComponentCount();
		
		if(componentCount > 0) {
			paramsFieldsLayout.removeComponent(paramsFieldsLayout.getComponent(componentCount - 1));
			paramsValues.remove(componentCount - 1);
			paramsTypes.remove(componentCount - 1);
		}
	}

	@Override
	public void buttonClick(ClickEvent event) {
		if(event.getButton().equals(executeQueryButton)) {
			executeQuery();
			queryTextArea.focus();
			
		} else if(event.getButton().equals(clearQueryButton)) {
			queryTextArea.setValue("");
			queryTextArea.focus();
			
		} else if(event.getButton().equals(addParamButton)) {
			addParamField();
			
		} else if(event.getButton().equals(deleteParamButton)) {
			deleteLastParamField();
		}
	}
	
	private void exportToExcel() {
		ExcelExport excelExport = new ExcelExport(table);
		excelExport.excludeCollapsedColumns();
		excelExport.setDisplayTotals(false);
		excelExport.setReportTitle(Constants.uiHqlQuery);
		excelExport.setExportFileName(Constants.uiHqlQuery.toLowerCase().replace(" ", "-") + "-" + Utils.getCurrentTimeAndDate());
		excelExport.export();
	}
	
	@Override
	public void itemClick(ItemClickEvent event) {
		Integer itemId = (Integer) event.getItemId();
		
		if(itemId > 0) {
			queryTextArea.setValue("" + queryTextArea.getValue() + tree.getItemCaption(itemId));
		} else if(itemId < 0) {
			String property = tree.getItemCaption(itemId);
			property = property.substring(0, property.lastIndexOf(" ("));
			queryTextArea.setValue("" + queryTextArea.getValue() + property);
		}
		
		queryTextArea.focus();
	}

	public Table getTable() {
		return table;
	}

	@Override
	public Action[] getActions(Object target, Object sender) {
		return new Action[] {
			ACTION_EXPORT_TO_EXCEL
		};
	}

	@Override
	public void handleAction(Action action, Object sender, Object target) {
		if(action == ACTION_EXPORT_TO_EXCEL) {
			exportToExcel();
		}
	}

}
