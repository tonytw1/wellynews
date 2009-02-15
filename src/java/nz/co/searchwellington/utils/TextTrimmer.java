package nz.co.searchwellington.utils;

public class TextTrimmer {
    
    // TODO needs todo something with sentences or paragraphs.
    public static String trimToCharacterCount(String description, int count) {
        if (description.length() > count) {
            return description.substring(0, count);
        }
        return description;
     }
     
}
