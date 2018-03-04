package nz.co.searchwellington.controllers.admin

import com.google.common.base.Strings
import nz.co.searchwellington.model.SiteInformation
import nz.co.searchwellington.model.frontend.{FrontendFeed, FrontendResource, FrontendWebsite}
import nz.co.searchwellington.urls.{UrlBuilder, UrlParameterEncoder}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem

@Component
class AdminUrlBuilder @Autowired()(siteInformation: SiteInformation, urlBuilder: UrlBuilder) {

  def getResourceEditUrl(resource: FrontendResource): String = {
    if (resource.getId > 0) {
      siteInformation.getUrl + "/edit?resource=" + resource.getId
    }
    else if (!Strings.isNullOrEmpty(resource.getUrlWords)) {
      siteInformation.getUrl + "/edit?resource=" + UrlParameterEncoder.encode(resource.getUrlWords)
    } else {
      null
    }
  }

  def getResourceEditUrl(resourceId: Int): String = {
    siteInformation.getUrl + "/edit?resource=" + resourceId
  }

  def getResourceDeleteUrl(resource: FrontendResource): String = {
    siteInformation.getUrl + "/delete?resource=" + resource.getId
  }

  def getSaveUrl: String = {
    siteInformation.getUrl + "/save"
  }

  def getResourceCheckUrl(resource: FrontendResource): String = {
    siteInformation.getUrl + "/admin/linkchecker/add?resource=" + resource.getId
  }

  def getViewSnapshotUrl(resource: FrontendResource): String = {
    siteInformation.getUrl + "/" + resource.getUrlWords + "/viewsnapshot"
  }

  def getFeednewsItemAcceptUrl(feed: FrontendFeed, feednewsitem: FeedItem): String = {
    siteInformation.getUrl + "/edit/accept?feed=" + feed.getUrlWords + "&url=" + UrlParameterEncoder.encode(feednewsitem.getUrl)
  }

  def getAcceptAllFromFeed(feed: FrontendFeed): String = {
    siteInformation.getUrl + "/admin/feed/acceptall?feed=" + feed.getUrlWords
  }

  def getFeedNewsitemSuppressUrl(feednewsitem: FeedItem): String = {
    siteInformation.getUrl + "/supress/supress?url=" + UrlParameterEncoder.encode(feednewsitem.getUrl)
  }

  def getFeedNewsitemUnsuppressUrl(feednewsitem: FeedItem): String = {
    siteInformation.getUrl + "/supress/unsupress?url=" + UrlParameterEncoder.encode(feednewsitem.getUrl)
  }

  def getPublisherAutoGatherUrl(resource: FrontendWebsite): String = {
    val resourceUrl: String = urlBuilder.getResourceUrl(resource)
    if (resourceUrl != null) {
      resourceUrl + "/gather"
    } else {
      null
    }
  }

  def getAddTagUrl: String = {
    siteInformation.getUrl + "/edit/tag/submit"
  }

}
