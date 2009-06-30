package nz.co.searchwellington.utils;

import org.apache.commons.lang.StringUtils;

public class TextTrimmer {
    
    public String trimToCharacterCount(String description, int count) {
        if (description.length() > count) {
        	String trimmed = description.substring(0, count);
        	if (trimmed.contains(".")) {
        		return StringUtils.substringBefore(trimmed, ".") + ".";
        	}
        }
        return description;
     }
	     
}
