package nz.co.searchwellington.controllers;

import nz.co.searchwellington.model.SiteInformation;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.Website;

public class RssUrlBuilder {
    
    SiteInformation siteInformation;
    
    public RssUrlBuilder(SiteInformation siteInformation) {
        this.siteInformation = siteInformation;
    }

    public String getBaseRssUrl() {
        return siteInformation.getUrl() + "/rss";        
    }
       
    public String getRssUrlForPublisher(Website publisher) {
        return siteInformation.getUrl() + "/" + publisher.getUrlWords() + "/newsitems/rss";
    }
    
    public String getRssUrlForTag(Tag tag) {          
        return siteInformation.getUrl() + "/rss/tag/" + tag.getName();      
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

    public Object getRssTitleForGeotagged() {
        return siteInformation.getSitename() + " - Geotagged newitems";
    }

    public String getRssTitleForWatchlist() {
        return siteInformation.getSitename() + " - News watchlist";
    }

	public String getRssTitleForTagCombiner(Tag tag, Tag tag2) {
		return siteInformation.getSitename() + " - " + tag.getDisplayName() + " + " + tag2.getDisplayName();
	}

	public String getRssUrlForTagCombiner(Tag tag, Tag tag2) {
		return siteInformation.getUrl() + "/tag/" + tag.getName() + "+" + tag2.getName() + "/rss";
	}
    
}
