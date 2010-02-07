package nz.co.searchwellington.utils;

import org.apache.log4j.Logger;

public class EscapeTools {
    
    Logger log = Logger.getLogger(EscapeTools.class);

    public String javascript(String input) {
        log.debug("Escaping javascript: " + input);
        return org.apache.commons.lang.StringEscapeUtils.escapeJavaScript(input);
    }
    
    public String html(String input) {        
        return org.apache.commons.lang.StringEscapeUtils.escapeHtml(input);
    }
    
}
