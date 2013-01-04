package enterpriseapp.hibernate.dto;

import java.util.Date;

/**
 * If you includes "db.interceptor=enterpriseapp.hibernate.LogBasedAuditInterceptor" in your configuration files, You must
 * define an Entity class implementing this interface. You must create a custom ContainerFactory returning a proper
 * DefaultHbnContainer for your audit Entity class when the method getContainer(Class<?> clazz) is called with
 * enterpriseapp.hibernate.dto.AuditLog.class as parameter.
 * 
 * @author Alejandro Duarte
 *
 */
public interface AuditLog {
	
	String getEntityType();

	void setEntityType(String entityType);

	Date getDate();

	void setDate(Date date);

	String getAction();

	void setAction(String action);

	String getDtoId();

	void setDtoId(String dtoId);

	String getDetails();

	void setDetails(String details);

	String getUser();

	void setUser(String user);

	String getIp();

	void setIp(String ip);

}
