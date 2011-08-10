package nz.co.searchwellington.controllers.models;

import java.util.ArrayList;
import java.util.List;

import nz.co.searchwellington.model.FrontendFeedNewsitem;

public class GeotaggedNewsitemExtractor {
	
	public List<FrontendFeedNewsitem> extractGeotaggedFeeditems(List<FrontendFeedNewsitem> feedNewsitems) {
		List<FrontendFeedNewsitem> geotaggedFeedNewsitems = new ArrayList<FrontendFeedNewsitem>();
		for (FrontendFeedNewsitem feedNewsitem : feedNewsitems) {
			if (feedNewsitem.getGeocode() != null && feedNewsitem.getGeocode().isValid()) {
				geotaggedFeedNewsitems.add(feedNewsitem);
			}
		}
		if (!geotaggedFeedNewsitems.isEmpty()) {
			return geotaggedFeedNewsitems;
		}
		return null;
	}
	
}
