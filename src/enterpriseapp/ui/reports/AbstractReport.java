package enterpriseapp.ui.reports;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRXhtmlExporter;
import net.sf.jasperreports.engine.export.JRXmlExporter;
import net.sf.jasperreports.engine.export.oasis.JROdsExporter;
import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRPptxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.j2ee.servlets.ImageServlet;

import org.vaadin.hene.popupbutton.PopupButton;
import org.vaadin.hene.splitbutton.SplitButton;
import org.vaadin.hene.splitbutton.SplitButton.SplitButtonClickEvent;
import org.vaadin.hene.splitbutton.SplitButton.SplitButtonClickListener;

import ar.com.fdvs.dj.core.DynamicJasperHelper;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.ColumnBuilder;
import ar.com.fdvs.dj.domain.builders.ColumnBuilderException;
import ar.com.fdvs.dj.domain.builders.DynamicReportBuilder;
import ar.com.fdvs.dj.domain.builders.FastReportBuilder;
import ar.com.fdvs.dj.domain.builders.GroupBuilder;
import ar.com.fdvs.dj.domain.builders.StyleBuilder;
import ar.com.fdvs.dj.domain.constants.Font;
import ar.com.fdvs.dj.domain.constants.GroupLayout;
import ar.com.fdvs.dj.domain.constants.HorizontalAlign;
import ar.com.fdvs.dj.domain.constants.Page;
import ar.com.fdvs.dj.domain.entities.columns.AbstractColumn;
import ar.com.fdvs.dj.domain.entities.columns.PropertyColumn;
import ar.com.fdvs.dj.domain.entities.conditionalStyle.ConditionalStyle;

import com.vaadin.terminal.StreamResource;
import com.vaadin.terminal.UserError;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

import enterpriseapp.Utils;
import enterpriseapp.ui.Constants;

/**
 * @author Alejandro Duarte
 *
 */
public abstract class AbstractReport extends CustomComponent implements ClickListener, SplitButtonClickListener {

	private static final long serialVersionUID = 1L;
	
	protected HorizontalSplitPanel layout;
	protected VerticalLayout leftLayout = new VerticalLayout();
	protected HorizontalLayout displayLayout;
	protected Button refreshButton;
	protected PopupButton columnsButton;
	protected PopupButton groupingButton;
	protected SplitButton exportButton;
	protected Button pdfButton;
	protected Button excelButton;
	protected Button wordButton;
	protected Button powerPointButton;
	protected Button odsButton;
	protected Button odtButton;
	protected Button rtfButton;
	protected Button htmlButton;
	protected Button csvButton;
	protected Button xmlButton;
	protected Accordion accordion;
	protected CheckBox printBackgroundOnOddRowsCheckBox;
	protected CheckBox printColumnNamesCheckBox;
	protected CheckBox stretchWithOverflowCheckBox;
	protected TextField columnsPerPageTextField;
	protected TextField pageWidthTextField;
	protected TextField pageHeightTextField;
	protected TextField marginTopTextField;
	protected TextField marginBottomTextField;
	protected TextField marginLeftTextField;
	protected TextField marginRightTextField;
	protected CheckBox[] columnsCheckBoxes;
	protected CheckBox[] groupingCheckBoxes;
	protected VerticalLayout observationsLayout;
	protected Label observationsLabel;
	
	public AbstractReport() { }
	
	@Override
	public void attach() {
		super.attach();
		initLayout();
		build();
	}

	public abstract void updateReport() throws UnsupportedEncodingException;
	
	/**
	 * @return property names that correspond to the objects returned by getData(). Each instance returned by
	 * getData() must have a getter for each property returned by this method.
	 */
	public abstract String[] getColumnProperties();
	
	/**
	 * @return property classes that correspond to the objects returned by getData() according to getColumnProperties().
	 */
	public abstract Class<?>[] getColumnClasses();
	
