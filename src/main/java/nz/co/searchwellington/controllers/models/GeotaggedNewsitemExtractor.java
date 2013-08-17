package nz.co.searchwellington.controllers.models;

import java.util.List;

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
	
}
