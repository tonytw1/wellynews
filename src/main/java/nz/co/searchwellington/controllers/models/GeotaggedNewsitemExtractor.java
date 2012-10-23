package nz.co.searchwellington.controllers.models;

import java.util.List;

import nz.co.searchwellington.model.FrontendFeedNewsitem;
import org.springframework.stereotype.Component;
import com.google.common.collect.Lists;

@Component
public class GeotaggedNewsitemExtractor {
	
	public List<FrontendFeedNewsitem> extractGeotaggedFeeditems(List<FrontendFeedNewsitem> feedNewsitems) {
		List<FrontendFeedNewsitem> geotaggedFeedNewsitems = Lists.newArrayList();
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
