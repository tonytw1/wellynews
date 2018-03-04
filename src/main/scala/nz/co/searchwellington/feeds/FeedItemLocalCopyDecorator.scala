package nz.co.searchwellington.feeds

import nz.co.searchwellington.model.frontend.{FeedNewsitemAcceptanceState, FeedNewsitemForAcceptance}
import nz.co.searchwellington.repositories.{HibernateResourceDAO, SupressionDAO}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem

@Component class FeedItemLocalCopyDecorator @Autowired() (resourceDAO: HibernateResourceDAO, suppressionDAO: SupressionDAO) {

  def addSupressionAndLocalCopyInformation(feedNewsitems: Seq[FeedItem]): Seq[FeedNewsitemForAcceptance] = {

    def acceptanceStateOf(feedNewsitem: FeedItem): FeedNewsitemAcceptanceState = {
      val feedNewsitemUrl = Option(feedNewsitem.getUrl)

      val localCopyId: Option[Int] = feedNewsitemUrl.flatMap { u =>
        resourceDAO.loadResourceByUrl(feedNewsitem.getUrl).map { lc =>
          lc.id
        }
      }

      var isSuppressed = feedNewsitemUrl.map { u =>
        suppressionDAO.isSupressed(u)
      }.getOrElse(false)

      var localCopyIdAsJava: Integer = if (localCopyId.isEmpty) null else (localCopyId.get) // TODO not great but velocity views don't undertand Options

      new FeedNewsitemAcceptanceState(localCopyIdAsJava, isSuppressed)
    }

    feedNewsitems.map(f => new FeedNewsitemForAcceptance(f, acceptanceStateOf(f)))
  }

}
