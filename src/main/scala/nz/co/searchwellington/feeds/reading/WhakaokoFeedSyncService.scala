package nz.co.searchwellington.feeds.reading

import com.google.common.base.Strings
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.Feed
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.Await

@Component class WhakaokoFeedSyncService @Autowired() (mongoReposity: MongoRepository, whakaokoService: WhakaokoService) extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[WhakaokoFeedSyncService])

  // @Scheduled(fixedRate = 3600000)
  def run {
    registerFeedsWithWhakaoko(Await.result(mongoReposity.getAllFeeds, TenSeconds))
  }

  private def registerFeedsWithWhakaoko(feeds: Seq[Feed]) {
    log.info("Registering feeds with whakaoko")
    feeds.map { feed =>
      feed.page.map { p =>
        if (!Strings.isNullOrEmpty(p)) {
          log.info("Registering feed with whakaoko: " + feed.title)
          whakaokoService.createFeedSubscription(p).map { createdSubscriptionId =>
            log.info("Created whakaoko feed: " + createdSubscriptionId)
          }
        }
      }
    }
    log.info("Finished registering feeds with whakaoro")
  }

}
