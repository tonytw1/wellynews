package nz.co.searchwellington.feeds

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.admin.AdminUrlBuilder
import nz.co.searchwellington.model.User
import nz.co.searchwellington.model.frontend.{Action, FrontendNewsitem, FrontendResource}
import nz.co.searchwellington.repositories.SuppressionDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component class FeedItemLocalCopyDecorator @Autowired()(mongoRepository: MongoRepository, suppressionDAO: SuppressionDAO,
                                                         adminUrlBuilder: AdminUrlBuilder) extends ReasonableWaits {

  def withFeedItemSpecificActions(feedNewsitems: Seq[FrontendResource], loggedInUser: Option[User]): Future[Seq[FrontendResource]] = {
    def addFeedItemsActions(feedNewsitem: FrontendResource): Future[FrontendResource] = {
      val eventuallyLocalCopy = mongoRepository.getResourceByUrl(feedNewsitem.url)
      val eventuallyIsSuppressed = suppressionDAO.isSupressed(feedNewsitem.url)

      for {
        localCopy <- eventuallyLocalCopy
        isSupressed <- eventuallyIsSuppressed
      } yield {
        feedNewsitem match {
          case n: FrontendNewsitem =>
            loggedInUser.map { l =>
              val acceptOrEditAction = localCopy.map { lc =>
                Action("Edit local copy", adminUrlBuilder.getResourceEditUrl(lc))
              }.getOrElse(
                Action("Accept", adminUrlBuilder.getFeednewsItemAcceptUrl(n.acceptedFrom.get, n))
              )

              val feedItemActions = Seq(acceptOrEditAction)
              n.copy(actions = feedItemActions)

            }.getOrElse{
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
