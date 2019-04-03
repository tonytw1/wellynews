package nz.co.searchwellington.controllers.admin

import com.google.common.base.Strings
import nz.co.searchwellington.model.{Resource, SiteInformation}
import nz.co.searchwellington.model.frontend.{FrontendFeed, FrontendNewsitem, FrontendResource, FrontendWebsite}
import nz.co.searchwellington.urls.{UrlBuilder, UrlParameterEncoder}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem

@Component
class AdminUrlBuilder @Autowired()(siteInformation: SiteInformation, urlBuilder: UrlBuilder) {

  def getResourceEditUrl(resource: FrontendResource): String = {
    siteInformation.getUrl + "/edit?resource=" + resource.getId
  }

  def getResourceEditUrl(resource: Resource): String = {
    siteInformation.getUrl + "/edit?resource=" + resource.id
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

  def getFeednewsItemAcceptUrl(feed: FrontendFeed, newsitem: FrontendNewsitem): String = {
    siteInformation.getUrl + "/edit/accept?feed=" + feed.urlWords + "&url=" + UrlParameterEncoder.encode(newsitem.getUrl)
  }

  def getAcceptAllFromFeed(feed: FrontendFeed): String = {
    siteInformation.getUrl + "/admin/feed/acceptall?feed=" + feed.getUrlWords
  }

  def getFeedNewsitemSuppressUrl(newsitem: FrontendNewsitem): String = {
    siteInformation.getUrl + "/supress/supress?url=" + UrlParameterEncoder.encode(newsitem.getUrl)
  }

  def getFeedNewsitemUnsuppressUrl(newsitem: FrontendNewsitem): String = {
    siteInformation.getUrl + "/supress/unsupress?url=" + UrlParameterEncoder.encode(newsitem.getUrl)
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
