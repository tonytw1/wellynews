package nz.co.searchwellington.controllers.admin;

import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.FrontendFeedNewsitem;
import nz.co.searchwellington.model.SiteInformation;
import nz.co.searchwellington.model.frontend.FrontendFeed;
import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.model.frontend.FrontendWebsite;
import nz.co.searchwellington.urls.UrlBuilder;
import nz.co.searchwellington.urls.UrlParameterEncoder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

@Component
public class AdminUrlBuilder {
	
	private SiteInformation siteInformation;
	private UrlBuilder urlBuilder;
	
	@Autowired
	public AdminUrlBuilder(SiteInformation siteInformation, UrlBuilder urlBuilder) {		
		this.siteInformation = siteInformation;
		this.urlBuilder = urlBuilder;
	}
	
	public String getResourceEditUrl(FrontendResource resource) {
		if (resource.getId() > 0) {
			return siteInformation.getUrl() + "/edit?resource=" + resource.getId();
		} else if (!Strings.isNullOrEmpty(resource.getUrlWords())) {
			return siteInformation.getUrl() + "/edit?resource=" + UrlParameterEncoder.encode(resource.getUrlWords());
		}
		return null;
	}
	
	public String getResourceDeleteUrl(FrontendResource resource) {
		return siteInformation.getUrl() + "/delete?resource=" + resource.getId();		
	}
	
	public String getResourceCheckUrl(FrontendResource resource) {
		return siteInformation.getUrl() + "/admin/linkchecker/add?resource=" + resource.getId();
	}
	
	public String getViewSnapshotUrl(FrontendResource resource) {
		final String resourceUrl = siteInformation.getUrl() + "/" + resource.getUrlWords();
		return resourceUrl + "/viewsnapshot";	
	}
	
	public String getFeednewsItemAcceptUrl(FrontendFeed feed, FrontendFeedNewsitem feednewsitem) {
		return siteInformation.getUrl() + "/edit/accept?feed=" + feed.getUrlWords() + "&url=" + UrlParameterEncoder.encode(feednewsitem.getUrl());		
	}
	
	public String getAcceptAllFromFeed(Feed feed) {
		return siteInformation.getUrl() + "/admin/feed/acceptall?feed=" + feed.getUrlWords();		
	}
	
	public String getDecacheFeed(Feed feed) {
		return siteInformation.getUrl() + "/admin/feed/decache?feed=" + feed.getUrlWords();		
	}
	
	public String getFeedNewsitemSuppressUrl(FrontendFeedNewsitem feednewsitem) {
		return siteInformation.getUrl() + "/supress/supress?url=" + UrlParameterEncoder.encode(feednewsitem.getUrl());
	}
	
	public String getFeedNewsitemUnsuppressUrl(FrontendFeedNewsitem feednewsitem) {
		return siteInformation.getUrl() + "/supress/unsupress?url=" + UrlParameterEncoder.encode(feednewsitem.getUrl());
	}
	
	public String getPublisherAutoGatherUrl(FrontendWebsite resource) {
		final String resourceUrl = urlBuilder.getResourceUrl(resource);
		if (resourceUrl != null) {
			return resourceUrl + "/gather";
		}
		return null;
	}
	
	public String getAddTagUrl() {
		return siteInformation.getUrl() + "/edit/tag/submit";
	}
	
}
