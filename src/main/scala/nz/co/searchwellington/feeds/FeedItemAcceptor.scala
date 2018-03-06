package nz.co.searchwellington.feeds

import nz.co.searchwellington.model.{Feed, Newsitem, User}
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem

@Component class FeedItemAcceptor @Autowired() (feeditemToNewsItemSerice: FeeditemToNewsitemService) {

  private val log = Logger.getLogger(classOf[FeedItemAcceptor])

  def acceptFeedItem(user: User, feeditem: FeedItem, feed: Feed): Newsitem = {
    feeditemToNewsItemSerice.makeNewsitemFromFeedItem(feed, feeditem) // TODO user
  }

}
