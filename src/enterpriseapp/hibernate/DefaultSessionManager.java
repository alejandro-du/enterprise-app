package enterpriseapp.hibernate;

import java.io.Serializable;

import org.hibernate.Session;

import enterpriseapp.hibernate.CustomHbnContainer.SessionManager;


/**
 * Helper class to get the current Hibernate session for use in HbnContainer. 
 * @author Alejandro Duarte
 *
 */
public class DefaultSessionManager implements SessionManager, Serializable {

	private static final long serialVersionUID = 1L;

	@Override
	public Session getSession() {
		Session currentSession = Db.getCurrentSession();
		
		if (!currentSession.getTransaction().isActive()) {
			currentSession.beginTransaction();
		}
		
		return currentSession;
	}

}
