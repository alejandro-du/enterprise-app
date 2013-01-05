package enterpriseapp.ui.window;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.terminal.UserError;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import enterpriseapp.ui.Constants;

/**
 * A helper Vaadin Window to make custom authentication windows.
 * 
 * @author Alejandro Duarte
 *
 */
public abstract class AuthWindow extends Window implements Button.ClickListener {

	private static final long serialVersionUID = 1L;
	
	protected VerticalLayout layout = new VerticalLayout();
	protected Panel panel = new Panel();
	protected Label label = new Label();
	protected TextField loginTf = new TextField();
	protected PasswordField passwordTf = new PasswordField();
	protected Button loginButton;
	
	public AuthWindow(String windowCaption, String buttonCaption, String userLoginCaption, String userPasswordCaption, String login, String password) {
		setCaption(windowCaption);
		loginTf.setCaption(userLoginCaption);
		passwordTf.setCaption(userPasswordCaption);
		loginButton = new Button(buttonCaption);
		loginTf.setValue(login);
		passwordTf.setValue(password);
		initLayout();
	}
	
	/**
	 * Override this to provide your own functionality.
	 */
	public abstract void buttonClicked();
	
	protected void initLayout() {
		setModal(true);
		setSizeUndefined();
		setResizable(false);
		
		layout.setSizeUndefined();
		
		FormLayout formLayout = new FormLayout();
		formLayout.setMargin(true);
		
		loginButton.addListener(this);
		
		panel.setVisible(false);
		panel.addComponent(label);
		
		formLayout.addComponent(loginTf);
		formLayout.addComponent(passwordTf);
		formLayout.addComponent(loginButton);
		
		layout.addComponent(panel);
		layout.setComponentAlignment(panel, Alignment.MIDDLE_CENTER);
		layout.addComponent(formLayout);
		layout.setComponentAlignment(formLayout, Alignment.MIDDLE_CENTER);
		
		setContent(layout);
		loginButton.setClickShortcut(KeyCode.ENTER);
		
		addListener(new FieldEvents.FocusListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void focus(FocusEvent event) {
				loginTf.focus();
			}
		});
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		loginTf.setComponentError(null);
		passwordTf.setComponentError(null);
		panel.setVisible(false);
		
		if(loginTf.isVisible() && (loginTf.getValue() == null || loginTf.getValue().toString().isEmpty())) {
			loginTf.setComponentError(new UserError(Constants.uiRequiredField));
			
		} else if(passwordTf.getValue() == null || passwordTf.getValue().toString().isEmpty()) {
			passwordTf.setComponentError(new UserError(Constants.uiRequiredField));
			
		} else {
			try {
				buttonClicked();
			} catch(InvalidValueException e) {
				label.setCaption(e.getMessage());
				panel.setVisible(true);
			}
		}
	}

	public TextField getLoginTf() {
		return loginTf;
	}

	public PasswordField getPasswordTf() {
		return passwordTf;
	}

	public Button getLoginButton() {
		return loginButton;
	}

}
