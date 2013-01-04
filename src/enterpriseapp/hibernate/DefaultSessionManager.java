package enterpriseapp.hibernate;

import org.hibernate.Session;

import enterpriseapp.hibernate.CustomHbnContainer.SessionManager;


/**
 * Helper class to get the current Hibernate session for use in HbnContainer. 
 * @author Alejandro Duarte
 *
 */
public class DefaultSessionManager implements SessionManager {

	@Override
	public Session getSession() {
		Session currentSession = Db.getCurrentSession();
		
		if (!currentSession.getTransaction().isActive()) {
			currentSession.beginTransaction();
		}
		
		return currentSession;
	}

}
