package enterpriseapp;


import com.vaadin.server.CustomizedSystemMessages;

import enterpriseapp.ui.Constants;

/**
 * System Messages from Constants. An instance of this class is returned by EnterpriseApplication.getSystemMessages().
 * @author Alejandro Duarte
 *
 */
public class PropertiesFileSystemMessages extends CustomizedSystemMessages {

	private static final long serialVersionUID = 1L;
	
	public PropertiesFileSystemMessages() {
		setSessionExpiredNotificationEnabled(false);
		setOutOfSyncCaption(Constants.uiError);
		setOutOfSyncMessage(Constants.uiOutOfSyncMessage);
		setCommunicationErrorCaption(Constants.uiError);
		setCommunicationErrorMessage(Constants.uiCommunicationErrorMessage);
		setInternalErrorCaption(Constants.uiError);
		setInternalErrorMessage(Constants.uiInternalErrorMessage);
		setCookiesDisabledCaption(Constants.uiError);
		setCookiesDisabledMessage(Constants.uiCookiesDisabledMessage);
		setOutOfSyncCaption(Constants.uiError);
		setOutOfSyncMessage(Constants.uiOutOfSyncMessage);
	}
	
}
