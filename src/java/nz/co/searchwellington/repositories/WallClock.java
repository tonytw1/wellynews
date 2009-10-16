package nz.co.searchwellington.repositories;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class WallClock {
	
	private static final String TIMEZONE_ID = "Pacific/Auckland";
	
	private DateTime getLocalTime() {
		DateTime now = new DateTime();		 
		DateTime localTime = now.withZone(DateTimeZone.forID(TIMEZONE_ID));
		return localTime;
	}
		
	public String getLocalWallClockTime() {
		DateTimeFormatter fmt = DateTimeFormat.forPattern("d MMM h:mma");						
		return (fmt.print(getLocalTime()));
	}
		
	public boolean isCurrentlyDaytime() {
		DateTime localTime = getLocalTime();
		return localTime.getHourOfDay() >=  8 && localTime.getHourOfDay() <= 21;		
	}

}
