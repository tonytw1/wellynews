package nz.co.searchwellington.utils;

import org.apache.log4j.Logger;

public class EscapeTools {
    
    Logger log = Logger.getLogger(EscapeTools.class);

    public String javascript(String input) {
        log.debug("Escaping javascript: " + input);
        return org.apache.commons.text.StringEscapeUtils.escapeEcmaScript(input);
    }
    
    public String html(String input) {        
        return org.apache.commons.text.StringEscapeUtils.escapeHtml4(input);
    }
    
}
