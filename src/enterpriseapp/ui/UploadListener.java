package enterpriseapp.ui;

public interface UploadListener {
	
	byte[] getBytes() throws Exception;
	void setBytes(byte[] bytes) throws Exception;
	String getFileName() throws Exception;
	
}
