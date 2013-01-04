package enterpriseapp.ui.window;

import enterpriseapp.hibernate.dto.User;


public interface Module {
	
	void init();
	
	boolean userCanAccess(User user);
	
	void add(MDIWindow mdiWindow, User user);
	
}
