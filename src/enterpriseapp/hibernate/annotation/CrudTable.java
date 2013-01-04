package enterpriseapp.hibernate.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Sets CRUD table details.
 * @author Alejandro Duarte
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface CrudTable {
	
	/**
	 * Java property to use in filters.
	 * @return Name of the Java property to use in filters.
	 */
	String filteringPropertyName();
	
	boolean embedded() default false;
	
}