	/**
	 * @return titles to use on the table.
	 */
	public abstract String[] getColumnTitles();
	
	/**
	 * Collection of rows to show on the table. Each object in the collection must define a getter for each String returned
	 * in getColumnProperties().
	 */
	public abstract Collection<?> getData();
	
	/**
	 * @return A custom component to add to the accordion component. You can use it to add custom filtering or configuration
	 * to the report. Return null if no component is needed.
	 */
	public abstract Component getParametersComponent();
	
	public Integer[] getColumnWidths() { return null; };
	
	public String getFileName() { return "report"; };
	
	public void configureColumn(String property, AbstractColumn column, DynamicReportBuilder reportBuilder) {};
	
	public void configureColumnBuilder(String property, ColumnBuilder columnBuilder, DynamicReportBuilder reportBuilder) {};
	
	public boolean getDefalutColumnCheckBox(String property) { return true; };
	
	public String getColumnPattern(String property) { return null; };
	
	public Style getColumnStyle(String property) { return null; };
	
	public List<ConditionalStyle> getColumnConditionalStyle(String property) { return null; };
	
	public static int mmToPoints(float f) {
		return Math.round(f / 25.4f * 72); // 1in = 25.4mm = 72pt
	}
	
	public DynamicReportBuilder getReportBuilder() {
		
		FastReportBuilder reportBuilder = new FastReportBuilder();
		reportBuilder.setWhenNoData(Constants.uiEmptyReport, null);
		reportBuilder.setPrintBackgroundOnOddRows((Boolean) printBackgroundOnOddRowsCheckBox.getValue());
		reportBuilder.setPrintColumnNames((Boolean) printColumnNamesCheckBox.getValue());
		reportBuilder.setUseFullPageWidth(true);
		
		try {
			Integer top = mmToPoints(new Float(marginTopTextField.getValue().toString()));
			Integer bottom = mmToPoints(new Float(marginBottomTextField.getValue().toString()));
			Integer left = mmToPoints(new Float(marginLeftTextField.getValue().toString()));
			Integer right = mmToPoints(new Float(marginRightTextField.getValue().toString()));
			
			Page page = new Page();
			page.setWidth(mmToPoints(new Float(pageWidthTextField.getValue().toString())));
			page.setHeight(mmToPoints(new Float(pageHeightTextField.getValue().toString())));
			
			if(page.getHeight() < 140 + top + bottom || page.getWidth() < left + right || page.getHeight() < 1 || page.getWidth() < 1 || top < 0 || bottom < 0 || left < 0 || right < 0) {
				throw new NumberFormatException();
			}
			
			reportBuilder.setPageSizeAndOrientation(page);
			reportBuilder.setColumnsPerPage(new Integer(columnsPerPageTextField.getValue().toString()));
			reportBuilder.setMargins(top, bottom, left, right);
			
		} catch(NumberFormatException e) {
			refreshButton.setComponentError(new UserError(Constants.uiReportConfigurationError));
		}
		
		return reportBuilder;
	}

