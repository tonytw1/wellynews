package nz.co.searchwellington.linkchecking

import java.net.{MalformedURLException, URL}
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.commentfeeds.CommentFeedDetectorService
import nz.co.searchwellington.htmlparsing.RssLinkExtractor
import nz.co.searchwellington.model.{DiscoveredFeed, DiscoveredFeedOccurrence, Resource}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component class FeedAutodiscoveryProcesser @Autowired()(mongoRepository: MongoRepository,
                                                         rssLinkExtractor: RssLinkExtractor,
                                                         commentFeedDetector: CommentFeedDetectorService)
  extends LinkCheckerProcessor with ReasonableWaits {

  private val log = Logger.getLogger(classOf[FeedAutodiscoveryProcesser])

  override def process(checkResource: Resource, pageContent: Option[String], seen: DateTime)(implicit ec: ExecutionContext): Future[Boolean] = {
    if (!checkResource.`type`.equals("F")) {
      val pageUrl = new URL(checkResource.page) // TODO catch

      def expandUrl(url: String): String = {
        if (!isFullQualified(url)) {
          log.info("url is not fully qualified; will try to expand: " + url) // TODO Really what's an example?
          try {
            val sitePrefix = pageUrl.getProtocol + "://" + pageUrl.getHost
            val fullyQualifiedUrl = sitePrefix + url
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

      val newlyDiscovered: Future[Seq[String]] = Future.sequence {
        rssLinkExtractor.extractFeedLinks(pageContent.get).map(expandUrl).map { discoveredUrl =>  // TODO naked get
          log.info("Processing discovered url: " + discoveredUrl)

          if (commentFeedDetector.isCommentFeedUrl(discoveredUrl)) {
            log.info("Discovered url is a comment feed; ignoring: " + discoveredUrl)
            Future.successful(None)

          } else {
            mongoRepository.getFeedByUrl(discoveredUrl).map { maybeExistingFeed =>
              if (maybeExistingFeed.isEmpty) {
                Some(discoveredUrl)
              } else {
                None
              }
            }
          }
        }
      }.map(_.flatten)
      newlyDiscovered.flatMap { ds =>
        Future.sequence {
          ds.map { d =>
            recordDiscoveredFeedUrl(checkResource, d, seen)
          }
        }.map { _ => true }
      }

    } else {
      Future.successful(false)
    }
  }

  private def isFullQualified(discoveredUrl: String): Boolean = discoveredUrl.startsWith("http://") || discoveredUrl.startsWith("https://")

  private def recordDiscoveredFeedUrl(checkResource: Resource, discoveredFeedUrl: String, seen: DateTime)(implicit ec: ExecutionContext): Future[Boolean] = {
    val occurrence = DiscoveredFeedOccurrence(referencedFrom = checkResource.page, seen = seen.toDate)
    val discoveredFeedWithNewOccurrence = mongoRepository.getDiscoveredFeedByUrl(discoveredFeedUrl).map { maybeExisting =>
      maybeExisting.map { existing =>
        val existingOccurrences = existing.occurrences
        val occurrences = if (!existingOccurrences.exists(_.referencedFrom == checkResource.page)) {
          existingOccurrences :+ occurrence
        } else {
          existingOccurrences
        }
        existing.copy(occurrences = occurrences)
      }.getOrElse{
        DiscoveredFeed(url = discoveredFeedUrl, occurrences = Seq(occurrence), firstSeen = occurrence.seen)
      }
    }

    discoveredFeedWithNewOccurrence.flatMap { y =>
      mongoRepository.saveDiscoveredFeed(y).map{_.writeErrors.isEmpty}
    }
  }

}
