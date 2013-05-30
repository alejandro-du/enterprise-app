package enterpriseapp;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.properties.EncryptableProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.Application;
import com.vaadin.terminal.Terminal;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;

import enterpriseapp.ui.Constants;

/**
 * Utility class to perform common tasks.
 * @author Alejandro Duarte
 *
 */
public class Utils {
	
	private static Logger logger = LoggerFactory.getLogger(Utils.class);
	
	private static EncryptableProperties properties = new EncryptableProperties(new StandardPBEStringEncryptor());
	
	private Utils() {}
	
	/**
	 * Loads properties from file.
	 * @param configurationFileName .properties file name.
	 * @param password Jasypt encription password.
	 */
	public static void loadProperties(String configurationFileName, String password) {
		try {
			logger.info("Loading properties from " + configurationFileName);
			
			StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
			encryptor.setPassword(password);
			EncryptableProperties newProperties = new EncryptableProperties(encryptor);
			
			InputStream inputStream = Utils.class.getResourceAsStream(configurationFileName);
			
			if(inputStream == null) {
				inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(configurationFileName);
			}
			
			if(inputStream == null) {
				logger.error("File not found: " + configurationFileName);
			} else {
				newProperties.load(inputStream);
			}
			
			if(properties == null) {
				properties = new EncryptableProperties(encryptor);
			}
			
			properties.putAll(newProperties);
			
		} catch (IOException e) {
			throw new RuntimeException("Error initializing Utils. ", e);
		}
	}
	
	/**
	 * Returns the property value for the especified key.
	 * @param key Property key.
	 * @return Property value.
	 */
	public static String getProperty(String key) {
		return properties.getProperty(key);
	}
	
	public static String getProperty(String key, String[] params) {
		String value = properties.getProperty(key);
		
		if(value != null && params != null) {
			for(int i = 0; i < params.length; i++) {
				value = value.replaceAll("\\{" + i + "\\}", params[i]);
			}
		}
		
		return value;
	}
	
	/**
	 * Returns the property value for the especified key.
	 * @param key Property key.
	 * @param defaultValue
	 * @return Property value if property key exists, defaultValue otherwise.
	 */
	public static String getProperty(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}
	
	/**
	 * Sets a property value.
	 * @param key Property key.
	 * @param value New value.
	 */
	public static void setProperty(String key, String value) {
		properties.setProperty(key, value);
	}
	
	/**
	 * Web context path (e.g. "cis/") for the specified Application instance.
	 * @param application
	 * @return Web context path.
	 */
	public static String getWebContextPath(Application application) {
		WebApplicationContext context = (WebApplicationContext) application.getContext();
		return context.getHttpSession().getServletContext().getContextPath();
	}
	
	/**
	 * Full web context path for the specified Application instance.
	 * @param application
	 * @return Full web context path.
	 */
	public static String getWebContextRealPath(Application application) {
		WebApplicationContext context = (WebApplicationContext) application.getContext();
		return context.getHttpSession().getServletContext().getRealPath("");
	}
	
	/**
	 * Generates a random String with specified length.
	 * @param length
	 * @return A random String.
	 */
	public static String generateRandomString(int length) {
		String string = "";
        Random r = new Random( new java.util.GregorianCalendar().getTimeInMillis());
        int i = 0;

        while (i < length) {

            char c = (char) r.nextInt(255);

            if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                string += c;
                i++;
            }
        }

