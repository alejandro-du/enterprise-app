package enterpriseapp.ui;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.FilesystemContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FileResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

import enterpriseapp.EnterpriseApplication;
import enterpriseapp.Utils;

public class FilesViewerComponent extends CustomComponent implements ClickListener, ValueChangeListener {

	private static final long serialVersionUID = 1L;
	
	protected Table filesTable;
	protected Table contentTable;
	protected FilesystemContainer fileContainer;
	
	protected Button deleteButton;
	protected Button downloadButton;
	
	public FilesViewerComponent(String directoryPath) {
		this(directoryPath, true, true);
	}

	public FilesViewerComponent(String directoryPath, boolean showDeleteButton, boolean showDownloadButton) {
		File directory = new File(directoryPath);
		
		if(!directory.exists()) {
			directory.mkdirs();
		}
		
		fileContainer = new FilesystemContainer(directory);
		
		filesTable = new Table();
		filesTable.setSizeFull();
		filesTable.setImmediate(true);
		filesTable.setSelectable(true);
		filesTable.setColumnReorderingAllowed(true);
		filesTable.setColumnCollapsingAllowed(true);
		filesTable.addListener((ValueChangeListener) this);
		updateTable();
		
		contentTable = new Table();
		contentTable.setSizeFull();
		contentTable.setSelectable(true);
		contentTable.setMultiSelect(true);
		
		VerticalLayout tableLayout = new VerticalLayout();
		tableLayout.setSizeFull();
		tableLayout.addComponent(contentTable);
		tableLayout.setExpandRatio(contentTable, 1f);
		
		deleteButton = new Button(Constants.uiDelete);
		deleteButton.setWidth("100%");
		deleteButton.setEnabled(false);
		deleteButton.addListener((ClickListener) this);
		
		downloadButton = new Button(Constants.uiDownloadFile);
		downloadButton.setWidth("100%");
		downloadButton.setEnabled(false);
		downloadButton.addListener((ClickListener) this);
		
		VerticalLayout buttonsLayout = new VerticalLayout();
		buttonsLayout.setWidth("100px");
		buttonsLayout.setSpacing(true);
		
		if(showDeleteButton) {
			buttonsLayout.addComponent(deleteButton);
		}
		
		if(showDownloadButton) {
			buttonsLayout.addComponent(downloadButton);
		}
		
		VerticalLayout firstLayout = new VerticalLayout();
		firstLayout.setSizeFull();
		firstLayout.setMargin(true);
		firstLayout.addComponent(filesTable);
		firstLayout.setExpandRatio(filesTable, 1f);
		
		HorizontalLayout secondLayout = new HorizontalLayout();
		secondLayout.setSizeFull();
		secondLayout.setMargin(true);
		secondLayout.setSpacing(true);
		secondLayout.addComponent(tableLayout);
		secondLayout.addComponent(buttonsLayout);
		secondLayout.setExpandRatio(tableLayout, 1f);
		
		HorizontalSplitPanel panel = new HorizontalSplitPanel();
		panel.setSplitPosition(40);
		panel.setFirstComponent(firstLayout);
		panel.setSecondComponent(secondLayout);
		
		setCompositionRoot(panel);
	}

	private void updateTable() {
		filesTable.setContainerDataSource(fileContainer);
		filesTable.setVisibleColumns(new Object[] {FilesystemContainer.PROPERTY_NAME, FilesystemContainer.PROPERTY_SIZE, FilesystemContainer.PROPERTY_LASTMODIFIED});
		filesTable.setColumnHeaders(new String[] {Constants.uiName, Constants.uiSize, Constants.uiLastUpdate});
	}

	@Override
	public void buttonClick(ClickEvent event) {
		
		if(event.getButton().equals(downloadButton)) {
			FileResource resource = new FileResource((File) filesTable.getValue());
			EnterpriseApplication.getInstance().getMainWindow().open(resource, "", true);
			updateTable();
			
		} else if(event.getButton().equals(deleteButton)) {
			Utils.yesNoDialog(this, Constants.uiConfirmDeletion, new ConfirmDialog.Listener() {
				
				public void onClose(ConfirmDialog dialog) {
					if(dialog.isConfirmed()) {
						File file = (File) filesTable.getValue();
						file.delete();
						updateTable();
					}
				}
				
			});
		}
		
	}

	@Override
	public void valueChange(Property.ValueChangeEvent event) {
		IndexedContainer container = new IndexedContainer();
		File file = (File) filesTable.getValue();
		boolean enabled = file != null;
		deleteButton.setEnabled(enabled);
		downloadButton.setEnabled(enabled);
		
		if(enabled) {
			try {
				container.addContainerProperty("c", String.class, "");
				
				FileInputStream fis = new FileInputStream(file);
				DataInputStream dis = new DataInputStream(fis);
				BufferedReader br = new BufferedReader(new InputStreamReader(dis));
				String line;
				
				while ((line = br.readLine()) != null)   {
					Object id = container.addItem();
					container.getContainerProperty(id, "c").setValue(line);
				}
				
				dis.close();
				
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
		}
		
		contentTable.setContainerDataSource(container);
		contentTable.setSortDisabled(true);
		
		if(enabled) {
			contentTable.setColumnHeader("c", file.getName());
		}
	}

}
