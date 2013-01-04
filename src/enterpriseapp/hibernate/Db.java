package enterpriseapp.hibernate;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.persistence.Persistence;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.ejb.HibernateEntityManagerFactory;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.collection.AbstractCollectionPersister;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.jasypt.util.binary.BasicBinaryEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import enterpriseapp.Utils;
import enterpriseapp.ui.Constants;


/**
 * Database management. Normally used from within a ServletContextListener when the context is initialized.
 * @author Alejandro Duarte
 *
 */
public class Db {
	
	private static Logger logger = LoggerFactory.getLogger(Db.class);
	private static ArrayList<HibernateEntityManagerFactory> entityManagerFactoryList = new ArrayList<HibernateEntityManagerFactory>();
	
	private Db() { }
	
	/**
	 * Configures Hibernate from properties file.
	 * @return Properties map.
	 */
	public static HashMap<String, String> getPropertiesFromFile() {
		HashMap<String, String> properties = new HashMap<String, String>();
		
		properties.put("javax.persistence.jdbc.driver", Constants.dbDriver());
		properties.put("javax.persistence.jdbc.url", Constants.dbUrl());
		properties.put("javax.persistence.jdbc.user", Constants.dbUser());
		properties.put("javax.persistence.jdbc.password", Constants.dbPassword());
		properties.put("hibernate.dialect", Constants.dbDialect());
		properties.put("hibernate.show_sql", Constants.dbShowSQL());
		properties.put("hibernate.hbm2ddl.auto", Constants.dbSchemaGeneration());
		properties.put("hibernate.c3p0.min_size", Constants.dbMinSize());
		properties.put("hibernate.c3p0.max_size", Constants.dbMaxSize());
		properties.put("hibernate.c3p0.timeout", Constants.dbPoolTimeout());
		properties.put("hibernate.c3p0.max_statements", Constants.dbMaxStatements());
		properties.put("hibernate.c3p0.validationQuery", Constants.dbPoolValidationQuery());
		properties.put("hibernate.current_session_context_class", "org.hibernate.context.ThreadLocalSessionContext");
		
		if(Constants.dbInterceptor() != null) {
			properties.put("hibernate.ejb.interceptor", Constants.dbInterceptor());
		}
		
		return properties;
	}
	
	/**
	 * Inits the database using the properties file.
	 * @param persistenceUnit
	 */
	public static void initFromPropertiesFile(String persistenceUnit) {
		logger.info("Initializing persistence unit " + persistenceUnit);
		
    	if(Constants.dbUseCloudFoundryDatabase) {
    		configureDbFromCloudFoundry();
    	}
		
		addNewEntityManagerFactory(persistenceUnit, getPropertiesFromFile());
	}
	
	/**
	 * Inits the database.
	 * @param persistenceUnit Persistence unit.
	 * @param properties Properties to use for Hibernate configuration.
	 */
	public static void init(String persistenceUnit, HashMap<String, String> properties) {
		logger.info("Initializing persistence unit " + persistenceUnit);
		
    	if(Constants.dbUseCloudFoundryDatabase) {
    		logger.info("Configuring database from Cloud Foundry");
    		configureDbFromCloudFoundry();
    	}
    	
		if(entityManagerFactoryList.isEmpty()) {
			throw new RuntimeException("No default database set. You must first initialize a default database.");
			
		} else {
			addNewEntityManagerFactory(persistenceUnit, properties);
		}
	}
	
	/**
	 * Sets the connection properties from CloudFoundry.
	 */
	private static void configureDbFromCloudFoundry() {
		Utils.setProperty("db.url", CloudFoundry.getDbUrl() + "?autoReconnect=true");
    	Utils.setProperty("db.user", CloudFoundry.getDbUser());
    	Utils.setProperty("db.password", CloudFoundry.getDbPassword());
	}
	
	/**
	 * Closes the database.
	 */
	public static void close() {
		logger.info("Closing database...");
		
		if(!entityManagerFactoryList.isEmpty()) {
			entityManagerFactoryList.get(0).close();
			logger.info("Database closed");
		}
	}
	
	/**
	 * @return the current Hibernate session.
	 */
	public static Session getCurrentSession() {
		return entityManagerFactoryList.get(0).getSessionFactory().getCurrentSession();
	}
	
