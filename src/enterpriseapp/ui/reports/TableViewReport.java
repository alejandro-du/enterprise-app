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
		table.setColumnCollapsingAllowed(true);
		table.setSelectable(true);
		
		leftLayout.setSizeFull();
		leftLayout.addComponent(table);
		
		columnsButton.setVisible(false);
		groupingButton.setVisible(false);
	}
	
	@Override
	public void updateReport() {
		table.removeAllItems();
		
		String[] columnProperties = getColumnProperties();
		Class<?>[] columnClasses = getColumnClasses();
		String[] columnTitles = getColumnTitles();
		
		for(int i = 0; i < columnProperties.length; i++) {
			table.addContainerProperty(columnProperties[i], columnClasses[i], null, columnTitles[i], null, null);
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
