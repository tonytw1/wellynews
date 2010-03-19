package nz.co.searchwellington.utils;

import org.apache.commons.lang.StringUtils;

public class TextTrimmer {
    
    public String trimToCharacterCount(String description, int maxLength) {
        if (description != null && description.length() > maxLength) {
        	String trimmed = description.substring(0, maxLength);
        	if (trimmed.contains(".")) {
        		return StringUtils.substringBefore(trimmed, ".") + ".";
        	}
        }
        return description;
     }
	     
}
