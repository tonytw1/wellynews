package nz.co.searchwellington.feeds

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.User
import nz.co.searchwellington.model.frontend.{Action, FrontendNewsitem, FrontendResource}
import nz.co.searchwellington.repositories.SuppressionDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.AdminUrlBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component class FeedItemActionDecorator @Autowired()(mongoRepository: MongoRepository, suppressionDAO: SuppressionDAO,
                                                      adminUrlBuilder: AdminUrlBuilder) extends ReasonableWaits {

  def withFeedItemSpecificActions(feedNewsitems: Seq[FrontendResource], loggedInUser: Option[User])(implicit ec: ExecutionContext): Future[Seq[FrontendResource]] = {
    def addFeedItemsActions(feedNewsitem: FrontendResource): Future[FrontendResource] = {
      val eventuallyLocalCopy = mongoRepository.getResourceByUrl(feedNewsitem.url)
      val eventuallyIsSuppressed = suppressionDAO.isSupressed(feedNewsitem.url)

      for {
        localCopy <- eventuallyLocalCopy
        isSuppressed <- eventuallyIsSuppressed
      } yield {
        feedNewsitem match {
          case n: FrontendNewsitem =>
            loggedInUser.map { _ =>
              val acceptOrEditAction: Option[Action] = localCopy.map { lc =>
                Some(Action("Edit local copy", adminUrlBuilder.getResourceEditUrl(lc)))
              }.getOrElse(
                Some(Action("Accept", adminUrlBuilder.getFeednewsItemAcceptUrl(n.acceptedFrom.get, n)))
              )

              val unsupressAction = if (isSuppressed) {
                Some(Action("Unsuppress", adminUrlBuilder.getFeedNewsitemUnsuppressUrl(n)))
              } else {
                None
              }

              val feedItemActions = Seq(acceptOrEditAction, unsupressAction).flatten
              n.copy(actions = feedItemActions)

            }.getOrElse {
              feedNewsitem
            }

          case _ =>
            feedNewsitem
        }
      }
    }

    Future.sequence(feedNewsitems.map(addFeedItemsActions))
  }

}
