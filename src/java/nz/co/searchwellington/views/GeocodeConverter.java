package nz.co.searchwellington.views;

import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.GeocodeImpl;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class GeocodeConverter implements Converter {

	@Override
	public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
		Geocode geocode = (Geocode) value;
		//writer.startNode("geotag");		
		writer.addAttribute("latitude", Double.toString(geocode.getLatitude()));
		writer.addAttribute("longitude", Double.toString(geocode.getLongitude()));
		//writer.endNode();
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader value, UnmarshallingContext context) {
		return null;
	}

	@Override
	public boolean canConvert(Class clazz) {
		boolean canConvert = GeocodeImpl.class == clazz;		
		return canConvert;
	}

}