	protected static void addNewEntityManagerFactory(String persistenceUnit, HashMap<String, String> properties) {
		try {
			entityManagerFactoryList.add((HibernateEntityManagerFactory) Persistence.createEntityManagerFactory(persistenceUnit, properties));
			
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static boolean isInitialized() {
		return entityManagerFactoryList != null && !entityManagerFactoryList.isEmpty();
	}
	
	/**
	 * Begins a transaction on the current session.
	 */
	public static void beginTransaction() {
		getCurrentSession().beginTransaction();
	}
	
	/**
	 * Commits transaction and closes current session.
	 */
	public static void commitTransactionAndCloseSession() {
		commitTransaction();
		
		if(getCurrentSession().isOpen()) {
			getCurrentSession().close();
		}
	}

	/**
	 * Commits current transaction. 
	 */
	public static void commitTransaction() {
		if(getCurrentSession().getTransaction().isActive()) {
			try {
				getCurrentSession().getTransaction().commit();
			} catch(Exception e) {
				getCurrentSession().getTransaction().rollback();
				logger.error("Can't commit transaction.", e);
			}
		}
	}
	
	/**
	 * Rolls back current transaction.
	 */
	public static void rollBackTransaction() {
		getCurrentSession().getTransaction().rollback();
	}
	
	public static Map<String, ClassMetadata> getAllClassMetadata() {
		return entityManagerFactoryList.get(0).getSessionFactory().getAllClassMetadata();
	}

	/**
	 * @return List with all the database table names.
	 */
	@SuppressWarnings("rawtypes")
	public static List<String> getAllTableNames() {
		HashSet<String> tables = new HashSet<String>();
		
		Map<String, ClassMetadata> allClassMetadata = entityManagerFactoryList.get(0).getSessionFactory().getAllClassMetadata();
		Map allCollectionMetadata = entityManagerFactoryList.get(0).getSessionFactory().getAllCollectionMetadata();
		
		for(String key : allClassMetadata.keySet()) {
			ClassMetadata classMetadata = allClassMetadata.get(key);
			
			if(classMetadata instanceof AbstractEntityPersister) {
				AbstractEntityPersister persister = (AbstractEntityPersister) classMetadata;
				tables.add(persister.getTableName());
			}
		}
		
		for(Object key : allCollectionMetadata.keySet()) {
			AbstractCollectionPersister persister = (AbstractCollectionPersister) allCollectionMetadata.get(key);
			tables.add(persister.getTableName());
		}
		
		
		return new ArrayList<String>(tables);
	}

	/**
	 * Creates a database backup file.
	 * @param fileName Databse backup file name.
	 * @param directory Database backup file directory.
	 * @param password Encryption password.
	 * @return Database backup file.
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	public static File newBackup(String fileName, String directory, String password) throws Exception {
		List<String> tables = getAllTableNames();
		String tablesLock = "";
		String tablesFlush = "";
		File zipFile = null;
		
		try {
			for(int i = 0; i < tables.size(); i++) {
				tablesLock += tables.get(i) + " WRITE";
				tablesFlush += tables.get(i);
				
				if(i < tables.size() - 1) {
					tablesFlush += ", ";
					tablesLock += ", ";
				}
				
				File file = new File(directory + tables.get(i) + ".backup");
				
				if(file.exists()) {
					file.delete();
				}
			}
			
			Statement statement = null;
			
			try {
				getCurrentSession().beginTransaction();
				statement = getCurrentSession().connection().createStatement();
				statement.execute("LOCK TABLES " + tablesLock);
				
				statement.execute("FLUSH TABLES " + tablesFlush);
				
				for(String table : tables) {
					statement.execute("SELECT * INTO OUTFILE '" + directory + table + ".backup' FROM " + table);
				}
				
			} catch(Exception e) {
				throw e;
				
			} finally {
				if(statement != null) {
					statement.execute("UNLOCK TABLES");
					statement.close();
					getCurrentSession().getTransaction().commit();
				}
			}
			
			byte[] buf = new byte[1024];
			zipFile = new File(directory + fileName);
			
			if(zipFile.exists()) {
				zipFile.delete();
			}
			
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
			
			for(String table : tables) {
				FileInputStream in = new FileInputStream(directory + table + ".backup");
				
				out.putNextEntry(new ZipEntry(table + ".backup"));
				
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				
				out.closeEntry();
				in.close();
				
				File file = new File(directory + table + ".backup");
				
				if(file.exists()) {
					file.delete();
				}
			}
			
			out.close();
			encryptFile(zipFile, password);
			
		} catch(Exception e) {
			if(zipFile != null) {
				zipFile.delete();
			}
			throw e;
		}
		
		return zipFile;
	}
	
	/**
	 * Restore a database from backup file.
	 * @param file Database backup file.
	 * @param password Encryption password.
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	public static void restoreBackup(File file, String password) throws Exception {
		List<String> tables = getAllTableNames();
		String path = file.getPath() + "-extracted/";
		path = path.replace('\\', '/');
		
		Statement statement = null;
		
		try {
			statement = getCurrentSession().connection().createStatement();
		} catch (HibernateException e) {
			throw new RuntimeException(e);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
		try {
			file = decryptFile(file, password);
			unzip(file, path);
			
			statement.execute("START TRANSACTION");
			statement.execute("SET foreign_key_checks = 0");
			
			for(String table : tables) {
				statement.execute("DELETE FROM " + table);
				statement.execute("LOAD DATA INFILE '" + path + table + ".backup' INTO TABLE " + table);
			}
			
			file.delete();
			
		} catch (Exception e) {
			try {
				logger.error("Rolling back transaction...");
				statement.execute("ROLLBACK");
				logger.error("Transaction rollback OK");
			} catch (SQLException e1) {
				logger.error("Transaction rollback ERROR");
				throw new RuntimeException(e);
			}
			
			throw e;
			
		} finally {
			try {
				
				statement.execute("SET foreign_key_checks = 1");
				statement.execute("COMMIT;");
				statement.close();
				
				for(String table : tables) {
					File f = new File(path + table + ".backup");
					f.delete();
				}
			
				File f = new File(path);
				f.delete();
				
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
			
		}
	}
	
	/**
	 * Compress files using zip format.
	 * @param files files to compress.
	 * @param zipFileName zip file name.
	 * @throws Exception
	 */
	public static void zip(File[] files, String zipFileName) throws Exception {
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
		
		for(File file : files) {
	        FileInputStream in = new FileInputStream(file);
	        out.putNextEntry(new ZipEntry(file.getName()));
	        byte[] buf = new byte[1024];
	        
	        int len;
	        while ((len = in.read(buf)) > 0) {
	            out.write(buf, 0, len);
	        }

	        // Complete the entry
	        out.closeEntry();
	        in.close();
	    }

	    // Complete the ZIP file
	    out.close();
	}
	
	/**
	 * Unzips a file.
	 * @param file File to unzip.
	 * @param directoryToExtractTo Destiny directory.
	 * @throws Exception
	 */
    @SuppressWarnings("rawtypes")
	public static void unzip(File file, String directoryToExtractTo) throws Exception {
        Enumeration entriesEnum;
        ZipFile zipFile = new ZipFile(file);
        entriesEnum = zipFile.entries();
        
        File directory = new File(directoryToExtractTo);
        
        if (!directory.exists()) {
            new File(directoryToExtractTo).mkdir();
        }
        
        while (entriesEnum.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entriesEnum.nextElement();

            if (entry.isDirectory()) {
                new File(directoryToExtractTo + "/" + entry.getName()).mkdir();
            } else {
                writeFile(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(directoryToExtractTo + entry.getName())));
            }
        }
        
        zipFile.close();
    }
    
    private static void writeFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        
        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }
        