        return string;
	}
	
	/**
	 * Shows a yos/no dialog window.
	 * @param component Parent component.
	 * @param message Message text to display on the window.
	 * @param listener You can override "public void onClose(ConfirmDialog dialog)" and use "dialog.isConfirmed()" to check users selection.
	 */
	public static void yesNoDialog(Component component, String message, ConfirmDialog.Listener listener) {
		ConfirmDialog.show(component.getApplication().getMainWindow(), Constants.uiPleaseConfirm, message, Constants.uiYes, Constants.uiNo, listener);
	}
	
	/**
	 * Default date format.
	 * @return
	 */
	public static String getDateFormatPattern() {
		return "yyyy-MM-dd";
	}
	
	/**
	 * Default date-time format.
	 * @return
	 */
	public static String getDateTimeFormatPattern() {
		return "yyyy-MM-dd HH:mm";
	}
	
	/**
	 * Alternate date-time format.
	 * @return
	 */
	public static String getAlternateDateTimeFormatPattern() {
		return "yyyy-MM-dd HH:mm:ss";
	}
	
	/**
	 * Max value for a date.
	 * @return
	 */
	public static Date getMaxDate() {
		return stringToDate("9999-12-31");
	}
	
	/**
	 * Converts string to date using default date format.
	 * @param strDate
	 * @return
	 */
	public static Date stringToDate(String strDate) {
		try {
			return getSimpleDateFormat(getDateFormatPattern()).parse(strDate);
		} catch (ParseException e) {
			return null;
		}
	}
	
	/**
	 * Converts string to date time using default date time format.
	 * @param strDate
	 * @return
	 */
	public static Date stringToDateTime(String strDate) {
		try {
			return getSimpleDateFormat(getDateTimeFormatPattern()).parse(strDate);
		} catch (ParseException e) {
			return null;
		}
	}
	
	/**
	 * Converts Date to string using specified format String.
	 * @param date
	 * @param format
	 * @return
	 */
	public static String dateToString(Date date, String format) {
		if(date == null) {
			return "";
		}
		
		return getSimpleDateFormat(format).format(date);
	}
	
	/**
	 * Converts Date to string using the default format String.
	 * @param date
	 * @return
	 */
	public static String dateToString(Date date) {
		return dateToString(date, getDateFormatPattern());
	}
	
	/**
	 * Converts Date (with time) to string using the default date-time format String.
	 * @param date
	 * @return
	 */
	public static String dateTimeToString(Date date) {
		return dateToString(date, getDateTimeFormatPattern());
	}
	
	/**
	 * 
	 * @return Curreng time-date as String using default time-date format.
	 */
	public static String getCurrentTimeAndDate() {
        return dateToString(Calendar.getInstance().getTime(), getDateTimeFormatPattern());
	}
	
	/**
	 * Returns a localized SimpleDateFormat. Localization info from EnterpriseApp.timeZoneId.
	 * @param format
	 * @return a localized SimpleDateFormat object using EnterpriseApp.timeZoneId.
	 */
	public static SimpleDateFormat getSimpleDateFormat(String format) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
		EnterpriseApplication application = EnterpriseApplication.getInstance();
		
		if(application != null && application.getTimeZoneId() != null) {
			simpleDateFormat.setTimeZone(TimeZone.getTimeZone(application.getTimeZoneId()));
		}
		
		return simpleDateFormat;
	}
	
	/**
	 * 
	 * @return Current year using default date format.
	 */
	public static String getCurrentYear() {
		return getSimpleDateFormat("yyyy").format(new Date());
	}
	
	/**
	 * 
	 * @return Current month using default date format.
	 */
	public static String getCurrentMonth() {
		return getSimpleDateFormat("MM").format(new Date());
	}
	
	/**
	 * 
	 * @return Previous month using default date format.
	 */
	public static String getPreviousMonth() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -1);
		
		return getSimpleDateFormat("MM").format(calendar.getTime());
	}
	
	/**
	 * 
	 * @return Current year using default date format.
	 */
	public static String getCurrentDay() {
		return getSimpleDateFormat("dd").format(new Date());
	}
	
	/**
	 * Returns a new String without accent simbols (ascii representation).
	 * @param str String to convert.
	 * @return a new String without accent simbols (ascii representation).
	 */
	public static String toAscii(String str) {
		return Normalizer.normalize(str, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
	}

	/**
	 * 
	 * @param str
	 * @return MD5 Hash of the specified string.
	 */
	public static String getMD5Hash(String str) {
		MessageDigest algorithm = null;
		
		try {
			algorithm = MessageDigest.getInstance("MD5");
			
		} catch (NoSuchAlgorithmException e) {
			logger.error("Error getting MD5 Hash", e);
		}
		
		algorithm.reset();
		algorithm.update(str.getBytes(), 0, str.length());
		str = new BigInteger(1, algorithm.digest()).toString(16);
		
		while (str.length() < 32) {
			str = "0" + str;
		}
		
		return str;
	}
	
    public static void checkSignaure(String k, String fileName, String signature) {
    	String c = "";
    	
		try {
			InputStream is = Utils.class.getResourceAsStream(fileName);
			
			if(is == null) {
				throw new RuntimeException("File not found: " + fileName);
			}
			
			DataInputStream dis = new DataInputStream(is);
			BufferedReader br = new BufferedReader(new InputStreamReader(dis));
			String l;
			
			while((l = br.readLine()) != null) {
				if(!l.contains(signature)) {
					c += l;
				}
			}
			
			br.close();
			dis.close();
			is.close();
			
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Error checking signature: " + fileName + " not found.");
		} catch (IOException e) {
			throw new RuntimeException("Error checking signature.");
		}
		
		StandardPBEStringEncryptor e = new StandardPBEStringEncryptor();
		e.setPassword(k);
		
		if(signature == null || signature.isEmpty() || !e.decrypt(signature).equals(c)) {
			//System.out.println("\n" + e.encrypt(c) + "\n");
			throw new RuntimeException("Invalid signature.");
		}
    }

	/**
	 * Shows a terminal error.
	 * @param event
	 * @param application
	 */
	public static void terminalError(Terminal.ErrorEvent event, Application application) {
		String errorTime = Utils.getCurrentTimeAndDate();
		logger.error("DefaultApplication Terminal Error (" + errorTime + "):", event.getThrowable());
		String errorMessage = Constants.uiTerminalErrorMessage + "<br/><i>" + Constants.uiErrorTime + ": " + errorTime + "</i>";
		
		if(application.getMainWindow() != null) {
			application.getMainWindow().showNotification(Constants.uiError, errorMessage, Window.Notification.TYPE_ERROR_MESSAGE);
		}
	}
	
	/**
	 * 
	 * @param type
	 * @return The simple name of the specified Class in lower case.
	 */
	public static String getLowerCaseTypeName(Class<?> type) {
    	String typeName = type.getSimpleName();
    	return typeName.substring(0, 1).toLowerCase() + typeName.substring(1, typeName.length());
	}
	
	/**
	 * 
	 * @param typeName
	 * @param propertyId
	 * @return The corresponding label for the property and type specified.
	 */
	public static String getPropertyLabel(String typeName, Object propertyId) {
		typeName = typeName.substring(0, 1).toLowerCase() + typeName.substring(1, typeName.length());
		return getProperty("ui." + typeName + "." + propertyId, DefaultFieldFactory.createCaptionByPropertyId(propertyId));
	}
	
	/**
	 * 
	 * @param type
	 * @return Array with the properties to show in crud forms (from properties file).
	 */
    public static Object[] getVisibleFormProperties(Class<?> type) {
    	String visibleFieldsProp = getProperty("ui." + getLowerCaseTypeName(type) + ".form.visibleFields");
    	
    	if(visibleFieldsProp == null) {
    		return null;
    	}
    	
    	return visibleFieldsProp.replace(" ", "").split(",");
    }
    
    /**
     * 
     * @param type
     * @return Array with the properties to show in crud tables (from properties file).
     */
    public static Object[] getVisibleTableProperties(Class<?> type) {
    	String visibleFieldsProp = getProperty("ui." + getLowerCaseTypeName(type) + ".table.visibleFields");
    	
    	if(visibleFieldsProp == null) {
    		return null;
    	}
    	
    	return visibleFieldsProp.replace(" ", "").split(",");
    }

    /**
     * @return server log directory (CATALINA_HOME/logs in tomcat).
     */
    public static String getServerLogsDirectory() {
		return System.getProperty("catalina.base").replace('\\', '/') + "/logs/";
	}
    
    /**
     * @return server directory (CATALINA_HOME in tomcat).
     */
    public static String getServerDirectory() {
		return System.getProperty("catalina.base").replace('\\', '/');
	}
    
    /**
     * Replaces special characters (like á, é, í, etc.) with the corresponding html code.
     * @param html html text to parse.
     * @return parsed html.
     */
    public static String replaceHtmlSpecialCharacters(String html) {
    	return StringUtils.replaceEach(
    		html,
    		new String[] {"á", "é", "í", "ó", "ú", "Á", "É", "Í", "Ó", "Ú", "¡"},
    		new String[] {"&aacute;", "&eacute;", "&iacute;", "&oacute;", "&uacute;", "&Aacute;", "&Eacute;", "&Iacute;", "&Oacute;", "&Uacute;", "&iexcl;"}
    	);
    }
    
    /**
     * Adds a Label with no caption in the given component.
     * @param component ComponentContainer to which the spacer will be added.
     * @param width
     * @param height
     */
    public static void addSpacer(ComponentContainer component, String width, String height) {
    	Label spacer = new Label();
    	spacer.setWidth(width);
    	spacer.setHeight(height);
    	component.addComponent(spacer);
    }
	
    /**
     * Returns the content of the given file.
     * @throws IOException
     */
	public static String readFile(String path) throws IOException {
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);

		if (is == null) {
			throw new FileNotFoundException("Can't find \"" + path + "\".");
		}

		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		try {
			String line;
			br = new BufferedReader(new InputStreamReader(is));
			
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}

		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}

		return sb.toString();
	}
}
