package nz.co.searchwellington.feeds.reading

import com.google.common.base.Strings
import nz.co.searchwellington.model.Feed
import nz.co.searchwellington.repositories.HibernateResourceDAO
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component class WhakaokoFeedSyncService @Autowired() (resourceDAO: HibernateResourceDAO, whakaokoService: WhakaokoService) {

  private val log = Logger.getLogger(classOf[WhakaokoFeedSyncService])

  // @Scheduled(fixedRate = 3600000)
  def run {
    registerFeedsWithWhakaoko(resourceDAO.getAllFeeds)
  }

  private def registerFeedsWithWhakaoko(feeds: Seq[Feed]) {
    log.info("Registering feeds with whakaoko")
    feeds.map { feed =>
      feed.page.map { p =>
        if (!Strings.isNullOrEmpty(p)) {
          log.info("Registering feed with whakaoko: " + feed.title)
          whakaokoService.createFeedSubscription(p).map { createdSubscriptionId =>
            log.info("Setting feed whakaoko id to: " + createdSubscriptionId)
            feed.setWhakaokoId(createdSubscriptionId)
            resourceDAO.saveResource(feed)
          }
        }
      }
    }
    log.info("Finished registering feeds with whakaoro")
  }

}
