package nz.co.searchwellington.utils;

public class EscapeTools {

    public String javascript(String input) {
        return org.apache.commons.text.StringEscapeUtils.escapeEcmaScript(input);
    }

}
