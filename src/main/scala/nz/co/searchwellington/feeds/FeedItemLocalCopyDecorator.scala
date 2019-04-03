package nz.co.searchwellington.feeds

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.Newsitem
import nz.co.searchwellington.model.frontend.{FeedNewsitemForAcceptance, FrontendNewsitem}
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.repositories.SupressionDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.Await

@Component class FeedItemLocalCopyDecorator @Autowired()(mongoRepository: MongoRepository, suppressionDAO: SupressionDAO,
                                                         frontendResourceMapper: FrontendResourceMapper) extends ReasonableWaits {

  def addSupressionAndLocalCopyInformation(feedNewsitems: Seq[Newsitem]): Seq[FeedNewsitemForAcceptance] = {

    def acceptanceStateOf(feedNewsitem: Newsitem): FeedNewsitemForAcceptance = {
      val localCopy = feedNewsitem.page.flatMap { u =>
        Await.result(mongoRepository.getResourceByUrl(u), TenSeconds)
      }
      val isSuppressed = feedNewsitem.page.exists(suppressionDAO.isSupressed)
      FeedNewsitemForAcceptance(frontendResourceMapper.createFrontendResourceFrom(feedNewsitem).asInstanceOf[FrontendNewsitem],
        localCopy, isSuppressed)
    }

    feedNewsitems.map(acceptanceStateOf)
  }

}