        in.close();
        out.close();
    }
    
    /**
     * Encrypts a file. Specified file will be overwriten.
     * @param file File to encrypt.
     * @param password Encryption password.
     * @throws Exception
     */
    public static void encryptFile(File file, String password) throws Exception {
		BasicBinaryEncryptor encryptor = new BasicBinaryEncryptor();
		encryptor.setPassword(password);
    	File tempFile = new File(file.getAbsolutePath() + "-temp");
    	DataInputStream dis = new DataInputStream(new FileInputStream(file));
    	DataOutputStream dos = new DataOutputStream(new FileOutputStream(tempFile));
    	byte[] buffer = new byte[1024];
    	int originalLength;
    	int chunks = 0;
    	
		while((originalLength = dis.read(buffer)) > 0) {
			try {
				byte[] encryptedBuffer = encryptor.encrypt(buffer);
				int encryptedLength = encryptedBuffer.length;
				
				dos.writeInt(encryptedLength);
				dos.writeInt(originalLength);
				dos.write(encryptedBuffer, 0, encryptedLength);
				
				chunks++;
			} catch(Exception e) {
				dis.close();
				dos.close();
				tempFile.delete();
				throw e;
			}
		}
		
		dos.writeInt(-chunks);
		
		dis.close();
		dos.close();
		
		file.delete();
		tempFile.renameTo(file);
    }
	
    /**
     * Decrypts a file.
     * @param file File to decrypt.
     * @param password Encryption password.
     * @return a decrypted file.
     * @throws Exception
     */
    public static File decryptFile(File file, String password) throws Exception {
		BasicBinaryEncryptor encryptor = new BasicBinaryEncryptor();
		encryptor.setPassword(password);
    	File tempFile = new File(file.getAbsolutePath() + "-temp");
    	DataInputStream dis = new DataInputStream(new FileInputStream(file));
    	DataOutputStream dos = new DataOutputStream(new FileOutputStream(tempFile));
    	int encryptedLenth, originalLength;
    	int chunks = 0;
    	
		while((encryptedLenth = dis.readInt()) > 0) {
			try {
				originalLength = dis.readInt();
		    	byte[] encryptedBuffer = new byte[encryptedLenth];
		    	dis.read(encryptedBuffer, 0, encryptedLenth);
				byte[] buffer = encryptor.decrypt(encryptedBuffer);
				dos.write(buffer, 0, originalLength);
				chunks++;
			} catch(Exception e) {
				dis.close();
				dos.close();
				tempFile.delete();
				throw e;
			}
		}
		
		if(-chunks != encryptedLenth) {
			throw new RuntimeException("Wrong chunk count.");
		}
		
		dis.close();
		dos.close();
		
		return tempFile;
    }
	
}
