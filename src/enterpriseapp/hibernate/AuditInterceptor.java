package enterpriseapp.hibernate;

import java.io.Serializable;
import java.util.Calendar;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

import enterpriseapp.EnterpriseApplication;
import enterpriseapp.hibernate.dto.AuditLog;
import enterpriseapp.ui.Constants;


/**
 * A Hibernate audit interceptor (see AuditLog class) that stores database actions into an Entity class (AuditLog).
 * You can configure a Hibernate interceptor using the "db.interceptor" property in configuration.properties.
 * To configure this interceptor add "db.interceptor=enterpriseapp.hibernate.AuditInterceptor" to your configuration
 * file, create a new Entity class (Dto) that implements AuditLog and create a custom ContainerFactory which
 * getContainer(Class<?> clazz) method returns your custom AuditLog class when
 * enterpriseapp.hibernate.dto.AuditLog.class is passed as parameter.
 * 
 * @author Alejandro Duarte
 *
 */
public class AuditInterceptor extends EmptyInterceptor {

	private static final long serialVersionUID = 1L;
	
	@Override
	public boolean onSave(Object dto, Serializable id, Object[] valor, String[] propiedad, Type[] tipo) {
		if(!(AuditLog.class.isAssignableFrom(dto.getClass()))) {
			AuditLog auditLog = createAuditLog(dto, id, valor, propiedad);
			auditLog.setAction(Constants.uiCreate);
			ContainerFactory.getInstance().getContainer(AuditLog.class).addItem(auditLog);
		}
		
		return false;
	}
	
	@Override
	public boolean onFlushDirty(Object dto, Serializable id, Object[] valor, Object[] valorAnterior, String[] propiedad, Type[] tipo) {
		if(!(AuditLog.class.isAssignableFrom(dto.getClass()))) {
			AuditLog auditLog = createAuditLog(dto, id, valor, propiedad);
			auditLog.setAction(Constants.uiModify);
			
			ContainerFactory.getInstance().getContainer(AuditLog.class).addItem(auditLog);
		}
		
		return false;
	}
	
	@Override
	public void onDelete(Object dto, Serializable id, Object[] valor, String[] propiedad, Type[] tipo) {
		if(!(AuditLog.class.isAssignableFrom(dto.getClass()))) {
			AuditLog auditLog = createAuditLog(dto, id, valor, propiedad);
			auditLog.setAction(Constants.uiDelete);
			ContainerFactory.getInstance().getContainer(AuditLog.class).addItem(auditLog);
		}
	}
	
	private AuditLog createAuditLog(Object dto, Serializable id, Object[] valor, String[] propiedad) {
		AuditLog auditLog = (AuditLog) ContainerFactory.getInstance().getContainer(AuditLog.class).newInstance();
		auditLog.setDetails(getDetails(valor, propiedad));
		auditLog.setDate(Calendar.getInstance().getTime());
		auditLog.setDtoId("" + id);
		auditLog.setEntityType(dto.getClass().getSimpleName());
		
		EnterpriseApplication application = EnterpriseApplication.getInstance();
		
		if(application != null && application.getUser() != null) {
			auditLog.setUser(application.getUser().toString());
		} else {
			auditLog.setUser(Constants.uiUnknownUser);
		}
		
		if(application != null && application.getRemoteAddr() != null) {
			auditLog.setIp(application.getRemoteAddr());
		} else {
			auditLog.setIp(Constants.uiUnknownIp);
		}
		
		return auditLog;
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
