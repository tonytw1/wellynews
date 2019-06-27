package nz.co.searchwellington.linkchecking

import java.net.{MalformedURLException, URL}

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.commentfeeds.{CommentFeedDetectorService, CommentFeedGuesserService}
import nz.co.searchwellington.htmlparsing.RssLinkExtractor
import nz.co.searchwellington.model.{DiscoveredFeed, Resource}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.Await

@Component class FeedAutodiscoveryProcesser @Autowired()(mongoRepository: MongoRepository,
                                                         linkExtractor: RssLinkExtractor,
                                                         commentFeedDetector: CommentFeedDetectorService,
                                                         commentFeedGuesser: CommentFeedGuesserService) extends LinkCheckerProcessor with ReasonableWaits {

  private val log = Logger.getLogger(classOf[FeedAutodiscoveryProcesser])

  override def process(checkResource: Resource, pageContent: String, seen: DateTime): Unit = {
    if (!checkResource.`type`.equals("F")) {
      if (pageContent != null) {

        def expandUrl(url: String): String = {
          if (!isFullQualified(url)) {
            log.info("url is not fully qualified; will try to expand: " + url) // TODO Really what's an example?
            try {
              val sitePrefix = new URL(checkResource.page.get).getHost // TODO naked get
              val fullyQualifiedUrl = "http://" + sitePrefix + url // TODO protocol!
              log.info("url expanded to: " + fullyQualifiedUrl)
              fullyQualifiedUrl

            } catch {
              case e: MalformedURLException =>
                log.error("Invalid url", e)
                url
              case e: Throwable =>
                log.error("Invalid url", e)
                url
            }

          } else {
            url
          }
        }

        linkExtractor.extractLinks(pageContent).map(expandUrl).foreach { discoveredUrl =>
          log.info("Processing discovered url: " + discoveredUrl)

          val isCommentFeedUrl = commentFeedDetector.isCommentFeedUrl(discoveredUrl)
          if (isCommentFeedUrl) {
            log.info("Discovered url is a comment feed; ignoring: " + discoveredUrl)

          } else {
            val isUrlOfExistingFeed = Await.result(mongoRepository.getFeedByUrl(discoveredUrl), TenSeconds).nonEmpty
            if (!isUrlOfExistingFeed) {
              recordDiscoveredFeedUrl(checkResource, discoveredUrl, seen)

            } else {
              log.info("Ignoring discovered url of existing feed")
            }
          }
        }
      }
    }
  }

  private def isFullQualified(discoveredUrl: String): Boolean = discoveredUrl.startsWith("http://") || discoveredUrl.startsWith("https://")

  private def recordDiscoveredFeedUrl(checkResource: Resource, discoveredFeedUrl: String, seen: DateTime): Unit = {
    if (Await.result(mongoRepository.getDiscoveredFeedByUrlAndReference(discoveredFeedUrl, checkResource.page.get), TenSeconds).isEmpty) {
      mongoRepository.saveDiscoveredFeed(DiscoveredFeed(url = discoveredFeedUrl, referencedFrom = checkResource.page.get, seen = seen.toDate))
    }
  }

}
