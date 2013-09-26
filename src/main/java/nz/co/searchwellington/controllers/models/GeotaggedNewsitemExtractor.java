package nz.co.searchwellington.controllers.models;

import java.util.List;

import nz.co.searchwellington.model.frontend.FrontendFeedNewsitem;
import nz.co.searchwellington.model.frontend.FrontendNewsitem;

import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
public class GeotaggedNewsitemExtractor {
	
	public List<FrontendNewsitem> extractGeotaggedItems(List<FrontendNewsitem> feedNewsitems) {
		List<FrontendNewsitem> geotaggedFeedNewsitems = Lists.newArrayList();
		for (FrontendNewsitem feedNewsitem : feedNewsitems) {
			if (feedNewsitem.getPlace() != null) {
				geotaggedFeedNewsitems.add(feedNewsitem);
			}
		}
		return geotaggedFeedNewsitems;
	}

	public List<FrontendNewsitem> extractGeotaggedItemsFromFeedNewsitems(List<FrontendFeedNewsitem> feedNewsitems) {	// TODO really Java? why are we not allowed to just call the above?
		List<FrontendNewsitem> geotaggedFeedNewsitems = Lists.newArrayList();
		for (FrontendNewsitem feedNewsitem : feedNewsitems) {
			if (feedNewsitem.getPlace() != null) {
				geotaggedFeedNewsitems.add(feedNewsitem);
			}
		}
		return geotaggedFeedNewsitems;
	}
	
}