	public void buildColumns(DynamicReportBuilder reportBuilder) {
		try {
			String[] title = getColumnTitles();
			String[] property = getColumnProperties();
			Class<?>[] clazz = getColumnClasses();
			Integer[] width = getColumnWidths();
			
			for(int i = 0; i < property.length; i++) {
				
				if(columnsCheckBoxes[i].booleanValue()) {
					ColumnBuilder columnBuilder = ColumnBuilder.getNew();
					columnBuilder.setColumnProperty(property[i], clazz[i]);
					columnBuilder.setTitle(title[i]);
					
					if(width != null && width[i] != null) {
						columnBuilder.setWidth(width[i]);
					}
					
					String columnPattern = getColumnPattern(property[i]);
					if(columnPattern != null) {
						columnBuilder.setPattern(columnPattern);
					}
					
					Style columnStyle = null;
					
					if(groupingCheckBoxes[i].booleanValue()) {
						columnStyle = new StyleBuilder(true).setHorizontalAlign(HorizontalAlign.LEFT).setFont(Font.ARIAL_MEDIUM_BOLD).setPaddingBottom(mmToPoints(5)).setPaddingTop(mmToPoints(10)).build();
					} else {
						columnStyle = getColumnStyle(property[i]);
					}
					
					if(columnStyle == null) {
						columnStyle = new StyleBuilder(true).setStretchWithOverflow(stretchWithOverflowCheckBox.booleanValue()).build();
						columnBuilder.setTruncateSuffix("...");
					}
					
					columnBuilder.setStyle(columnStyle);
					
					List<ConditionalStyle> conditionalStyle = getColumnConditionalStyle(property[i]);
					if(conditionalStyle != null) {
						columnBuilder.addConditionalStyles(conditionalStyle);
					}
					
					configureColumnBuilder(property[i], columnBuilder, reportBuilder);
					
					AbstractColumn column = columnBuilder.build();
					reportBuilder.addColumn(column);
					
					configureColumn(property[i], column, reportBuilder);
					
					if(groupingCheckBoxes[i].booleanValue()) {
						GroupBuilder groupBuilder = new GroupBuilder();
						groupBuilder.setCriteriaColumn((PropertyColumn) column);
						groupBuilder.setGroupLayout(GroupLayout.VALUE_IN_HEADER);
						reportBuilder.addGroup(groupBuilder.build());
					}
				}
				
			}
			
		} catch (ColumnBuilderException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String getLastVisiblePropertyName() {
		String[] columnProperties = getColumnProperties();
		
		for(int i = columnProperties.length - 1; i >= 0; i--) {
			if(columnsCheckBoxes[i].booleanValue()) {
				return columnProperties[i];
			}
		}
		
		return null;
	}
	
	protected JRHtmlExporter getHtmlExporter() {
		JRHtmlExporter exporter = new JRHtmlExporter();
		String random = "" + Math.random() * 1000.0;
		random = random.replace('.', '0').replace(',', '0');
		
		exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, Utils.getWebContextPath(getApplication()) + "/image?r=" + random + "&image=");
		return exporter;
	}
	
	protected void build() {
		try {
			refreshButton.setComponentError(null);
			setObservations("");
			layout.setFirstComponent(leftLayout);
			updateReport();
			
			if(!getObservations().isEmpty()) {
				refreshButton.setComponentError(new UserError(Constants.uiSeeObservationsOnTheReport));
			}
			
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	protected ByteArrayOutputStream getOutputStream(JRExporter exporter) {
		ByteArrayOutputStream outputStream = null;
		
		try {
			DynamicReportBuilder reportBuilder = getReportBuilder();
			buildColumns(reportBuilder);
			
			Collection<?> data = getData();
			JasperPrint jasperPrint = DynamicJasperHelper.generateJasperPrint(reportBuilder.build(), new ClassicLayoutManager(), data);
			
			outputStream = new ByteArrayOutputStream();
			
			WebApplicationContext context = (WebApplicationContext) getApplication().getContext();
			context.getHttpSession().setAttribute(ImageServlet.DEFAULT_JASPER_PRINT_SESSION_ATTRIBUTE, jasperPrint);
			
			exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
			exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, outputStream);
			
			exporter.exportReport();
			
			outputStream.flush();
			outputStream.close();
			
		} catch (JRException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		return outputStream;
	}

	public void initLayout() {
		refreshButton = new Button(Constants.uiRefresh);
		pdfButton = new Button(Constants.uiPdf);
		excelButton = new Button(Constants.uiExcel);
		wordButton = new Button(Constants.uiWord);
		powerPointButton = new Button(Constants.uiPowerPoint);
		odsButton = new Button(Constants.uiOds);
		odtButton = new Button(Constants.uiOdt);
		rtfButton = new Button(Constants.uiRtf);
		htmlButton = new Button(Constants.uiHtml);
		csvButton = new Button(Constants.uiCsv);
		xmlButton = new Button(Constants.uiXml);
		
		pdfButton.setStyleName(BaseTheme.BUTTON_LINK);
		excelButton.setStyleName(BaseTheme.BUTTON_LINK);
		wordButton.setStyleName(BaseTheme.BUTTON_LINK);
		powerPointButton.setStyleName(BaseTheme.BUTTON_LINK);
		odsButton.setStyleName(BaseTheme.BUTTON_LINK);
		odtButton.setStyleName(BaseTheme.BUTTON_LINK);
		rtfButton.setStyleName(BaseTheme.BUTTON_LINK);
		htmlButton.setStyleName(BaseTheme.BUTTON_LINK);
		csvButton.setStyleName(BaseTheme.BUTTON_LINK);
		xmlButton.setStyleName(BaseTheme.BUTTON_LINK);
		
		refreshButton.addListener(this);
		pdfButton.addListener(this);
		excelButton.addListener(this);
		wordButton.addListener(this);
		powerPointButton.addListener(this);
		odsButton.addListener(this);
		odtButton.addListener(this);
		rtfButton.addListener(this);
		htmlButton.addListener(this);
		csvButton.addListener(this);
		xmlButton.addListener(this);
		
		VerticalLayout exportOptionsLayout = new VerticalLayout();
		exportOptionsLayout.setSizeUndefined();
		exportOptionsLayout.setSpacing(true);
		
		exportOptionsLayout.addComponent(pdfButton);
		exportOptionsLayout.addComponent(excelButton);
		exportOptionsLayout.addComponent(wordButton);
		exportOptionsLayout.addComponent(powerPointButton);
		exportOptionsLayout.addComponent(odsButton);
		exportOptionsLayout.addComponent(odtButton);
		exportOptionsLayout.addComponent(rtfButton);
		exportOptionsLayout.addComponent(htmlButton);
		exportOptionsLayout.addComponent(csvButton);
		exportOptionsLayout.addComponent(xmlButton);
		
		exportButton = new SplitButton(Constants.uiPdf);
		exportButton.setComponent(exportOptionsLayout);
		exportButton.addClickListener(this);
		
		String[] columnTitles = getColumnTitles();
		columnsCheckBoxes = new CheckBox[columnTitles.length];
		groupingCheckBoxes = new CheckBox[columnTitles.length];
		
		VerticalLayout columnsLayout = new VerticalLayout();
		columnsLayout.setSizeUndefined();
		columnsLayout.setSpacing(true);
		
		VerticalLayout groupingLayout = new VerticalLayout();
		groupingLayout.setSizeUndefined();
		groupingLayout.setSpacing(true);
		
		for(int i = 0; i < columnTitles.length; i++) {
			CheckBox columnCheckBox = new CheckBox(columnTitles[i], true);
			columnCheckBox.setValue(getDefalutColumnCheckBox(getColumnProperties()[i]));
			columnsLayout.addComponent(columnCheckBox);
			columnsCheckBoxes[i] = columnCheckBox;
			
			CheckBox groupingCheckBox = new CheckBox(columnTitles[i], false);
			groupingLayout.addComponent(groupingCheckBox);
			groupingCheckBoxes[i] = groupingCheckBox;
		}
		
		columnsButton = new PopupButton(Constants.uiColumns);
		columnsButton.setComponent(columnsLayout);
		
		groupingButton = new PopupButton(Constants.uiGrouping);
		groupingButton.setComponent(groupingLayout);
		
		displayLayout = new HorizontalLayout();
		displayLayout.setSpacing(true);
		displayLayout.addComponent(refreshButton);
		displayLayout.addComponent(columnsButton);
		displayLayout.addComponent(groupingButton);
		displayLayout.addComponent(exportButton);
		
		Panel exportPanel = new Panel();
		exportPanel.addComponent(displayLayout);
		
		printBackgroundOnOddRowsCheckBox = new CheckBox(Constants.uiPrintBackgroundOnOddRows, true);
		printColumnNamesCheckBox = new CheckBox(Constants.uiPrintColumnNames);
		stretchWithOverflowCheckBox = new CheckBox(Constants.uiStretchWithOverflow);
		printColumnNamesCheckBox.setValue(true);
		columnsPerPageTextField = new TextField(Constants.uiColumnsPerPage);
		columnsPerPageTextField.setValue("1");
		pageWidthTextField = new TextField(Constants.uiPageWidth);
		pageWidthTextField.setValue(Constants.reportPageWidth);
		pageHeightTextField = new TextField(Constants.uiPageHeight);
		pageHeightTextField.setValue(Constants.reportPageHeight);
		marginTopTextField = new TextField(Constants.uiMarginTop);
		marginTopTextField.setValue(Constants.reportMarginTop);
		marginTopTextField.setWidth("115px");
		marginBottomTextField = new TextField(Constants.uiMarginBottom);
		marginBottomTextField.setValue(Constants.reportMarginBottom);
		marginBottomTextField.setWidth("115px");
		marginLeftTextField = new TextField(Constants.uiMarginLeft);
		marginLeftTextField.setValue(Constants.reportMarginLeft);
		marginLeftTextField.setWidth("115px");
		marginRightTextField = new TextField(Constants.uiMarginRight);
		marginRightTextField.setValue(Constants.reportMarginRight);
		marginRightTextField.setWidth("115px");
		
		Button reverseButton = new Button(Constants.uiReverse);
		
		reverseButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void buttonClick(ClickEvent event) {
				Object height = pageHeightTextField.getValue();
				pageHeightTextField.setValue(pageWidthTextField.getValue());
				pageWidthTextField.setValue(height);
			}
		});
		
		HorizontalLayout pageLayout = new HorizontalLayout();
		pageLayout.setSpacing(true);
		pageLayout.addComponent(pageWidthTextField);
		pageLayout.addComponent(pageHeightTextField);
		pageLayout.addComponent(reverseButton);
		pageLayout.setComponentAlignment(reverseButton, Alignment.BOTTOM_LEFT);
		
		GridLayout marginLayout = new GridLayout(3, 3);
		marginLayout.addComponent(marginTopTextField, 1, 0);
		marginLayout.addComponent(marginBottomTextField, 1, 2);
		marginLayout.addComponent(marginLeftTextField, 0, 1);
		marginLayout.addComponent(marginRightTextField, 2, 1);
		
		Panel marginPanel = new Panel();
		marginPanel.setSizeUndefined();
		marginPanel.addComponent(marginLayout);
		
		VerticalLayout reportConfigurationLayout = new VerticalLayout();
		reportConfigurationLayout.setWidth("100%");
		reportConfigurationLayout.setMargin(true);
		reportConfigurationLayout.setSpacing(true);
		reportConfigurationLayout.addComponent(printBackgroundOnOddRowsCheckBox);
		reportConfigurationLayout.addComponent(printColumnNamesCheckBox);
		reportConfigurationLayout.addComponent(stretchWithOverflowCheckBox);
		reportConfigurationLayout.addComponent(new Label());
		reportConfigurationLayout.addComponent(columnsPerPageTextField);
		reportConfigurationLayout.addComponent(new Label());
		reportConfigurationLayout.addComponent(pageLayout);
		reportConfigurationLayout.addComponent(new Label());
		reportConfigurationLayout.addComponent(marginPanel);
		
		Component parametersComponent = getParametersComponent();
		
		VerticalLayout parametersLayout = new VerticalLayout();
		parametersLayout.setMargin(true);
		
		if(parametersComponent != null) {
			parametersLayout.addComponent(parametersComponent);
		}
		
		observationsLabel = new Label("", Label.CONTENT_XHTML);
		
		observationsLayout = new VerticalLayout();
		observationsLayout.setMargin(true);
		observationsLayout.addComponent(observationsLabel);
		
		accordion = new Accordion();
		accordion.setSizeFull();
		accordion.addTab(parametersLayout, Constants.uiParameters, null);
		accordion.addTab(reportConfigurationLayout, Constants.uiConfiguration, null);
		accordion.addTab(observationsLayout, Constants.uiObservations, null);
		
		VerticalLayout rightLayout = new VerticalLayout();
		rightLayout.setSizeFull();
		rightLayout.setMargin(true);
		rightLayout.addComponent(exportPanel);
		rightLayout.addComponent(accordion);
		rightLayout.setExpandRatio(accordion, 1);
		
		layout = new HorizontalSplitPanel();
		layout.setSplitPosition(65);
		layout.setSizeFull();
		layout.setSecondComponent(rightLayout);
		
		setCompositionRoot(layout);
	}
	
	public void addObservation(String text) {
		String currentText = observationsLabel.getValue().toString();
		
		if(currentText.isEmpty()) {
			setObservations("- " + text);
		} else {
			setObservations(currentText + "<br/><br/>- " + text);
		}
	}
	
	public void setObservations(String text) {
		observationsLabel.setValue(text);
	}
	
	public String getObservations() {
		return (String) observationsLabel.getValue();
	}
	
	@Override
	public void splitButtonClick(SplitButtonClickEvent event) {
		exportToPdf();
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		if(event.getButton().equals(refreshButton)) {
			build();
		} else if(event.getButton().equals(pdfButton)) {
			exportToPdf();
		} else if(event.getButton().equals(excelButton)) {
			exportToExcel();
		} else if(event.getButton().equals(wordButton)) {
			exportToWord();
		} else if(event.getButton().equals(powerPointButton)) {
			exportToPowerPoint();
		} else if(event.getButton().equals(odsButton)) {
			exportToOds();
		} else if(event.getButton().equals(odtButton)) {
			exportToOdt();
		} else if(event.getButton().equals(rtfButton)) {
			exportToRtf();
		} else if(event.getButton().equals(htmlButton)) {
			exportToHtml();
		} else if(event.getButton().equals(csvButton)) {
			exportToCsv();
		} else if(event.getButton().equals(xmlButton)) {
			exportToXml();
		}
	}

	public void exportToPdf() {
		download(getFileName() + ".pdf", new JRPdfExporter());
	}

	public void exportToExcel() {
		download(getFileName() + ".xlsx", new JRXlsxExporter());
	}

	public void exportToWord() {
		download(getFileName() + ".docx", new JRDocxExporter());
	}

	public void exportToPowerPoint() {
		download(getFileName() + ".pptx", new JRPptxExporter());
	}

	public void exportToOds() {
		download(getFileName() + ".ods", new JROdsExporter());
	}

	public void exportToOdt() {
		download(getFileName() + ".odf", new JROdtExporter());
	}

	public void exportToRtf() {
		download(getFileName() + ".rtf", new JRRtfExporter());
	}

	public void exportToHtml() {
		download(getFileName() + ".html", new JRXhtmlExporter());
	}
	
	public void exportToCsv() {
		download(getFileName() + ".csv", new JRCsvExporter());
	}
	
	public void exportToXml() {
		download(getFileName() + ".xml", new JRXmlExporter());
	}
	
	protected void download(String filename, final JRExporter exporter) {
		StreamResource resource = new StreamResource(new StreamResource.StreamSource() {
			private static final long serialVersionUID = 1L;

			@Override
			public InputStream getStream() {
				return new ByteArrayInputStream(getOutputStream(exporter).toByteArray());
			}
			
		}, filename, getApplication());
		
		getApplication().getMainWindow().open(resource, "_blank", 700, 350, 0);
	}
	
}
