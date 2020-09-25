package nz.co.searchwellington.controllers.admin

import nz.co.searchwellington.feeds.whakaoko.model.Subscription
import nz.co.searchwellington.model.frontend.{FrontendFeed, FrontendNewsitem, FrontendResource, FrontendWebsite}
import nz.co.searchwellington.model._
import nz.co.searchwellington.urls.{UrlBuilder, UrlParameterEncoder}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Component

@Component
class AdminUrlBuilder @Autowired()(urlBuilder: UrlBuilder,
                                   @Value("${whakaoko.url}") whakaokoUrl: String,
                                   @Value("${whakaoko.username}") whakaokoUsername: String) {

  def adminPage(): String = "/admin"

  def getResourceEditUrl(resource: FrontendResource): String = {
    resource match {
      case f: FrontendFeed =>
        "/edit-feed/" + f.id
      case n: FrontendNewsitem =>
        "/edit-newsitem/" + n.id
      case w: FrontendWebsite =>
        "/edit-website/" + w.id
      case _ =>
        "/edit?resource=" + resource.id
    }
  }

  @Deprecated
  def getResourceEditUrl(resource: Resource): String = {
    resource match {
      case f: Feed =>
        "/edit-feed/" + f.id
      case n: Newsitem =>
        "/edit-newsitem/" + n.id
      case w: Website =>
        "/edit-website/" + w.id
      case _ =>
        "/edit?resource=" + resource.id
    }
  }

  def getResourceEditUrl(resourceId: Int): String = {
    "/edit?resource=" + resourceId
  }

  def getResourceDeleteUrl(resource: FrontendResource): String = {
    "/delete?resource=" + resource.getId
  }

  def getSaveUrl: String = {
    "/save"
  }

  def getResourceCheckUrl(resource: FrontendResource): String = {
    "/admin/linkchecker/add?resource=" + resource.getId
  }

  def getViewSnapshotUrl(resource: FrontendResource): String = {
    "/" + resource.getUrlWords + "/viewsnapshot"
  }

  def getFeednewsItemAcceptUrl(feed: FrontendFeed, newsitem: FrontendNewsitem): String = {
    "/accept-feed-item?feed=" + feed.urlWords + "&url=" + UrlParameterEncoder.encode(newsitem.getUrl)
  }

  def getAcceptAllFromFeed(feed: FrontendFeed): String = {
    "/admin/feed/accept-all?feed=" + feed.getUrlWords
  }

  def getFeedNewsitemSuppressUrl(newsitem: FrontendNewsitem): String = {
    "/suppress/suppress?url=" + UrlParameterEncoder.encode(newsitem.getUrl)
  }

  def getFeedNewsitemUnsuppressUrl(newsitem: FrontendNewsitem): String = {
    "/suppress/unsuppress?url=" + UrlParameterEncoder.encode(newsitem.getUrl)
  }

  def getPublisherAutoGatherUrl(resource: FrontendWebsite): String = {
    Option(urlBuilder.getResourceUrl(resource)).map(_ + "/gather").orNull
  }

  def getAddTagUrl: String = "/new-tag"

  def getEditTagUrl(tag: Tag): String = {
    "/edit-tag/" + tag.id
  }

  def getWhakaokoPreviewUrl(subscription: Subscription): String = {
    if (subscription != null) {
      whakaokoUrl + "/ui/" + "/subscriptions/" + subscription.id
    } else {
      ""
    }
  }

}
