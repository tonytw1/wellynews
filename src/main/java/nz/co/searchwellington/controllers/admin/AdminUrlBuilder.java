package nz.co.searchwellington.controllers.admin;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import nz.co.searchwellington.model.FrontendFeedNewsitem;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.SiteInformation;
import nz.co.searchwellington.model.UrlWordsGenerator;
import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.model.frontend.FrontendWebsite;

public class AdminUrlBuilder {

	private SiteInformation siteInformation;
	
	public AdminUrlBuilder(SiteInformation siteInformation) {		
		this.siteInformation = siteInformation;
	}
	
	public String getResourceEditUrl(FrontendResource resource) {
		String editId = null;
		if (resource.getType() == "N") {
			editId = UrlWordsGenerator.markUrlForNewsitem((Newsitem) resource);
		}
		if (resource.getType() == "F") {
			editId = "feed/" + UrlWordsGenerator.makeUrlWordsFromName(resource.getName());
		}
		if (resource.getType() == "W") {
			editId = ((FrontendWebsite) resource).getUrlWords();
		}
		if (editId != null) {
			return siteInformation.getUrl() + "/edit/edit?resource=" + editId;
		}
		return null;
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
		return siteInformation.getUrl() + "/admin/gather?publisher=" + URLEncoder.encode(resource.getUrlWords(), "UTF-8");
	}
	
	public String getAddTagUrl() throws UnsupportedEncodingException {
		return siteInformation.getUrl() + "/edit/tag/submit";
	}
		
}
