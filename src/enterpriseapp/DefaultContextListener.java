package enterpriseapp;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import enterpriseapp.hibernate.Db;
import enterpriseapp.job.LogFilesCollectorJob;
import enterpriseapp.ui.Constants;


/**
 * Default ServletContextListener. Loads properties from files and initializes the DB. Add this listener to your web.xml.
 * @author Alejandro Duarte
 *
 */
public class DefaultContextListener implements ServletContextListener {

	private static Logger logger = LoggerFactory.getLogger(DefaultContextListener.class);

	@Override
    public void contextInitialized(ServletContextEvent contextEvent) {
		
		logger.info("Initializing context...");
		
    	for(String fileName : getPropertiesFileNames()) {
    		Utils.loadProperties(fileName, getPassword());
    	}
    	
    	String[] signedPropertiesFileNames = getSignedPropertiesFileNames();
    	String[] propertiesFileSignatures = getPropertiesFileSignatures();
    	
		if(signedPropertiesFileNames != null) {
    		for(int i = 0; i < signedPropertiesFileNames.length; i++) {
    			Utils.checkSignaure(getPassword(), signedPropertiesFileNames[i], propertiesFileSignatures[i]);
    		}
    	}
    	
		initDatabase();
		
		initQuartz();
		
		if(Constants.appCollectLogFiles) {
			LogFilesCollectorJob.scheduleLogFilesCollectorJob();
		}
		
		logger.info("Context initialized (" + contextEvent.getServletContext().getContextPath() + ")");
    }

	@Override
    public void contextDestroyed(ServletContextEvent contextEvent) {
		try {
			Scheduler scheduler = EnterpriseApplication.getScheduler();
			
			if(scheduler != null) {
				scheduler.shutdown();
			}
			
		} catch (SchedulerException e) {
			throw new RuntimeException(e);
		}
		
    	Db.close();
    	logger.info("Context destroyed");
    }
	
	/**
	 * Override this method to provide your custom Quartz configuration.
	 */
	protected void initQuartz() {
		try {
			StdSchedulerFactory schedulerFactory = new StdSchedulerFactory();
			Scheduler scheduler = schedulerFactory.getScheduler();
			scheduler.start();
			EnterpriseApplication.setScheduler(scheduler);
			
		} catch (SchedulerException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Override this method to provide your custom database configuration.
	 */
	protected void initDatabase() {
		Db.initFromPropertiesFile();
	}

	/**
	 * Override this to provide your custom configuration files.
	 * @return
	 */
	public String[] getPropertiesFileNames() {
		return new String[] {"/defaultConfiguration.properties", "/configuration.properties"};
	}
	
	/**
	 * Override this to specify digital signed properties files to check.
	 * @return
	 */
	public String[] getSignedPropertiesFileNames() {
		return null;
	}

	/**
	 * Override this to specify digital signatures. getPropertiesFileSignatures.size() must be equals to getSignedPropertiesFileNames().size().
	 * @return
	 */
	public String[] getPropertiesFileSignatures() {
		return null;
	}
	
	/**
	 * Override this to provide your custom encryption password.
	 * @return
	 */
	public String getPassword() {
		return "password";
	}
	
}
