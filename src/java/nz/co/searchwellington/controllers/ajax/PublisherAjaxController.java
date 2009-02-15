package nz.co.searchwellington.controllers.ajax;

import java.io.IOException;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.PublisherGuessingService;
import nz.co.searchwellington.views.AjaxXMLView;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;


public class PublisherAjaxController implements Controller {

    
    Logger log = Logger.getLogger(PublisherAjaxController.class);
    
    
    private PublisherGuessingService publisherGuessingService;

    
    public PublisherAjaxController(PublisherGuessingService publisherGuessingService) {  
        this.publisherGuessingService = publisherGuessingService;
    }


    @SuppressWarnings("unchecked")
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();                              
        mv.getModel().put("response", outputXmlSuggestions(request.getParameter("url")));       
        mv.setView(new AjaxXMLView());
        return mv;
    }

    
    /**
     * Given a url string (probably of a newsitem), return the XMLPRC XML to
     * populate the publishers dropdown menu with websites which could 
     * be the parent of this url.
     *
     * @param url
     * @return
     * @throws IOException 
     */ 
    private String outputXmlSuggestions(String url) throws IOException {
        
        // Use dom4j to create a correctly escaped XML tree.
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("publisherlist");
    
        if (url != null) {
            log.info("Looking up possible publishers for url: " + url);            
            for (Resource resource : publisherGuessingService.guessPossiblePublishersForUrl(url)) {                
                String name = new String(resource.getName());
                String id = new String(new Integer(resource.getId()).toString());
    
                Element publisher = root.addElement("publisher");
                Element idElement = publisher.addElement("id");
                idElement.setText(id);
                Element nameElement = publisher.addElement("name");
                nameElement.setText(name);
            }            
        }
    
        // Output to a string.
        StringWriter output = new StringWriter();
        try {
            document.write(output);
        } catch (IOException e) {
            log.error("Problem will building publishers ajax response.", e);
        }
        
        return output.toString(); 
    }

}
    