package nz.co.searchwellington.repositories.elasticsearch

import nz.co.searchwellington.model.{Tag, User}
import nz.co.searchwellington.model.frontend.FrontendResource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class KeywordSearchService  @Autowired()(elasticSearchIndexer: ElasticSearchIndexer) {
  
  // TODO implement
  def getNewsitemsMatchingKeywordsCount(keywords: String, shouldShowBroken: Boolean, tag: Tag) = 0

  def getWebsitesMatchingKeywords(keywords: String, showBroken: Boolean, tag: Tag, startIndex: Int, maxItems: Int): Seq[FrontendResource] = {
    Seq.empty
  }

}
