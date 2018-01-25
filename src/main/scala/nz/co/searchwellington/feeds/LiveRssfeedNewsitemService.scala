package nz.co.searchwellington.feeds

import nz.co.searchwellington.feeds.reading.WhakaokoFeedReader
import nz.co.searchwellington.model.Feed
import nz.co.searchwellington.model.frontend.FrontendFeedNewsitem
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Deprecated // TODO just inline
@Component class LiveRssfeedNewsitemService @Autowired() (whakaokoFeedReader: WhakaokoFeedReader) {

  def getFeedNewsitems (feed: Feed): Seq[FrontendFeedNewsitem] = {
    import scala.collection.JavaConversions._
    whakaokoFeedReader.fetchFeedItems (feed)
  }

}