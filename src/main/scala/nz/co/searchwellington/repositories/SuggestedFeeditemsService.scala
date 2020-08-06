package nz.co.searchwellington.repositories

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.whakaoko.WhakaokoService
import nz.co.searchwellington.feeds.{FeedItemLocalCopyDecorator, FeeditemToNewsitemService, RssfeedNewsitemService}
import nz.co.searchwellington.model.{Newsitem, User}
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component class SuggestedFeeditemsService @Autowired()(rssfeedNewsitemService: RssfeedNewsitemService,
                                                        feedItemLocalCopyDecorator: FeedItemLocalCopyDecorator,
                                                        feeditemToNewsitemService: FeeditemToNewsitemService,
                                                        frontendResourceMapper: FrontendResourceMapper,
                                                        mongoRepository: MongoRepository, suppressionDAO: SuppressionDAO,
                                                        whakaokoService: WhakaokoService) extends
  ReasonableWaits {

  private val log = Logger.getLogger(classOf[SuggestedFeeditemsService])

  private val MaximumChannelPagesToScan = 5

  def getSuggestionFeednewsitems(maxItems: Int, loggedInUser: Option[User])(implicit ec: ExecutionContext): Future[Seq[FrontendResource]] = {

    whakaokoService.getSubscriptions.flatMap { subscriptions =>

      def paginateChannelFeedItems(page: Int = 1, output: Seq[FrontendResource] = Seq.empty): Future[Seq[FrontendResource]] = {
        log.info("Fetching filter page: " + page + "/" + output.size)
        rssfeedNewsitemService.getChannelFeedItems(page, subscriptions).flatMap { channelFeedItems =>
          log.info("Found " + channelFeedItems.size + " channel newsitems on page " + page)

          val channelNewsitems = channelFeedItems.map(i => feeditemToNewsitemService.makeNewsitemFromFeedItem(i._1, i._2))

          val eventuallyFiltered = channelNewsitems.map { newsitem =>
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
            val eventualFrontendChannelResources: Future[Seq[FrontendResource]] = Future.sequence {
              val filtered = filterResults.flatten
              log.info("After filtering out those with local copies: " + filtered.size)
              filtered.map { r =>
                frontendResourceMapper.mapFrontendResource(r, r.geocode)
              }
            }

            eventualFrontendChannelResources.flatMap { rs =>
              feedItemLocalCopyDecorator.withFeedItemSpecificActions(rs, loggedInUser).flatMap { suggestions =>
                log.info("Adding " + suggestions.size + " to " + output.size)
                val result = output ++ suggestions
                if (result.size >= maxItems || channelFeedItems.isEmpty || page == MaximumChannelPagesToScan) {
                  Future.successful(result.take(maxItems))
                } else {
                  paginateChannelFeedItems(page + 1, result)
                }
              }
            }
          }
        }
      }

      paginateChannelFeedItems(1)
    }
  }

}
