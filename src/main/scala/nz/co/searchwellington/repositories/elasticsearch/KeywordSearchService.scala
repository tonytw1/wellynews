package nz.co.searchwellington.repositories.elasticsearch

import nz.co.searchwellington.model.frontend.FrontendWebsite
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.Future

@Component class KeywordSearchService  @Autowired()(elasticSearchIndexer: ElasticSearchIndexer) {

  // TODO reimplement
  def getWebsitesMatchingKeywords(keywords: String, showBroken: Boolean, startIndex: Int, maxItems: Int): Future[Seq[FrontendWebsite]] = {
    Future.successful(Seq.empty)
  }

}
