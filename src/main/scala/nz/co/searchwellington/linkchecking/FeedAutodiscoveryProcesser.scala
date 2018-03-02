package nz.co.searchwellington.linkchecking

import java.net.{MalformedURLException, URL}

import nz.co.searchwellington.commentfeeds.{CommentFeedDetectorService, CommentFeedGuesserService}
import nz.co.searchwellington.htmlparsing.CompositeLinkExtractor
import nz.co.searchwellington.model.{CommentFeed, DiscoveredFeed, Resource}
import nz.co.searchwellington.repositories.{HibernateResourceDAO, ResourceFactory}
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class FeedAutodiscoveryProcesser @Autowired()(val resourceDAO: HibernateResourceDAO, val linkExtractor: CompositeLinkExtractor, val commentFeedDetector: CommentFeedDetectorService, val commentFeedGuesser: CommentFeedGuesserService, val resourceFactory: ResourceFactory) extends LinkCheckerProcessor {

  private val log = Logger.getLogger(classOf[FeedAutodiscoveryProcesser])

  override def process(checkResource: Resource, pageContent: String): Unit = {
    if (!checkResource.`type`.equals("F")) {

      if (pageContent != null) {

        val iter = linkExtractor.extractLinks(pageContent).iterator
        while (iter.hasNext) {
          var discoveredUrl = iter.next.asInstanceOf[String]
          log.info("Processing discovered url: " + discoveredUrl)

          if (!(discoveredUrl.startsWith("http://"))) {
            log.info("url is not fully qualified: " + discoveredUrl)
            try {
              val sitePrefix: String = new URL(checkResource.page.get).getHost  // TODO naked get
              discoveredUrl = "http://" + sitePrefix + discoveredUrl
              log.info("url expanded to: " + discoveredUrl)
            } catch {
              case e: MalformedURLException =>
                log.error("Invalid url", e)
            }
          }

          val isCommentFeedUrl: Boolean = commentFeedDetector.isCommentFeedUrl(discoveredUrl)
          if (isCommentFeedUrl) {
            log.debug("Discovered url is a comment feed: " + discoveredUrl)
            if (checkResource.`type`.equals("N")) {
              recordCommentFeed(checkResource, discoveredUrl)
            }

          } else {
            val isUrlOfExistingFeed: Boolean = resourceDAO.loadFeedByUrl(discoveredUrl) != null
            if (!(isUrlOfExistingFeed)) {
              recordDiscoveredFeedUrl(checkResource, discoveredUrl)
            }
            else {
              log.debug("Not recording discovered feed as there is currently a feed of the same url: " + discoveredUrl)
            }
          }
        }

        if (checkResource.`type`.equals("N")) {
          addGuessedCommentFeeds(checkResource)
        }

      }
    }
  }

  // TODO merge this with the discoveredFeedUrl method.
  private def recordCommentFeed(checkResource: Resource, commentFeedUrl: String): Unit = {
    log.info("Recording comment feed url for '" + checkResource.title + "': " + commentFeedUrl)
    // TODO can hibernate take care of this?
    var commentFeed: CommentFeed = resourceDAO.loadCommentFeedByUrl(commentFeedUrl)
    if (commentFeed == null) {
      log.debug("Comment feed url was not found in the database. Creating new comment feed: " + commentFeedUrl)
      commentFeed = resourceFactory.createNewCommentFeed(commentFeedUrl)
      resourceDAO.saveCommentFeed(commentFeed)
    }
    // TODO ((Newsitem) checkResource).setCommentFeed(commentFeed);
  }

  private def recordDiscoveredFeedUrl(checkResource: Resource, discoveredFeedUrl: String): Unit = {
    var discoveredFeed: DiscoveredFeed = resourceDAO.loadDiscoveredFeedByUrl(discoveredFeedUrl)
    if (discoveredFeed == null) {
      log.info("Recording newly discovered feed url: " + discoveredFeedUrl)
      discoveredFeed = resourceFactory.createNewDiscoveredFeed(discoveredFeedUrl)
    }
    discoveredFeed.getReferences.add(checkResource)
    resourceDAO.saveDiscoveredFeed(discoveredFeed)
  }

  private def addGuessedCommentFeeds(checkResource: Resource): Unit = {
    val commentFeedUrl = commentFeedGuesser.guessCommentFeedUrl(checkResource.page.get) // TODO naked get
    if (commentFeedUrl != null) {
      recordCommentFeed(checkResource, commentFeedUrl)
    }
  }

}
