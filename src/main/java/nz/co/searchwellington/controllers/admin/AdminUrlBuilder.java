package nz.co.searchwellington.controllers.admin;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import nz.co.searchwellington.model.FeedNewsitem;
import nz.co.searchwellington.model.FrontEndWebsite;
import nz.co.searchwellington.model.SiteInformation;
import nz.co.searchwellington.model.SuggestionFeednewsitem;
import nz.co.searchwellington.model.frontend.FrontendResource;

public class AdminUrlBuilder {

	private SiteInformation siteInformation;

		
	public AdminUrlBuilder(SiteInformation siteInformation) {		
		this.siteInformation = siteInformation;
	}
	
	public String getResourceEditUrl(FrontendResource resource) {
		return siteInformation.getUrl() + "/edit/edit?resource=" + resource.getId();
	}
	
	public String getResourceDeleteUrl(FrontendResource frontendResource) {
		return siteInformation.getUrl() + "/edit/delete?resource=" + frontendResource.getId();
	}
	
	public String getResourceCheckUrl(FrontendResource resource) {
		return siteInformation.getUrl() + "/admin/linkchecker/add?resource=" + resource.getId();
	}
	
	public String getViewSnapshotUrl(FrontendResource resource) throws UnsupportedEncodingException {
		return siteInformation.getUrl() + "/edit/viewsnapshot?resource=" + resource.getId();	
	}
	
	public String getFeedNewsitemAcceptUrl(FeedNewsitem feednewsitem) {
		return siteInformation.getUrl() + "/edit/accept?feed=" + feednewsitem.getFeed().getId() + "&item=" + feednewsitem.getItemNumber();
	}
	
	public String getSuggestionAcceptUrl(SuggestionFeednewsitem suggestion) throws UnsupportedEncodingException {
		return siteInformation.getUrl() + "/edit/accept?url=" + URLEncoder.encode(suggestion.getUrl(), "UTF-8");		
	}
	
	public String getFeedNewsitemSuppressUrl(FeedNewsitem feednewsitem) throws UnsupportedEncodingException {
		return makeSuppressionUrl(feednewsitem.getUrl());
	}	
	public String getSuggestionSuppressUrl(SuggestionFeednewsitem suggestion) throws UnsupportedEncodingException {
		return makeSuppressionUrl(suggestion.getSuggestion().getUrl());		
	}
		
	private String makeSuppressionUrl(String url) throws UnsupportedEncodingException {
		return siteInformation.getUrl() + "/supress/supress?url=" + URLEncoder.encode(url, "UTF-8");
	}
	
	public String getFeedNewsitemUnsuppressUrl(FeedNewsitem feednewsitem) throws UnsupportedEncodingException {
		return siteInformation.getUrl() + "/supress/unsupress?url=" + URLEncoder.encode(feednewsitem.getUrl(), "UTF-8");
	}
	
	public String getPublisherAutoGatherUrl(FrontEndWebsite resource) throws UnsupportedEncodingException {
		return siteInformation.getUrl() + "/admin/gather?publisher=" + URLEncoder.encode(resource.getUrlWords(), "UTF-8");
	}
	
	public String getAddTagUrl() throws UnsupportedEncodingException {
		return siteInformation.getUrl() + "/edit/tag/submit";
	}
		
}
