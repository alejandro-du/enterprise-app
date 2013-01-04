package enterpriseapp;

import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.vaadin.Application;
import com.vaadin.terminal.Terminal;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;

import enterpriseapp.hibernate.Db;
import enterpriseapp.hibernate.DefaultTransactionListener;
import enterpriseapp.ui.Constants;

/**
 * Base Application class with transaction listener and error messages configured from .properties file.
 * @author Alejandro Duarte
 *
 */
public class EnterpriseApplication extends Application implements HttpServletRequestListener {

	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.getLogger(EnterpriseApplication.class);
	
	protected static ThreadLocal<EnterpriseApplication> threadLocal = new ThreadLocal<EnterpriseApplication>();
	
	protected String remoteAddr;
	
	protected static Scheduler scheduler;
	
	protected String timeZoneId = TimeZone.getDefault().getID();
	
	/**
	 * Adds a default transaction listener.
	 */
	@Override
	public void init() {
		if(Db.isInitialized()) {
			getContext().addTransactionListener(new DefaultTransactionListener());
		} else {
			logger.warn("No TransactionListener added: Database is not initialized. You can initialize a database configuring a new 'DefaultTransactionListener' in your web.xml and adding a 'configuration.properties' file to your classpath.");
		}
	}
	
	/**
	 * 
	 * @return SystemMessages from .properties file.
	 */
	public static SystemMessages getSystemMessages() {
		return new PropertiesFileSystemMessages();
	}
	
	/**
	 * Terminal errors are delegated to Utils.
	 */
	@Override
	public void terminalError(Terminal.ErrorEvent event) {
		Utils.terminalError(event, this);
	}
	
	/**
	 * 
	 * @return the current EnterpriseApplication instance.
	 */
	// @return the current application instance
	public static EnterpriseApplication getInstance() {
		return threadLocal.get();
	}
	
	/**
	 * Sets the current application instance.
	 * @param application EnterpriseApplication to set.
	 */
	public static void setInstance(EnterpriseApplication application) {
		threadLocal.set(application);
	}
	
	/**
	 * Sets the current UtilsApplication instance for future reference.
	 */
	@Override
	public void onRequestStart(HttpServletRequest request, HttpServletResponse response) {
		EnterpriseApplication.setInstance(this);
		
		if(Constants.dbUseCloudFoundryDatabase) {
			remoteAddr = request.getHeader("X-Cluster-Client-IP");
		} else {
			remoteAddr = request.getRemoteAddr();
		}
	}
	
	/**
	 * Removes the current threadlocal value.
	 */
	@Override
	public void onRequestEnd(HttpServletRequest request, HttpServletResponse response) {
		threadLocal.remove();
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
	
}
