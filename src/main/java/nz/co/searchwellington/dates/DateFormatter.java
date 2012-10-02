package nz.co.searchwellington.dates;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class DateFormatter {
   
    public static final String DAY_MONTH_YEAR_FORMAT = "d MMM yyyy";
    public static final String TIME_DAY_MONTH_YEAR_FORMAT = "h:mm a d MMM yyyy";
    public static final String MONTH_YEAR_FORMAT = "MMMM yyyy";
    public static final String W3C_DATETIME_FORMAT = "yyyy-MM-dd'T'hh:mm:ssZZ";
    public static final String MONTH_FACET = "yyyyMM";
    
    // TODO This probably needs to be private to pull all of the date formatting strings back into this class.
    public String formatDate (Date date, String format) {        
        if (date != null) {
            SimpleDateFormat sdfOutput = new SimpleDateFormat(format);
            return sdfOutput.format(date);
        }
        return null;
    }
    
    public String formatW3CDate(Date startDate) {   
        DateTimeFormatter fmt = DateTimeFormat.forPattern(W3C_DATETIME_FORMAT);
        DateTime now = new DateTime(startDate);
        return (fmt.print(now));
    }
    
}
