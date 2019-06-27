package nz.co.searchwellington.feeds

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.Newsitem
import nz.co.searchwellington.model.frontend.{FeedNewsitemForAcceptance, FrontendNewsitem}
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.repositories.SuppressionDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component class FeedItemLocalCopyDecorator @Autowired()(mongoRepository: MongoRepository, suppressionDAO: SuppressionDAO,
                                                         frontendResourceMapper: FrontendResourceMapper) extends ReasonableWaits {

  def addSupressionAndLocalCopyInformation(feedNewsitems: Seq[Newsitem]): Future[Seq[FeedNewsitemForAcceptance]] = {

    def acceptanceStateOf(feedNewsitem: Newsitem): Future[FeedNewsitemForAcceptance] = {
      val eventuallyLocalCopy = feedNewsitem.page.map { u =>
        mongoRepository.getResourceByUrl(u)
      }.getOrElse {
        Future.successful(None)
      }
      val eventuallyIsSuppressed = feedNewsitem.page.map(suppressionDAO.isSupressed).getOrElse(Future.successful(false))

      eventuallyLocalCopy.flatMap { localCopy =>
        eventuallyIsSuppressed.map { isSuppressed =>
          FeedNewsitemForAcceptance(frontendResourceMapper.createFrontendResourceFrom(feedNewsitem).asInstanceOf[FrontendNewsitem],
            localCopy, isSuppressed)
        }
      }
    }

    Future.sequence(feedNewsitems.map(acceptanceStateOf))
  }

}
