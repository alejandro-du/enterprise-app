package enterpriseapp.hibernate;

import java.io.Serializable;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import enterpriseapp.EnterpriseApplication;
import enterpriseapp.ui.Constants;


/**
 * A Hibernate audit interceptor (see AuditLog class) that logs database accions using SLF4J.
 * You can configure a Hibernate interceptor using the "db.interceptor" property in configuration.properties.
 * To configure this interceptor add "db.interceptor=enterpriseapp.hibernate.LogBasedAuditInterceptor" to your configuration file.
 * 
 * @author Alejandro Duarte
 *
 */
public class LogBasedAuditInterceptor extends EmptyInterceptor {

	private static final long serialVersionUID = 1L;
	
	private static Logger logger = LoggerFactory.getLogger(LogBasedAuditInterceptor.class);
	
	@Override
	public boolean onSave(Object dto, Serializable id, Object[] values, String[] properties, Type[] tipo) {
		logger.info(getAuditLog(Constants.uiCreate, dto, id, values, properties));
		return false;
	}
	
	@Override
	public boolean onFlushDirty(Object dto, Serializable id, Object[] valor, Object[] valorAnterior, String[] propiedad, Type[] tipo) {
		logger.info(getAuditLog(Constants.uiModify, dto, id, valor, propiedad));
		return false;
	}
	
	@Override
	public void onDelete(Object dto, Serializable id, Object[] valor, String[] propiedad, Type[] tipo) {
		logger.info(getAuditLog(Constants.uiDelete, dto, id, valor, propiedad));
	}
	
	private String getAuditLog(String action, Object dto, Serializable id, Object[] valor, String[] propiedad) {
		String details = getDetails(valor, propiedad);
		String entityType = dto.getClass().getSimpleName();
		String user = null;
		String ip = null;
		
		EnterpriseApplication application = EnterpriseApplication.getInstance();
		
		if(application != null && application.getUser() != null) {
			user = application.getUser().toString();
		} else {
			user = Constants.uiUnknownUser;
		}
		
		if(application != null && application.getRemoteAddr() != null) {
			ip = application.getRemoteAddr();
		} else {
			ip = Constants.uiUnknownIp;
		}
		
		return Constants.appLogBasedAuditFormat
			.replace("${user}", user)
			.replace("${ip}", ip)
			.replace("${action}", action)
			.replace("${type}", entityType)
			.replace("${id}", "" + id)
			.replace("${details}", details);
	}
	
	public String getDetails(Object[] values, String[] properties) {
		String details = "";
		
		for(int i = 0; i < properties.length; i++) {
			details += "[" + properties[i] + "=" + values[i] + "]";
			if(i != properties.length - 1) {
				details += ", ";
			}
		}
		
		return details;
	}
	
}
