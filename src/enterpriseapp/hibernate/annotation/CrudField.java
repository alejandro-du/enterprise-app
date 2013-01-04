package enterpriseapp.hibernate.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Sets CRUD fields details. Use it in your Entities classes.
 * @author Alejandro Duarte
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface CrudField {
	
	/**
	 * @return if true, the field will be required in the CRUD. If false, JPA annotations will be used to configure the
	 * required state.
	 */
	boolean forceRequired() default false;

	/**
	 * @return true if the field must be shown in the CRUD table.
	 */
	boolean showInTable() default true;
	
	/**
	 * If true, an email validator will be added to the field.
	 * @return true if the field is an email address.
	 */
	boolean isEmail() default false;
	
	/**
	 * If true, a password field will be created.
	 * @return true if the field is a password string.
	 */
	boolean isPassword() default false;
	
	/**
	 * If true, and property is ontToMany or manyToMany, will show a CRUD table for this property. 
	 * @return
	 */
	boolean embedded() default false;
}
