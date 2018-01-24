package nz.co.searchwellington.feeds

import nz.co.searchwellington.model.Resource
import nz.co.searchwellington.model.frontend.{FeedNewsitemAcceptanceState, FeedNewsitemForAcceptance, FrontendFeedNewsitem}
import nz.co.searchwellington.repositories.{HibernateResourceDAO, SupressionDAO}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._

@Component class FeedItemLocalCopyDecorator @Autowired() (resourceDAO: HibernateResourceDAO, suppressionDAO: SupressionDAO) {

  def addSupressionAndLocalCopyInformation(feedNewsitems: Seq[FrontendFeedNewsitem]): Seq[FeedNewsitemForAcceptance] = {
    feedNewsitems.map(f => new FeedNewsitemForAcceptance(f, acceptanceStateOf(f)))
  }

  private def acceptanceStateOf(feedNewsitem: FrontendFeedNewsitem): FeedNewsitemAcceptanceState = {
    var localCopyId: Integer = null
    var isSuppressed: Boolean = false
    if (feedNewsitem.getUrl != null) {
      val localCopy: Resource = resourceDAO.loadResourceByUrl(feedNewsitem.getUrl)
      if (localCopy != null) {
        localCopyId = localCopy.getId
      }
      isSuppressed = suppressionDAO.isSupressed(feedNewsitem.getUrl)
    }
    new FeedNewsitemAcceptanceState(localCopyId, isSuppressed)
  }

}
