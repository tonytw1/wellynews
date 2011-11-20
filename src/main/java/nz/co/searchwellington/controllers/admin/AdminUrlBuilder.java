package nz.co.searchwellington.controllers.admin;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import nz.co.searchwellington.model.FrontendFeedNewsitem;
import nz.co.searchwellington.model.SiteInformation;
import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.model.frontend.FrontendWebsite;
import nz.co.searchwellington.urls.UrlBuilder;

public class AdminUrlBuilder {

	private SiteInformation siteInformation;
	private UrlBuilder urlBuilder;
	
	public AdminUrlBuilder(SiteInformation siteInformation, UrlBuilder urlBuilder) {		
		this.siteInformation = siteInformation;
		this.urlBuilder = urlBuilder;
	}
	
	public String getResourceEditUrl(FrontendResource resource) {
		final String resourceUrl = urlBuilder.getResourceUrl(resource);
		if (resourceUrl != null) {
			return resourceUrl + "/edit";
		}		
		return null;
	}
	
	public String getResourceDeleteUrl(FrontendResource resource) {
		final String resourceUrl = urlBuilder.getResourceUrl(resource);
		if (resourceUrl != null) {
			return resourceUrl + "/delete";
		}
		return null;
	}
	
	public String getResourceCheckUrl(FrontendResource resource) {
		return siteInformation.getUrl() + "/admin/linkchecker/add?resource=" + resource.getId();
	}
	
	public String getViewSnapshotUrl(FrontendResource resource) throws UnsupportedEncodingException {
		final String resourceUrl = urlBuilder.getResourceUrl(resource);
		if (resourceUrl != null) {
			return resourceUrl + "/viewsnapshot";
		}
		return null;
	}
	
	public String getFeednewsItemAcceptUrl(FrontendFeedNewsitem feednewsitem) throws UnsupportedEncodingException {
		return siteInformation.getUrl() + "/edit/accept?url=" + URLEncoder.encode(feednewsitem.getUrl(), "UTF-8");		
	}
	
	public String getFeedNewsitemSuppressUrl(FrontendFeedNewsitem feednewsitem) throws UnsupportedEncodingException {
		return siteInformation.getUrl() + "/supress/supress?url=" + URLEncoder.encode(feednewsitem.getUrl(), "UTF-8");
	}
	
	public String getFeedNewsitemUnsuppressUrl(FrontendFeedNewsitem feednewsitem) throws UnsupportedEncodingException {
		return siteInformation.getUrl() + "/supress/unsupress?url=" + URLEncoder.encode(feednewsitem.getUrl(), "UTF-8");
	}
	
	public String getPublisherAutoGatherUrl(FrontendWebsite resource) throws UnsupportedEncodingException {
		final String resourceUrl = urlBuilder.getResourceUrl(resource);
		if (resourceUrl != null) {
			return resourceUrl + "/gather";
		}
		return null;
	}
	
	public String getAddTagUrl() throws UnsupportedEncodingException {
		return siteInformation.getUrl() + "/edit/tag/submit";
	}
		
}
