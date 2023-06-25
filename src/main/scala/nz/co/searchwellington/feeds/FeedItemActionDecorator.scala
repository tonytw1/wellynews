package nz.co.searchwellington.feeds

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.frontend.{Action, FrontendFeedItem}
import nz.co.searchwellington.model.{Feed, Resource, User}
import nz.co.searchwellington.repositories.SuppressionDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.AdminUrlBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component class FeedItemActionDecorator @Autowired()(mongoRepository: MongoRepository, suppressionDAO: SuppressionDAO,
                                                      adminUrlBuilder: AdminUrlBuilder) extends ReasonableWaits {

  def withFeedItemSpecificActions(feed: Feed, feedItem: FrontendFeedItem, loggedInUser: Option[User])(implicit ec: ExecutionContext): Future[FrontendFeedItem] = {
    def addFeedItemsActions(feedNewsitem: FrontendFeedItem): Future[FrontendFeedItem] = {
      val eventuallyLocalCopy = mongoRepository.getResourceByUrl(feedNewsitem.url)
      val eventuallyIsSuppressed = suppressionDAO.isSupressed(feedNewsitem.url)

      for {
        localCopy <- eventuallyLocalCopy
        isSuppressed <- eventuallyIsSuppressed
      } yield {
        loggedInUser.map { _ =>
          val acceptOrEditAction: Option[Action] = localCopy.map { lc =>
            Some(Action("Edit local copy", adminUrlBuilder.getResourceEditUrl(lc)))
          }.getOrElse(
            Some(Action("Accept", adminUrlBuilder.getFeednewsItemAcceptUrl(feed, feedItem)))
          )

          val unsupressAction = if (isSuppressed) {
            Some(Action("Unsuppress", adminUrlBuilder.getFeedNewsitemUnsuppressUrl(feedItem)))
          } else {
            None
          }

          val feedItemActions = Seq(acceptOrEditAction, unsupressAction).flatten
          feedItem.copy(actions = feedItemActions)

        }.getOrElse {
          feedNewsitem
        }
      }
    }

    addFeedItemsActions(feedItem)
  }

}
