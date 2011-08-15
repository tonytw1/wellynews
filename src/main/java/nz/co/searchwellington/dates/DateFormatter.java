package nz.co.searchwellington.dates;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

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
           
    public static String formatDateToTimeZone(Date startDate) {
        final String TIMEZONE_ID = "Pacific/Auckland";
        DateTimeFormatter fmt = DateTimeFormat.forPattern("d MMM yyy h:mma");
        DateTime now = new DateTime(startDate);
        DateTime localTime = now.withZone(DateTimeZone.forID(TIMEZONE_ID));
        return (fmt.print(localTime));
    }
    
    public String formatW3CDate(Date startDate) {   
        DateTimeFormatter fmt = DateTimeFormat.forPattern(W3C_DATETIME_FORMAT);
        DateTime now = new DateTime(startDate);        
        return (fmt.print(now));
    }
    
    public String timeSince(Date then) {        
        Date now = Calendar.getInstance().getTime();
        final long deltaInMills = now.getTime() - then.getTime();

        if (deltaInMills > 0) {
            if (deltaInMills < 60 * 1000) {
                return "less than 1 minute ago";
            } else if (deltaInMills < 120 * 1000) {
                return "1 minute ago";
            } else if (deltaInMills < 60 * 1000 * 60) {
                float minutes = (deltaInMills / (1000 * 60));
                return (Math.round(minutes) + " minutes ago");
            } else if (deltaInMills < 60 * 1000 * 60 * 24){
                int hours = Math.round((deltaInMills / (1000 * 60 * 60)));
                if (hours == 1) {
                    return hours + " hour ago";
                } else {
                    return hours + " hours ago";
                }
            } else if (deltaInMills < 60 * 1000 * 60 * 24 * 7){
                int days = Math.round((deltaInMills / (1000 * 60 * 60 * 24)));
                if (days == 1) {
                    return days + " day ago";
                } else {
                    return days + " days ago";
                }
            } else {
                int weeks = Math.round((deltaInMills / (1000 * 60 * 60 * 24 * 7)));
                // TODO we have a plural class for this somewhere?
                // No it's in vm not java.
                if (weeks == 1) {
                    return weeks + " week ago";
                } else {
                    return weeks + " weeks ago";
                }
            }
        }
        return null;
    }
    
}
