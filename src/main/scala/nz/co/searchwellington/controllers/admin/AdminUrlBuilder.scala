package nz.co.searchwellington.controllers.admin

import com.google.common.base.Strings
import nz.co.searchwellington.model.frontend.{FrontendFeed, FrontendFeedNewsitem, FrontendResource, FrontendWebsite}
import nz.co.searchwellington.model.{Feed, SiteInformation}
import nz.co.searchwellington.urls.{UrlBuilder, UrlParameterEncoder}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class AdminUrlBuilder @Autowired() (siteInformation: SiteInformation, urlBuilder: UrlBuilder) {

  def getResourceEditUrl(resource: FrontendResource): String = {
    if (resource.getId > 0) {
      return siteInformation.getUrl + "/edit?resource=" + resource.getId
    }
    else if (!Strings.isNullOrEmpty(resource.getUrlWords)) {
      return siteInformation.getUrl + "/edit?resource=" + UrlParameterEncoder.encode(resource.getUrlWords)
    }
    return null
  }

  def getResourceEditUrl(resourceId: Int): String = {
    return siteInformation.getUrl + "/edit?resource=" + resourceId
  }

  def getResourceDeleteUrl(resource: FrontendResource): String = {
    return siteInformation.getUrl + "/delete?resource=" + resource.getId
  }

  def getSaveUrl: String = {
    return siteInformation.getUrl + "/save"
  }

  def getResourceCheckUrl(resource: FrontendResource): String = {
    return siteInformation.getUrl + "/admin/linkchecker/add?resource=" + resource.getId
  }

  def getViewSnapshotUrl(resource: FrontendResource): String = {
    val resourceUrl: String = siteInformation.getUrl + "/" + resource.getUrlWords
    return resourceUrl + "/viewsnapshot"
  }

  def getFeednewsItemAcceptUrl(feed: FrontendFeed, feednewsitem: FrontendFeedNewsitem): String = {
    return siteInformation.getUrl + "/edit/accept?feed=" + feed.getUrlWords + "&url=" + UrlParameterEncoder.encode(feednewsitem.getUrl)
  }

  def getAcceptAllFromFeed(feed: Feed): String = {
    return siteInformation.getUrl + "/admin/feed/acceptall?feed=" + feed.getUrlWords
  }

  def getFeedNewsitemSuppressUrl(feednewsitem: FrontendFeedNewsitem): String = {
    return siteInformation.getUrl + "/supress/supress?url=" + UrlParameterEncoder.encode(feednewsitem.getUrl)
  }

  def getFeedNewsitemUnsuppressUrl(feednewsitem: FrontendFeedNewsitem): String = {
    return siteInformation.getUrl + "/supress/unsupress?url=" + UrlParameterEncoder.encode(feednewsitem.getUrl)
  }

  def getPublisherAutoGatherUrl(resource: FrontendWebsite): String = {
    val resourceUrl: String = urlBuilder.getResourceUrl(resource)
    if (resourceUrl != null) {
      return resourceUrl + "/gather"
    }
    return null
  }

  def getAddTagUrl: String = {
    return siteInformation.getUrl + "/edit/tag/submit"
  }
  
}