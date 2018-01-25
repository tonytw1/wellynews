package nz.co.searchwellington.feeds.reading

import nz.co.searchwellington.model.Feed
import nz.co.searchwellington.model.frontend.{FrontendFeed, FrontendFeedNewsitem, FrontendImage}
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem

@Component class WhakaokoFeedItemMapper {

  def mapWhakaokoFeeditem(feed: Feed, feedItem: FeedItem): FrontendFeedNewsitem = {
    new FrontendFeedNewsitem(feedItem.getTitle, feedItem.getUrl, feedItem.getDate, feedItem.getBody, feedItem.getPlace, makeFrontendFeed(feed), if (feed != null && feed.getPublisher != null) feed.getPublisher.getName
    else null, if (feedItem.getImageUrl != null) new FrontendImage(feedItem.getImageUrl)
    else null)
  }

  private def makeFrontendFeed(feed: Feed): FrontendFeed = {
    Option(feed).map { f =>
      val frontendFeed: FrontendFeed = new FrontendFeed
      frontendFeed.setUrlWords(f.getUrlWords)
      frontendFeed
    }.getOrElse(null)
  }

}