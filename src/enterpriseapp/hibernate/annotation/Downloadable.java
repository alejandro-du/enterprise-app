package enterpriseapp.hibernate.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Properties annotated with this will be downloadable.
 * 
 * @author Alejandro Duarte
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Downloadable {
	
	/**
	 * 
	 * @return Java property name used as the file name when file is downloaded.
	 */
	String propertyFileName();
	
	/**
	 * 
	 * @return Sufix for the file name when the file is downloaded.
	 */
	String fileNameSufix() default "";
	
}
