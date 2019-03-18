package nz.co.searchwellington.feeds

import nz.co.searchwellington.model.Newsitem
import nz.co.searchwellington.model.frontend.{FeedNewsitemAcceptanceState, FeedNewsitemForAcceptance}
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.repositories.{HibernateResourceDAO, SupressionDAO}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class FeedItemLocalCopyDecorator @Autowired()(resourceDAO: HibernateResourceDAO, suppressionDAO: SupressionDAO,
                                                         frontendResourceMapper: FrontendResourceMapper) {

  def addSupressionAndLocalCopyInformation(feedNewsitems: Seq[Newsitem]): Seq[FeedNewsitemForAcceptance] = {

    def acceptanceStateOf(feedNewsitem: Newsitem): FeedNewsitemAcceptanceState = {
      val localCopyId = feedNewsitem.page.flatMap { u =>
        resourceDAO.loadResourceByUrl(u).map { lc =>
          lc.id
        }
      }

      var isSuppressed = feedNewsitem.page.map { u =>
        suppressionDAO.isSupressed(u)
      }.getOrElse(false)

      var localCopyIdAsJava: String = if (localCopyId.isEmpty) null else (localCopyId.get) // TODO not great but velocity views don't undertand Options

      new FeedNewsitemAcceptanceState(localCopyIdAsJava, isSuppressed)
    }

    feedNewsitems.map(f => new FeedNewsitemForAcceptance(frontendResourceMapper.createFrontendResourceFrom(f), acceptanceStateOf(f)))
  }

}
