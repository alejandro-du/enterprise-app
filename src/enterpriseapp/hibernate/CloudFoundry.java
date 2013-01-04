package enterpriseapp.hibernate;

import argo.jdom.JdomParser;
import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;
import argo.saj.InvalidSyntaxException;

/**
 * CloudFoundry integration helper class.
 * 
 * @author Alejandro Duarte
 *
 */
public class CloudFoundry {
	
	/**
	 * Gets the database connection url.
	 * @return database connection url.
	 */
	public static String getDbUrl() {
		String url = null;
		JsonNode credentials = getMySqlCredentialsJsonNode();
		
		String name = credentials.getStringValue("name");
		String hostname = credentials.getStringValue("hostname");
		String port = credentials.getNumberValue("port");
		
		url = "jdbc:mysql://" + hostname + ":" + port + "/" + name;
		
		return url;
	}
	
	/**
	 * Gets the database user.
	 * @return database user name.
	 */
	public static String getDbUser() {
		return getMySqlCredentialsJsonNode().getStringValue("user");
	}

	/**
	 * Gets the database password.
	 * @return database password.
	 */
	public static String getDbPassword() {
		return getMySqlCredentialsJsonNode().getStringValue("password");
	}

	/**
	 * Gets CloudFoundry services from "VCAP_SERVICES" environment variable as a JsonRootNode.
	 * @return CloudFoundry services from "VCAP_SERVICES" environment variable as a JsonRootNode.
	 */
	protected static JsonRootNode getServicesJsonRootNode() {
		JsonRootNode root = null;
		String services = System.getenv("VCAP_SERVICES");
		
		if(services != null && !services.isEmpty()) {
			try {
				root = new JdomParser().parse(services);
			} catch (InvalidSyntaxException e) {
				throw new RuntimeException(e);
			}
		} else {
			throw new RuntimeException("Error getting configuration from CloudFoundry.");
		}
		
		return root;
	}
	
	/**
	 * Gets a MySQL service from "VCAP_SERVICES" environment variable as a JsonNode.
	 * @return MySQL service from "VCAP_SERVICES" environment variable as a JsonNode.
	 */
	protected static JsonNode getMysqlJsonNode() {
		JsonRootNode root = getServicesJsonRootNode();
		JsonNode mysql = null;
		
		if(root != null) {
			mysql = root.getNode("mysql-5.1");
		}
		
		return mysql;
	}
	
	/**
	 * Gets a MySQL service credentials from "VCAP_SERVICES" environment variable as a JsonNode.
	 * @return MySQL service credentials from "VCAP_SERVICES" environment variable as a JsonNode.
	 */
	protected static JsonNode getMySqlCredentialsJsonNode() {
		JsonNode credentials = getMysqlJsonNode().getNode(0).getNode("credentials");
		return credentials;
	}

}
