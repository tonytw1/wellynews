package nz.co.searchwellington.linkchecking.processors

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.commentfeeds.CommentFeedDetectorService
import nz.co.searchwellington.htmlparsing.RssLinkExtractor
import nz.co.searchwellington.linkchecking.UrlWrangling
import nz.co.searchwellington.model._
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.commons.logging.LogFactory
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.net.{URI, URL}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Component class FeedAutodiscoveryProcessor @Autowired()(mongoRepository: MongoRepository,
                                                         rssLinkExtractor: RssLinkExtractor,
                                                         commentFeedDetector: CommentFeedDetectorService)
  extends LinkCheckerProcessor with ReasonableWaits with UrlWrangling {

  private val log = LogFactory.getLog(classOf[FeedAutodiscoveryProcessor])

  override def process(checkResource: Resource, pageContent: Option[String], seen: DateTime)(implicit ec: ExecutionContext): Future[Resource] = {
    if (!checkResource.`type`.equals("F")) {
      val pageUrl = new URL(checkResource.page) // TODO catch

      def expandRelativeUrls(url: String): Option[URL] = {
        Try(new URI(url)).map { uri =>
          if (!isFullQualifiedUrl(uri)) {
            // TODO Is this really happening; what's an example?
            log.info("url is not fully qualified; will try to expand: " + url)
            expandUrlRelativeFrom(uri, pageUrl).toURL
          } else {
            uri.toURL
          }
        }.toOption
      }

      val extractedRssLinkUrls = {
        pageContent.map { html =>
          rssLinkExtractor.extractFeedLinks(html).flatMap(expandRelativeUrls)
        }.getOrElse(Seq.empty)
      }

      val newlyDiscovered: Future[Seq[String]] = Future.sequence {
        extractedRssLinkUrls.map { discoveredUrl =>
          log.info("Processing discovered url: " + discoveredUrl)

          if (commentFeedDetector.isCommentFeedUrl(discoveredUrl, checkResource)) {
            log.info("Discovered url is a comment feed; ignoring: " + discoveredUrl)
            Future.successful(None)

          } else {
            val httpAndHttpsVersions = Seq("http", "https").map { protocol =>
              new URL(protocol, discoveredUrl.getHost, discoveredUrl.getPort,
                discoveredUrl.getFile)
            }
            Future.sequence {
              httpAndHttpsVersions.map { url =>
                mongoRepository.getFeedByUrl(url.toString)
              }
            }.map { existingFeeds =>
              if (existingFeeds.flatten.isEmpty) {
                Some(discoveredUrl.toString)
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
        }.map { _ => checkResource }
      }

    } else {
      Future.successful(checkResource)
    }
  }

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
        val publisher = checkResource match {
          case published: PublishedResource => published.publisher
          case publisher: Website => Some(publisher._id)
          case _ => None
        }
        DiscoveredFeed(url = discoveredFeedUrl, occurrences = Seq(occurrence), firstSeen = occurrence.seen, publisher = publisher)
      }
    }

    discoveredFeedWithNewOccurrence.flatMap { y =>
      mongoRepository.saveDiscoveredFeed(y).map{_.writeErrors.isEmpty}
    }
  }

}
