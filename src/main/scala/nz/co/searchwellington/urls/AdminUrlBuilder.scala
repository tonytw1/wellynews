package nz.co.searchwellington.urls

import nz.co.searchwellington.feeds.whakaoko.model.{FeedItem, Subscription}
import nz.co.searchwellington.model._
import nz.co.searchwellington.model.frontend._
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Component

@Component
class AdminUrlBuilder @Autowired()(urlBuilder: UrlBuilder, @Value("${whakaoko.url}") whakaokoUrl: String) {

  def adminPage(): String = "/admin"

  def getResourceEditUrl(resource: FrontendResource): String = {
    resource match {
      case f: FrontendFeed =>
        "/edit-feed/" + f.id
      case n: FrontendNewsitem =>
        "/edit-newsitem/" + n.id
      case w: FrontendWebsite =>
        "/edit-website/" + w.id
      case l: FrontendWatchlist =>
        "/edit-watchlist/" + l.id
    }
  }
  def getResourceEditUrl(resource: Resource): String = {
    resource match {
      case f: Feed =>
        "/edit-feed/" + f.id
      case n: Newsitem =>
        "/edit-newsitem/" + n.id
      case w: Website =>
        "/edit-website/" + w.id
      case l: Watchlist =>
        "/edit-watchlist/" + l.id
    }
  }

  def getResourceDeleteUrl(resource: FrontendResource): String = {
    "/delete-resource/" + resource.getId
  }

  def getSaveUrl: String = {
    "/save"
  }

  def getResourceCheckUrl(resource: FrontendResource): String = {
    "/check-resource/" + resource.getId
  }

  def getViewSnapshotUrl(resource: FrontendResource): String = {
    "/" + resource.getUrlWords + "/viewsnapshot"
  }

  def getFeednewsItemAcceptUrl(feed: Feed, feedItem: FrontendFeedItem): String = {
    "/accept-feed-item?feed=" + feed.url_words.get + "&url=" + UrlParameterEncoder.encode(feedItem.getUrl)
  }

  def getAcceptAllFromFeed(feed: FrontendFeed): String = {
    urlBuilder.getFeedUrl(feed) + "/accept-all"
  }

  def getFeedNewsitemSuppressUrl(newsitem: FrontendNewsitem): String = {
    "/suppress/suppress?url=" + UrlParameterEncoder.encode(newsitem.getUrl)
  }

  def getFeedNewsitemUnsuppressUrl(feedItem: FrontendFeedItem): String = {
    "/suppress/unsuppress?url=" + UrlParameterEncoder.encode(feedItem.getUrl)
  }

  def getPublisherAutoGatherUrl(publisher: FrontendWebsite): String = {
    "/admin/gather/" + publisher.id
  }

  def getAddTagUrl: String = "/new-tag"

  def getEditTagUrl(tag: Tag): String = {
    "/edit-tag/" + tag.id
  }

  def deleteTagUrl(tag: Tag): String = "/delete-tag/" + tag.id

  def getWhakaokoPreviewUrl(subscription: Subscription): String = {
    if (subscription != null) {
      whakaokoUrl + "/ui/" + "/subscriptions/" + subscription.id
    } else {
      ""
    }
  }

}
