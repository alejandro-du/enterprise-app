package enterpriseapp.hibernate.dto;

/**
 * Your User Entity must implement this interface if you want to use Modules and MDIWindow capabilities.
 * 
 * @author Alejandro Duarte
 *
 */
public interface User {
	
	/**
	 * @return User's id.
	 */
	Object getId();
	
	/**
	 * @return User's login.
	 */
	String getLogin();
	
	/**
	 * @return User's password.
	 */
	String getPassword();
	
	/**
	 * Sets User's password.
	 */
	void setPassword(String password);
	
}
