package nz.co.searchwellington.feeds.suggesteditems

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.{FeedItemActionDecorator, FeeditemToNewsitemService}
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.{Feed, FeedAcceptancePolicy, Newsitem, User}
import nz.co.searchwellington.repositories.SuppressionDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component class SuggestedFeeditemsService @Autowired()(rssfeedNewsitemService: RssfeedNewsitemService,
                                                        feedItemActionDecorator: FeedItemActionDecorator,
                                                        feeditemToNewsitemService: FeeditemToNewsitemService,
                                                        frontendResourceMapper: FrontendResourceMapper,
                                                        mongoRepository: MongoRepository,
                                                        suppressionDAO: SuppressionDAO) extends
  ReasonableWaits {

  private val log = Logger.getLogger(classOf[SuggestedFeeditemsService])

  private val MaximumChannelPagesToScan = 5

  /*
    Return a list of feed newsitems which an editor may wish to accept after reviewing them.
    This list should exclude feeds items which are going to be automatically accepted anyway.
    It should exclude items from ignored feeds.
    Items which have suppressed URLs should be excluded.
   */
  def getSuggestionFeednewsitems(maxItems: Int, loggedInUser: Option[User])(implicit ec: ExecutionContext): Future[Seq[FrontendResource]] = {

    def paginateChannelFeedItems(page: Int = 1, output: Seq[Newsitem] = Seq.empty, feeds: Seq[Feed]): Future[Seq[Newsitem]] = {
      log.info("Fetching filter page: " + page + "/" + output.size)
      rssfeedNewsitemService.getChannelFeedItemsDecoratedWithFeeds(page, feeds).flatMap { channelFeedItems =>
        log.info("Found " + channelFeedItems.size + " channel newsitems on page " + page)

        val suggestedChannelNewsitems = channelFeedItems.map(i => feeditemToNewsitemService.makeNewsitemFromFeedItem(i._1, i._2))

        val eventuallyFiltered = suggestedChannelNewsitems.map { newsitem =>
          val eventuallyLocalCopy = mongoRepository.getResourceByUrl(newsitem.page)
          val eventuallyIsSuppressed = suppressionDAO.isSupressed(newsitem.page)
          for {
            localCopy <- eventuallyLocalCopy
            isSuppressed <- eventuallyIsSuppressed
          } yield {
            if (localCopy.nonEmpty || isSuppressed) {
              None
            } else {
              Some(newsitem)
            }
          }
        }

        Future.sequence(eventuallyFiltered).flatMap { filterResults =>
          val suggestions = filterResults.flatten
          log.info("Adding " + suggestions.size + " to " + output.size)
          val result = output ++ suggestions
          if (result.size >= maxItems || channelFeedItems.isEmpty || page == MaximumChannelPagesToScan) {
            Future.successful(result.take(maxItems))
          } else {
            paginateChannelFeedItems(page + 1, result, feeds)
          }
        }
      }
    }

    val eventualFrontendResources = for {
        feeds <- mongoRepository.getAllFeeds()
        suggestedFeeds = feeds.filter(feed => feed.acceptance == FeedAcceptancePolicy.SUGGEST)
        suggestedFeedItems <- paginateChannelFeedItems(feeds = suggestedFeeds)
        eventualFrontendResources <- Future.sequence(suggestedFeedItems.map(r => frontendResourceMapper.mapFrontendResource(r, r.geocode)))
      } yield {
        eventualFrontendResources
      }

    eventualFrontendResources.flatMap { suggestedNewsitems =>
      feedItemActionDecorator.withFeedItemSpecificActions(suggestedNewsitems, loggedInUser)
    }
  }

}
