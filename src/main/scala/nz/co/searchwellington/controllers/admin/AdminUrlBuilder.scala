package nz.co.searchwellington.controllers.admin

import nz.co.searchwellington.model.frontend.{FrontendFeed, FrontendNewsitem, FrontendResource, FrontendWebsite}
import nz.co.searchwellington.model.{Feed, Resource, SiteInformation, Website}
import nz.co.searchwellington.urls.{UrlBuilder, UrlParameterEncoder}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.whakaoro.client.model.Subscription

@Component
class AdminUrlBuilder @Autowired()(siteInformation: SiteInformation,
                                   urlBuilder: UrlBuilder,
                                   @Value("#{config['whakaoko.url']}") whakaokoUrl: String,
                                   @Value("#{config['whakaoko.username']}") whakaokoUsername: String) {

  def getResourceEditUrl(resource: FrontendResource): String = {
    resource match {
      case f: FrontendFeed =>
        "/edit-feed/" + f.id
      case w: FrontendWebsite =>
        "/edit-website/" + w.id
      case _ =>
        siteInformation.getUrl + "/edit?resource=" + resource.id
    }
  }

  @Deprecated
  def getResourceEditUrl(resource: Resource): String = {
    resource match {
      case f: Feed =>
        "/edit-feed/" + f.id
      case w: Website =>
        "/edit-website/" + w.id
      case _ =>
        siteInformation.getUrl + "/edit?resource=" + resource.id
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

  def getWhakaokoPreviewUrl(subscription: Subscription): String = {
    whakaokoUrl + "/ui/" + whakaokoUsername + "/subscriptions/" + subscription.getId
  }

}
