package nz.co.searchwellington.geocoding.osm;

import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.geo.model.OsmId;
import uk.co.eelpieconsulting.common.geo.model.OsmType;

@Component
public class OsmIdParser {

    public OsmId parseOsmId(final String osm) {
        String[] split = osm.split("/");
        if (split.length == 2) {
            final String idString = split[0];
            final String typeString = split[1];
            return new OsmId(Long.parseLong(idString), OsmType.valueOf(typeString));
        }
        return null;
    }

}
