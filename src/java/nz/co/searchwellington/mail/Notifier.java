package nz.co.searchwellington.mail;

import java.io.StringWriter;

import nz.co.searchwellington.model.Resource;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.VelocityException;




public class Notifier {

    private VelocityEngine velocityEngine;
    private MailSender mailSender;
 
    Logger log = Logger.getLogger(Notifier.class);
    
    public Notifier(VelocityEngine velocityEngine, MailSender mailSender) {
        super();
        this.velocityEngine = velocityEngine;
        this.mailSender = mailSender;
    }
    

    public void sendAcceptanceNotification(String toAddress, String subject, Resource resource) {
        mailSender.sendMessage(toAddress, subject, createAcceptanceMessageBody(resource));
        ;
        
    }
    
    public void sendTaggingNotification(String toAddress, String subject, Resource editResource) {      
        mailSender.sendMessage(toAddress, subject, createPublicTaggingMessageBody(editResource));
    }
    
    public void sendSubmissionNotification(String toAddress, String subject, Resource editResource) {
        mailSender.sendMessage(toAddress, subject, createSubmissionMessageBody(editResource));     
    }
    
    
    private String createAcceptanceMessageBody(Resource accepted) {
     return processTemplate(accepted, "mail/acceptance.vm");      
    }

    private String createSubmissionMessageBody(Resource editResource) {
        return processTemplate(editResource, "mail/submission.vm"); 
    }
       
    private String createPublicTaggingMessageBody(Resource editResource) {
        return processTemplate(editResource, "mail/tagging.vm");        
    }

    
    
    
    

    private String processTemplate(Resource editResource, String templatePath) {
        try {
            Template template = velocityEngine.getTemplate(templatePath);
            VelocityContext context = new VelocityContext();
            
            context.put("title", editResource.getName());
            context.put("url", editResource.getUrl());
            context.put("description", editResource.getDescription());
            context.put("tags", editResource.getTags());
            
            StringWriter sw = new StringWriter(); template.merge( context, sw );
            return sw.toString();
                        
        } catch (ResourceNotFoundException e) {
            log.error("Problem creating templated message.", e);
        } catch (ParseErrorException e) {
            log.error("Problem creating templated message.", e);
        } catch (VelocityException e) {
            log.error("Problem creating templated message.", e);
        } catch (Exception e) {
            log.error("Problem creating templated message.", e);
        }
        return null;
    }



   
      
}
