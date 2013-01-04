package enterpriseapp.ui.crud;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Window;

import enterpriseapp.ui.Constants;

public class ImportFromClipboardWindow extends Window implements TextChangeListener {

	private static final long serialVersionUID = 1L;
	
	protected String clipboardContent;
	
	public ImportFromClipboardWindow(String typeName, String columnsStringLabel) {
		super(Constants.uiImportFromClipboard);
		
		setResizable(false);
		setWidth("420px");
		
		TextArea textArea = new TextArea();
		textArea.addListener(this);
		textArea.setImmediate(true);
		textArea.setWriteThrough(true);
		textArea.setWidth("100%");
		
		addComponent(new Label(Constants.uiImportFromClipboardInstructions(columnsStringLabel), Label.CONTENT_XHTML));
		addComponent(textArea);
		
		textArea.focus();
	}

	@Override
	public void textChange(TextChangeEvent event) {
		clipboardContent = event.getText();
		this.close();
	}
	
	public String getClipboardContent() {
		return clipboardContent;
	}

}
