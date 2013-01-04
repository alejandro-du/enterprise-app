package enterpriseapp.ui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.vaadin.addon.customfield.CustomField;
import org.vaadin.easyuploads.UploadField;

import com.vaadin.Application;
import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.Link;
import com.vaadin.ui.Upload.FinishedEvent;

public class DownloadField extends CustomField {

	private static final long serialVersionUID = 1L;
	
	protected Link link;
	protected UploadField uploadField;
	protected String fileName;
	
	public DownloadField(Application application) {
		this(application, Constants.uiDownloadFile);
	}
	
	public DownloadField(Application application, String linkCaption) {
		uploadField = new UploadField() {
			private static final long serialVersionUID = 1L;
			@Override
			protected String getDisplayDetails() {
				return getLastFileName();
			}
			@Override
			public void uploadFinished(FinishedEvent event) {
				try {
					uploadFinishedEvent(event);
				} catch (IllegalArgumentException e) {
					throw new RuntimeException(e);
				}
				super.uploadFinished(event);
			}
		};
		
		uploadField.setFieldType(UploadField.FieldType.BYTE_ARRAY);
		uploadField.setButtonCaption(Constants.uiUploadFile);
		uploadField.setFileDeletesAllowed(false);
		
		try {
			link = new Link(linkCaption, new StreamResource(new StreamResource.StreamSource() {
				private static final long serialVersionUID = 1L;
				public InputStream getStream() {
					try {
						return new ByteArrayInputStream((byte[]) getValue());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}, getFileName(), application));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		setCompositionRoot(link);
	}
	
	public void uploadFinishedEvent(FinishedEvent event) {
		try {
			setValue(uploadField.getValue());
			fileName = event.getFilename();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public Class<?> getType() {
		return byte[].class;
	}
	
	@Override
	public void setReadOnly(boolean readOnly) {
		super.setReadOnly(false);
		
		if(readOnly) {
			setCompositionRoot(link);
			link.setEnabled(getValue() != null);
		} else {
			setCompositionRoot(uploadField);
		}
	}
	
	public String getFileName() {
		return fileName;
	}
	
}