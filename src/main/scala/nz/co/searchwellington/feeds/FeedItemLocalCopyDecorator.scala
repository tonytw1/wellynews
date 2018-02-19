package nz.co.searchwellington.feeds

import nz.co.searchwellington.model.frontend.{FeedNewsitemAcceptanceState, FeedNewsitemForAcceptance, FrontendFeedNewsitem}
import nz.co.searchwellington.repositories.{HibernateResourceDAO, SupressionDAO}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class FeedItemLocalCopyDecorator @Autowired() (resourceDAO: HibernateResourceDAO, suppressionDAO: SupressionDAO) {

  def addSupressionAndLocalCopyInformation(feedNewsitems: Seq[FrontendFeedNewsitem]): Seq[FeedNewsitemForAcceptance] = {

    def acceptanceStateOf(feedNewsitem: FrontendFeedNewsitem): FeedNewsitemAcceptanceState = {
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
