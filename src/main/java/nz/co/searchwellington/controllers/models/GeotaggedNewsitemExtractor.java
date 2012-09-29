package nz.co.searchwellington.controllers.models;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import nz.co.searchwellington.model.FrontendFeedNewsitem;

@Component
public class GeotaggedNewsitemExtractor {
	
	@Autowired
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
