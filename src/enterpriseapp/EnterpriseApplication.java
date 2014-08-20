package enterpriseapp;

import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.server.ErrorEvent;
import com.vaadin.server.ErrorHandler;
import com.vaadin.server.LegacyApplication;
import com.vaadin.server.SystemMessages;
import com.vaadin.server.VaadinServletService;
import com.vaadin.server.VaadinSession;

import enterpriseapp.hibernate.Db;

/**
 * @deprecated use EnterpriseAppUI
 * Base Application class with transaction listener and error messages configured from .properties file.
 * @author Alejandro Duarte
 *
 */
@Deprecated
public class EnterpriseApplication extends LegacyApplication {

	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.getLogger(EnterpriseApplication.class);
	
	protected String remoteAddr;
	
	protected static Scheduler scheduler;
	
	protected String timeZoneId = TimeZone.getDefault().getID();

	/**
	 * Adds a default transaction listener.
	 */
	@Override
	public void init() {
		final LegacyApplication app = this;
		
		VaadinSession.getCurrent().setErrorHandler(new ErrorHandler() {
			private static final long serialVersionUID = 1L;

			@Override
			public void error(ErrorEvent event) {
				Utils.terminalError(event, app);
			}
		});

		if(!Db.isInitialized()) {
			logger.warn("No TransactionListener added: Database is not initialized. You can initialize a database configuring a new 'DefaultTransactionListener' in your web.xml and adding a 'configuration.properties' file to your classpath.");
		}
		
		VaadinSession.getCurrent().setAttribute("application", this);
	}
	
	/**
	 * 
	 * @return SystemMessages from .properties file.
	 */
	public static SystemMessages getSystemMessages() {
		return new PropertiesFileSystemMessages();
	}
	
	/**
	 * @deprecated see EnterpriseApplication.init
	 */
	@Deprecated
	public void terminalError(ErrorEvent event) {
		Utils.terminalError(event, this);
	}
	
	/**
	 * 
	 * @return the current EnterpriseApplication instance.
	 */
	// @return the current application instance
	public static EnterpriseApplication getInstance() {
		return (EnterpriseApplication) VaadinSession.getCurrent().getAttribute("application");
	}
	
	/**
	 */
	@Deprecated
	public static void setInstance(EnterpriseApplication application) {
	}
	
	/**
	 * 
	 * @return the remote (client) address.
	 */
	public String getRemoteAddr() {
		return remoteAddr;
	}
	
	public static Scheduler getScheduler() {
		return scheduler;
	}

	public static void setScheduler(Scheduler scheduler) {
		EnterpriseApplication.scheduler = scheduler;
	}

	public String getTimeZoneId() {
		return timeZoneId;
	}

	public void setTimeZoneId(String timeZoneId) {
		this.timeZoneId = timeZoneId;
	}

	/**
	 * 
	 * @deprecated use VaadinServletService.getCurrentServletRequest()
	 */
	@Deprecated
	public HttpServletRequest getHttpServletRequest() {
		return VaadinServletService.getCurrentServletRequest();
	}

	/**
	 * 
	 * @deprecated use VaadinServletService.getCurrentResponse()
	 */
	@Deprecated
	public HttpServletResponse getHttpServletResponse() {
		return VaadinServletService.getCurrentResponse();
	}

	public Object getUser() {
		return VaadinSession.getCurrent().getAttribute("user");
	}
	
	public void setUser(Object user) {
		VaadinSession.getCurrent().setAttribute("user", user);
	}
	
}
