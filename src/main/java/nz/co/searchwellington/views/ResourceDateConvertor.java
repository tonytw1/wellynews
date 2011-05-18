package nz.co.searchwellington.views;

import nz.co.searchwellington.dates.DateFormatter;

import org.joda.time.DateTime;

import com.thoughtworks.xstream.converters.SingleValueConverter;

public class ResourceDateConvertor implements SingleValueConverter {

	
	public boolean canConvert(Class type) {		
		return type.equals(java.sql.Date.class) || type.equals(java.util.Date.class) ;
	}

	public Object fromString(String arg0) {
		return null;
	}
	
	public String toString(Object value) {
		DateTime date = new DateTime(value);	
		DateFormatter df = new DateFormatter();
		return df.formatDate(date.toDate(), DateFormatter.DAY_MONTH_YEAR_FORMAT);		
	}

	
	


}
