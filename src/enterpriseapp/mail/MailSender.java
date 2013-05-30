package enterpriseapp.mail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import enterpriseapp.Utils;
import enterpriseapp.ui.Constants;

/**
 * Helper class to send emails. Sending account is configured in your properties files (see defaultConfiguration.properties).
 * 
 * @author Alejandro Duarte
 *
 */
public class MailSender {
	
	/**
	 * Sends an email message.
	 * @param recipient destination email.
	 * @param subject Subject of the message
	 * @param message Text of the message to send.
	 * @throws javax.mail.MessagingException
	 * @throws IOException 
	 */
	public static void send(String recipient, String subject, String message) throws javax.mail.MessagingException, IOException {
		List<String> recipients = new ArrayList<String>();
		recipients.add(recipient);
		send(recipients, subject, message);
	}
	
	/**
	 * Sends an email to multiple recipients.
	 * @param recipients Collection of destination emails.
	 * @param subject Subject of the message.
	 * @param message Text of the message to send.
	 * @throws javax.mail.MessagingException
	 * @throws IOException 
	 */
	public static void send(Collection<String> recipients, String subject, String message) throws javax.mail.MessagingException, IOException {
		send(recipients, subject, message, null, null, null);
	}
	
	/**
	 * Sends an email to multiple recipients.
	 * @param recipients Collection of destination emails.
	 * @param subject Subject of the message.
	 * @param message Text of the message to send.
	 * @param dataHandler DataHandler used to atthach a file.
	 * @param fileName Name of the file.
	 * @throws javax.mail.MessagingException
	 * @throws IOException 
	 */
	public static void send(
		Collection<String> recipients,
		String subject,
		String message,
		byte[] attachment,
		String attachmentMimeType,
		String fileName
	) throws javax.mail.MessagingException, IOException {
		
        Properties props = new Properties();
        props.put("mail.smtp.host", Constants.mailSmtpHost);
        props.put("mail.smtp.auth", "true");
        props.put("mail.debug", "true");
        props.put("mail.smtp.port", Constants.mailSmtpPort);
        props.put("mail.smtp.socketFactory.port", Constants.mailSmtpPort);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        
        props.put("mail.smtp.socketFactory.fallback", "false");
        Session session = Session.getDefaultInstance(props,
            new javax.mail.Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(Constants.mailSmtpUsername, Constants.mailSmtpPassword());
                }
            }
        );

        session.setDebug(false);

        Message msg = new MimeMessage(session);
        InternetAddress addressFrom = new InternetAddress(Constants.mailSmtpAddress);
        msg.setFrom(addressFrom);

        InternetAddress[] addressTo = new InternetAddress[recipients.size()];
        
        for (int i = 0; i < recipients.size(); i++) {
        	if(Constants.mailDeviateTo != null && !Constants.mailDeviateTo.isEmpty()) {
        		addressTo[i] = new InternetAddress(Constants.mailDeviateTo);
        	} else {
        		addressTo[i] = new InternetAddress((String) recipients.toArray()[i]);
        	}
        }
        
        msg.setRecipients(Message.RecipientType.TO, addressTo);

        msg.setSubject(subject);
        msg.setHeader("Content-Type", "text/html; charset=\"utf-8\"");
        
        MimeBodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setContent(Utils.replaceHtmlSpecialCharacters(message), "text/html");
        
        Multipart multipart = new MimeMultipart();
		multipart.addBodyPart(messageBodyPart);
        
        if(attachment != null) {
        	MimeBodyPart attachPart = new MimeBodyPart();
			attachPart.setContent(attachment, attachmentMimeType);
			attachPart.setFileName(fileName);
			
			multipart.addBodyPart(attachPart);
        }
        
        msg.setContent(multipart);
        Transport.send(msg);
	}

}
