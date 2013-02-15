package enterpriseapp.ui.reports;

import java.util.Collection;

import org.apache.commons.beanutils.BasicDynaBean;

import com.vaadin.ui.Table;

/**
 * Extend this template class to create table based custom reports.
 * @author Alejandro Duarte
 *
 */
public abstract class TableViewReport extends AbstractReport {

	private static final long serialVersionUID = 1L;
	
	protected Table table = new Table();
	
	@Override
	public void initLayout() {
		super.initLayout();
		
		table.setSizeFull();
		table.setImmediate(true);
		table.setSelectable(true);
		table.setColumnCollapsingAllowed(true);
		
		leftLayout.setSizeFull();
		leftLayout.addComponent(table);
		
		groupingButton.setVisible(false);
	}
	
	@Override
	public void updateReport() {
		table.setContainerDataSource(null);
		
		String[] columnProperties = getColumnProperties();
		Class<?>[] columnClasses = getColumnClasses();
		String[] columnTitles = getColumnTitles();
		
		for(int i = 0; i < columnProperties.length; i++) {
			table.addContainerProperty(columnProperties[i], columnClasses[i], null, columnTitles[i], null, null);
		}
		
		for(int i = 0; i < columnProperties.length; i++) {
			if(!columnsCheckBoxes[i].booleanValue()) {
				table.setColumnCollapsed(columnProperties[i], true);
			}
		}
		
		Collection<?> data = getData();
		
		for(Object row : data) {
			BasicDynaBean bean = (BasicDynaBean) row;
			Object[] cells = new Object[columnProperties.length];
			
			for(int i = 0; i < columnProperties.length; i++) {
				cells[i] = bean.get(columnProperties[i]);
			}
			
			table.addItem(cells, row);
		}
	}

}
