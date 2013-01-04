package enterpriseapp.ui.crud;

import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.data.Property;
import com.vaadin.ui.Table;

import enterpriseapp.hibernate.CustomHbnContainer.EntityItem;
import enterpriseapp.hibernate.dto.Dto;

/**
 * This class uses "TableExport" Vaadin add-on.
 * 
 * @author Alejandro Duarte
 *
 */
public class CrudExcelExport extends ExcelExport {

	private static final long serialVersionUID = 1L;

	/**
	 * @param table Table to export.
	 */
	public CrudExcelExport(Table table) {
		super(table);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void addDataRow(final Sheet sheetToAddTo, final Object rootItemId, final int row) {
		final Row sheetRow = sheetToAddTo.createRow(row);
		Property prop;
		Object value;
		Cell sheetCell;
		List<Object> propIds = getPropIds();
		for (int col = 0; col < propIds.size(); col++) {
			sheetCell = sheetRow.createCell(col);
			prop = getTable().getContainerDataSource().getContainerProperty(rootItemId, propIds.get(col));

			CrudTable crudTable = (CrudTable) getTable();
			
			if(propIds.get(col).equals("id")) {
				value = ((Dto) ((EntityItem) crudTable.getItem(rootItemId)).getPojo()).getId();
			} else {
				value = crudTable.formatPropertyValue(rootItemId, propIds.get(col), prop);
			}

			if (null != value) {
				sheetCell.setCellValue(createHelper.createRichTextString(value.toString()));
			}
		}
	}

}
