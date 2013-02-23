package enterpriseapp.ui.reports;

import java.io.UnsupportedEncodingException;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.Reindeer;

/**
 * Extend this template class to create nice print view custom reports.
 * @author Alejandro Duarte
 *
 */
public abstract class PrintViewReport extends AbstractReport {

	private static final long serialVersionUID = 1L;
	
	protected Label htmlLabel = new Label("", Label.CONTENT_XHTML);
	
	@Override
	public void initLayout() {
		super.initLayout();
		
		htmlLabel.setStyleName(Reindeer.LAYOUT_WHITE);
		htmlLabel.setSizeUndefined();
		
		leftLayout.setStyleName(Reindeer.LAYOUT_BLACK);
		leftLayout.addStyleName("report-background");
		leftLayout.addComponent(htmlLabel);
		leftLayout.setComponentAlignment(htmlLabel, Alignment.TOP_CENTER);
	}
	
	@Override
	public void updateReport() throws UnsupportedEncodingException {
		htmlLabel.setValue(getOutputStream(getHtmlExporter()).toString("UTF-8"));
	}
	
}
