package nz.co.searchwellington.views;

import java.io.IOException;
import java.text.SimpleDateFormat;

import nz.co.searchwellington.dates.DateFormatter;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;

public class JsonSerializer {
	
	private ObjectMapper mapper;
	
	public JsonSerializer() {
		mapper = new ObjectMapper();
		mapper.setDateFormat(new SimpleDateFormat(DateFormatter.DAY_MONTH_YEAR_FORMAT));
		mapper.configure(Feature.WRITE_NULL_PROPERTIES, false);
	}
	
	public String serialize(Object object) {		
		try {
			return mapper.writeValueAsString(object);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
}
