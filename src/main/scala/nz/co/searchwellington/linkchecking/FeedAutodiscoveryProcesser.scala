package nz.co.searchwellington.linkchecking

import java.net.{MalformedURLException, URL}

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.commentfeeds.{CommentFeedDetectorService, CommentFeedGuesserService}
import nz.co.searchwellington.htmlparsing.CompositeLinkExtractor
import nz.co.searchwellington.model.Resource
import nz.co.searchwellington.repositories.ResourceFactory
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.Await

@Component class FeedAutodiscoveryProcesser @Autowired()(mongoRepository: MongoRepository,
                                                         linkExtractor: CompositeLinkExtractor,
                                                         commentFeedDetector: CommentFeedDetectorService, commentFeedGuesser: CommentFeedGuesserService,
                                                         resourceFactory: ResourceFactory) extends LinkCheckerProcessor with ReasonableWaits {

  private val log = Logger.getLogger(classOf[FeedAutodiscoveryProcesser])

  override def process(checkResource: Resource, pageContent: String): Unit = {
    if (!checkResource.`type`.equals("F")) {

      if (pageContent != null) {

        val iter = linkExtractor.extractLinks(pageContent).iterator
        while (iter.hasNext) {
          var discoveredUrl = iter.next.asInstanceOf[String]
          log.info("Processing discovered url: " + discoveredUrl)

          if (!(isFullQualified(discoveredUrl))) {
            log.info("url is not fully qualified; will try to expand: " + discoveredUrl)  // TODO Really what's an example?
            try {
              val sitePrefix: String = new URL(checkResource.page.get).getHost  // TODO naked get
              discoveredUrl = "http://" + sitePrefix + discoveredUrl
              log.info("url expanded to: " + discoveredUrl)

            } catch {
              case e: MalformedURLException =>
                log.error("Invalid url", e)
            }
          }

          val isCommentFeedUrl = commentFeedDetector.isCommentFeedUrl(discoveredUrl)
          if (isCommentFeedUrl) {
            log.debug("Discovered url is a comment feed; ignoring: " + discoveredUrl)

          } else {
            val isUrlOfExistingFeed = Await.result(mongoRepository.getFeedByUrl(discoveredUrl), TenSeconds).nonEmpty
            if (!isUrlOfExistingFeed) {
              recordDiscoveredFeedUrl(checkResource, discoveredUrl)
            }
          }
        }
      }
    }
  }

  private def isFullQualified(discoveredUrl: String) = {
    discoveredUrl.startsWith("http://") || discoveredUrl.startsWith("https://")
  }

  private def recordDiscoveredFeedUrl(checkResource: Resource, discoveredFeedUrl: String): Unit = {
    val maybeExisting = Await.result(mongoRepository.getDiscoveredFeedByUrl(discoveredFeedUrl), TenSeconds)
    val discoveredFeed = maybeExisting.getOrElse(resourceFactory.createNewDiscoveredFeed(discoveredFeedUrl))
    discoveredFeed.references.add(checkResource) // This isn't great; suggests discovered feeds should be url -> page seen pairs
    mongoRepository.saveDiscoveredFeed(discoveredFeed)
  }

}
