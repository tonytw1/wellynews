package nz.co.searchwellington.views;

import nz.co.searchwellington.dates.DateFormatter;
import nz.co.searchwellington.model.NewsitemImpl;
import nz.co.searchwellington.model.Resource;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class JSONConverter implements Converter {
	
	
	private DateFormatter dateFormatter;
	
	public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
		
		dateFormatter = new DateFormatter();
		Resource newsitem = (Resource) value;			
		writer.addAttribute("title", newsitem.getName());          
		writer.addAttribute("url", newsitem.getName());
		writer.addAttribute("date", dateFormatter.formatDate(newsitem.getDate(), DateFormatter.DAY_MONTH_YEAR_FORMAT));
		writer.addAttribute("description", newsitem.getDescription());
	}

	public Object unmarshal(HierarchicalStreamReader arg0, UnmarshallingContext arg1) {
		throw new UnsupportedOperationException();
	}

	public boolean canConvert(Class clazz) {	
		 return NewsitemImpl.class == clazz;
	}
	
}
