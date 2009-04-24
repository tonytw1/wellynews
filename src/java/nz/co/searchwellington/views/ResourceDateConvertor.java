package nz.co.searchwellington.views;

import java.sql.Date;

import nz.co.searchwellington.dates.DateFormatter;

import com.thoughtworks.xstream.converters.SingleValueConverter;

public class ResourceDateConvertor implements SingleValueConverter {

	
	public boolean canConvert(Class type) {		
		return type.equals(java.sql.Date.class);
	}

	public Object fromString(String arg0) {
		return null;
	}
	
	public String toString(Object value) {		
		Date date = (Date) value;
		DateFormatter df = new DateFormatter();
		return df.formatDate(date, DateFormatter.DAY_MONTH_YEAR_FORMAT);		
	}

	
	


}
