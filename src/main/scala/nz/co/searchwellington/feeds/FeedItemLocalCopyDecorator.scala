package nz.co.searchwellington.feeds

import nz.co.searchwellington.model.Resource
import nz.co.searchwellington.model.frontend.{FeedNewsitemAcceptanceState, FeedNewsitemForAcceptance, FrontendFeedNewsitem}
import nz.co.searchwellington.repositories.{HibernateResourceDAO, SupressionDAO}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._
import scala.collection.mutable

@Component class FeedItemLocalCopyDecorator @Autowired() (resourceDAO: HibernateResourceDAO, suppressionDAO: SupressionDAO) {

  def addSupressionAndLocalCopyInformation(feedNewsitems: java.util.List[FrontendFeedNewsitem]): java.util.List[FeedNewsitemForAcceptance] = {
    val decoratedFeednewsitems: mutable.MutableList[FeedNewsitemForAcceptance] = mutable.MutableList.empty
    for (feedNewsitem <- feedNewsitems) {
      decoratedFeednewsitems += new FeedNewsitemForAcceptance(feedNewsitem, determineCurrentAcceptanceStateOf(feedNewsitem))
    }
    decoratedFeednewsitems
  }

  private def determineCurrentAcceptanceStateOf(feedNewsitem: FrontendFeedNewsitem): FeedNewsitemAcceptanceState = {
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