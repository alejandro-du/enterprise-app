package enterpriseapp.hibernate;


import com.vaadin.Application;
import com.vaadin.service.ApplicationContext;

/**
 * This class allows automatic committing and closing of the current Hibernate session. An instance of this class is
 * added to the context in EnterpriseApplication.
 * 
 * @author Alejandro Duarte
 *
 */
public class DefaultTransactionListener implements ApplicationContext.TransactionListener {

	private static final long serialVersionUID = 1L;

	public void transactionStart(Application application, Object transactionData) {
	}

	/**
	 * If the current session is active, commit and close the session.
	 */
	public void transactionEnd(Application application, Object transactionData) {
		Db.commitTransactionAndCloseSession();
	}

}
