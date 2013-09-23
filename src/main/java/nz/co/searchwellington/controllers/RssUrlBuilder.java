package nz.co.searchwellington.controllers;

import nz.co.searchwellington.model.SiteInformation;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.urls.UrlParameterEncoder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.geo.model.LatLong;
import uk.co.eelpieconsulting.common.geo.model.OsmId;
import uk.co.eelpieconsulting.common.geo.model.Place;

import com.google.common.base.Strings;

@Component
public class RssUrlBuilder {
    
    private SiteInformation siteInformation;
    
    public RssUrlBuilder() {
	}
    
    @Autowired
    public RssUrlBuilder(SiteInformation siteInformation) {
        this.siteInformation = siteInformation;
    }

    public String getBaseRssUrl() {
        return siteInformation.getUrl() + "/rss";        
    }
    
    public String getBaseRssTitle() {
    	return siteInformation.getAreaname() + " Newslog";
    }
       
    public String getRssUrlForPublisher(Website publisher) {
        return siteInformation.getUrl() + "/" + publisher.getUrlWords() + "/rss";
    }
    
    public String getRssUrlForTag(Tag tag) {          
        return siteInformation.getUrl() + "/" + tag.getName() + "/rss";
    }

    public String getRssUrlForGeotagged() {
    	return siteInformation.getUrl() + "/geotagged/rss";
	}
    
    public String getRssUrlForJustin() {
        return siteInformation.getUrl() + "/justin/rss";
    }
    
    public String getRssUrlForWatchlist() {
        return siteInformation.getUrl() + "/watchlist/rss";
    }
    
	public String getRssTitleForTag(Tag tag) {
        return siteInformation.getSitename() + " - " + tag.getDisplayName();
	}

	public String getRssTitleForPublisher(Website publisher) {
		 return publisher.getName() + " RSS Feed";
	}

    public String getRssTitleForJustin() {
        return siteInformation.getSitename() + " - Latest Additions";
    }

    public String getTitleForWatchlist() {
        return siteInformation.getSitename() + " - News Watchlist";
    }
    
    public String getRssTitleForWatchlist() {
        return siteInformation.getSitename() + " - News watchlist";
    }

	public String getRssTitleForTagCombiner(Tag tag, Tag tag2) {
		return siteInformation.getSitename() + " - " + tag.getDisplayName() + " + " + tag2.getDisplayName();
	}

	public String getRssUrlForTagCombiner(Tag tag, Tag tag2) {
		return siteInformation.getUrl() + "/" + tag.getName() + "+" + tag2.getName() + "/rss";
	}

	public String getRssTitleForPublisherCombiner(Website publisher, Tag tag) {
		return siteInformation.getSitename() + " - " + publisher.getName() + " + " + tag.getDisplayName();
	}

	public String getRssUrlForPublisherCombiner(Website publisher, Tag tag) {
		 return siteInformation.getUrl() + "/" + publisher.getUrlWords() + "+" + tag.getName() + "/rss";
	}

	public String getRssTitleForTagComment(Tag tag) {
		return siteInformation.getSitename() + " - " + tag.getDisplayName() + " comment";
	}

	public String getRssUrlForTagComment(Tag tag) {
		return siteInformation.getUrl() + "/" + tag.getName() + "/comment/rss";
	}

	public String getRssTitleForTagGeotagged(Tag tag) {
		return siteInformation.getSitename() + " - " + tag.getDisplayName() + " geotagged";
	}

	public String getRssUrlForTagGeotagged(Tag tag) {
		return siteInformation.getUrl() + "/" + tag.getName() + "/geotagged/rss";
	}
	
	public String getRssUrlForFeedSuggestions() {
		return siteInformation.getUrl() + "/feeds/inbox/rss";
	}

	public String getRssDescriptionForTag(Tag tag) {
		if (tag.getDescription() != null && !tag.getDescription().isEmpty()) {
			return tag.getDescription();
		}
		return siteInformation.getAreaname() + " related newsitems tagged with " + tag.getDisplayName();
	}

	public String getTitleForSuggestions() {
		return "Feed newsitem suggestions";
	}
	
	public String getRssTitleForPlace(Place place, double radius) {
		String placeLabel = place.toString();
		if (!Strings.isNullOrEmpty(place.getAddress())) {
			placeLabel = place.getAddress();
		} else if (place.getLatLong() != null) {
			placeLabel = place.getLatLong().toString();
		}
		return "Newsitems within " + radius + " km of " + placeLabel;
	}
	
	public String getRssUrlForPlace(Place place) {
		if (place.getOsmId() != null) {		
			return getRssUrlForOsmId(place.getOsmId());					
		} else if (place.getLatLong() != null) {
			return getRssUrlForLatLong(place.getLatLong());
		}
		return null;		
	}
	
	public String getRssTitleForGeotagged() {
		return "Geotagged newsitems";
	}
	
	public String getRssHeadingForGivenHeading(String heading) {
		return heading + " - " + siteInformation.getSitename();
	}

	public String getRssUrlForOsmId(OsmId osmId) {
		return getRssUrlForGeotagged() + "?osm=" + UrlParameterEncoder.encode(osmId.getId() + "/" + osmId.getType());
	}

	public String getRssUrlForLatLong(LatLong latLong) {
		return getRssUrlForGeotagged() + "?latitude=" + latLong.getLatitude() + "&longitude=" + latLong.getLongitude();
	}
	
}
