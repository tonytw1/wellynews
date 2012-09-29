package nz.co.searchwellington.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.SiteInformation;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.Website;

@Component
public class RssUrlBuilder {
    
    private SiteInformation siteInformation;
    
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
	
	public String getRssUrlForGeotagged(Double latitude, Double longitude) {
		return getRssUrlForGeotagged() + "?latitude=" + latitude + "&longitude=" + longitude;
	}

	public String getRssTitleForGeotagged(Geocode userSuppliedLocation) {
		return "Newsitems near " + userSuppliedLocation.toString();
	}
	
	public String getRssUrlForGeotagged(String location) {
		return getRssUrlForGeotagged() + "?location=" + location;
	}

	public String getRssTitleForGeotagged() {
		return "Geotagged newsitems";
	}
	
	public String getRssHeadingForGivenHeading(String heading) {
		return heading + " - " + siteInformation.getSitename();
	}
		
}
