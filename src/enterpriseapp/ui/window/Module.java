package enterpriseapp.ui.window;

import enterpriseapp.hibernate.dto.User;


public interface Module {
	
	/**
	 * Executed once per app instance.
	 */
	void init();
	
	/**
	 * @param user
	 * @return Returns true if the given user can access this module.
	 */
	boolean userCanAccess(User user);
	
	/**
	 * This is called when the module is added to an application.
	 * @param mdiWindow MDIWindow that the module is to be added.
	 * @param user Session's user.
	 */
	void add(MDIWindow mdiWindow, User user);
	
}
