package nz.co.searchwellington.geocoding.osm;

import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.geo.model.OsmId;
import uk.co.eelpieconsulting.common.geo.model.OsmType;

@Component
public class OsmIdParser {

	public OsmId parseOsmId(final String osm) {
		final String idString = osm.split("/")[0];
		final String typeString = osm.split("/")[1];
		return new OsmId(Long.parseLong(idString), OsmType.valueOf(typeString));
	}
	
}
