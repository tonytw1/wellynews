package nz.co.searchwellington.views;

import org.springframework.stereotype.Component;

import nz.co.searchwellington.model.Geocode;
import uk.co.eelpieconsulting.common.geo.model.LatLong;
import uk.co.eelpieconsulting.common.geo.model.OsmId;
import uk.co.eelpieconsulting.common.geo.model.Place;

@Component
public class GeocodeToPlaceMapper {
	
	public Place mapGeocodeToPlace(final Geocode contentItemGeocode) {
		LatLong latLong = null;
		if (contentItemGeocode.getLatitude() != null && contentItemGeocode.getLongitude() != null) {
			latLong = new LatLong(contentItemGeocode.getLatitude(), contentItemGeocode.getLongitude());
		}
		OsmId osmId = null;
		if (contentItemGeocode.getOsmId() != null && contentItemGeocode.getOsmType() != null) {
			osmId = new OsmId(contentItemGeocode.getOsmId(), contentItemGeocode.getOsmType());
		}
		String displayName = contentItemGeocode.getDisplayName();
		Place place = new Place(displayName, latLong, osmId);
		return place;
	}

}
