package nz.co.searchwellington.feeds.reading

import nz.co.searchwellington.model.Feed
import nz.co.searchwellington.model.frontend.{FrontendFeed, FrontendImage}
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem

@Component class WhakaokoFeedItemMapper {

  def mapWhakaokoFeeditem(feed: Feed, feedItem: FeedItem): FeedItem = {
    /*
    var publisherName = null // if (feed != null && feed.getPublisher != null) feed.getPublisher.getName else null
    var imageUrl = null //if (feedItem.getImageUrl != null) new FrontendImage(feedItem.getImageUrl) else null
    new FrontendFeedNewsitem(feedItem.getTitle, feedItem.getUrl, feedItem.getDate, feedItem.getBody, feedItem.getPlace, makeFrontendFeed(feed),
      publisherName,
      imageUrl
    )
    */
    feedItem
  }

  private def makeFrontendFeed(feed: Feed): Option[FrontendFeed] = {
    Option(feed).map { f =>
      FrontendFeed(urlWords = f.url_words.getOrElse(""))
    }
  }

}
