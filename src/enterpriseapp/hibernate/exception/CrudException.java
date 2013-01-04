package enterpriseapp.hibernate.exception;

/**
 * Exception in a crud operation.
 * 
 * @author Alejandro Duarte
 *
 */
public class CrudException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public CrudException(String message) {
		super(message);
	}

}
