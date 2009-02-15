package nz.co.searchwellington.mail;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.apache.log4j.Logger;

public class MailSender {

    
    Logger log = Logger.getLogger(MailSender.class);
        
    private String smtpHost;
    private String smtpUsername;
    private String smtpPassword;
    
    private String mailFromAddress;
    

    public MailSender(String smtpHost, String smtpUsername, String smtpPassword, String mailFromAddress) {
        super();
        this.smtpHost = smtpHost;
        this.smtpUsername = smtpUsername;
        this.smtpPassword = smtpPassword;
        this.mailFromAddress = mailFromAddress;
    }


    public void sendMessage(String toAddress, String subject, String body) {
        log.info("Sending message to: " + toAddress);
        
        try {
            SimpleEmail email = new SimpleEmail();
            email.setHostName(smtpHost);

            boolean isAuthenticationSet = smtpUsername != null && !smtpUsername.equals("") && smtpPassword != null && !smtpPassword.equals("");            
            if (isAuthenticationSet) {
                email.setAuthentication(smtpUsername, smtpPassword);
            }
        
            email.setFrom(mailFromAddress);             
            email.addTo(toAddress);        
            email.setSubject(subject);
            email.setMsg(body);
            email.send();
            
        } catch (EmailException e) {
            log.error("Failed to send acceptance email.", e);          
        }
        
    }
    
    
    
}
