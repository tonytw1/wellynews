package nz.co.searchwellington.views;

import java.text.DateFormat;
import java.util.Date;

import nz.co.searchwellington.dates.DateFormatter;



import com.sun.org.apache.xerces.internal.impl.dv.xs.DateDV;
import com.thoughtworks.xstream.converters.SingleValueConverter;

public class ResourceDateConvertor implements SingleValueConverter {

	@Override
	public boolean canConvert(Class type) {
		return type.equals(Date.class);
	}

	@Override
	public Object fromString(String arg0) {
		return null;
	}

	@Override
	public String toString(Object value) {
		Date date = (Date) value;
		DateFormatter df = new DateFormatter();
		return df.formatDate(date, DateFormatter.DAY_MONTH_YEAR_FORMAT);
	}

	
	


}
