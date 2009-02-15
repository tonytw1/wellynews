package nz.co.searchwellington.repositories;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class WallClock {
	
	private static final String TIMEZONE_ID = "Pacific/Auckland";
		
	public String getLocalWallClockTime() {
		DateTimeFormatter fmt = DateTimeFormat.forPattern("d MMM h:mma");
		
		DateTime now = new DateTime();		 
		DateTime localTime = now.withZone(DateTimeZone.forID(TIMEZONE_ID));				
		return (fmt.print(localTime));
	}

}
