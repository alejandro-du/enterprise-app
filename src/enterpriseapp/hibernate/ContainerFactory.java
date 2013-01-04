package enterpriseapp.hibernate;


/**
 * This class allows you to get the container for a specified class. Normally, you extend this class to create a
 * custom ContainerFactory for your app. You must do it if you want to use any of the audit loggers provided by
 * Enterprise App for Vaadin (see AuditInterceptor and LogBasedAuditInterceptor). Once you have defined a custom
 * ContainerFactory call init(ContainerFactory containerFactory) passing your custom ContainerFactory (you can
 * do this in your Application class or, if you have one, in your custom DefaultContextListener).
 * 
 * @author Alejandro Duarte
 *
 */
public abstract class ContainerFactory {
	
	private static ContainerFactory containerFactory;

	protected ContainerFactory() {}
	
	/**
	 * Sets a custom factory.
	 * @param containerFactory
	 */
	public static void init(ContainerFactory containerFactory) {
		ContainerFactory.containerFactory = containerFactory;
	}
	
	/**
	 * @return A ContainerFactory instance.
	 */
	public static ContainerFactory getInstance() {
		return containerFactory == null ? getDefaultFactory() : containerFactory;
	}
	
	protected static ContainerFactory getDefaultFactory() {
		return new ContainerFactory() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public DefaultHbnContainer getContainer(Class<?> clazz) {
				return new DefaultHbnContainer(clazz);
			}
		};
	}
	
	/**
	 * Override this to return a Container for the specified class.
	 * @param clazz Entity class
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public abstract DefaultHbnContainer getContainer(Class<?> clazz);
	
}
