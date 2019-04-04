package nz.co.searchwellington.repositories.elasticsearch

import com.sksamuel.elastic4s.http.ElasticDsl.{bool, must, termQuery}
import com.sksamuel.elastic4s.searches.queries.QueryDefinition
import nz.co.searchwellington.controllers.ShowBrokenDecisionService

trait ModeratedQueries extends ElasticFields {

  def showBrokenDecisionService: ShowBrokenDecisionService

  def withModeration(query: QueryDefinition): QueryDefinition = {

    if (!showBrokenDecisionService.shouldShowBroken) {
      /*
        if (!shouldShowBroken) {
          val contentIsOk: TermQueryBuilder = QueryBuilders.termQuery(HTTP_STATUS, "200")
          val contentIsApproved: TermQueryBuilder = QueryBuilders.termQuery(HELD, false)
          val contentIsPublic = QueryBuilders.boolQuery.must(contentIsOk).must(contentIsApproved)
          val userCanViewContent = QueryBuilders.boolQuery.minimumNumberShouldMatch(1).should(contentIsPublic)
          if (loggedInUserFilter.getLoggedInUser != null) {
            userCanViewContent.should(QueryBuilders.termQuery(OWNER, loggedInUserFilter.getLoggedInUser.getProfilename))
          }
          query.must(userCanViewContent)
        }
      */
      val contentIsOk = termQuery(HttpStatus, 200)
      val contentIsApproved = termQuery(Held, false)
      val contentIsPublic = bool {
        must(contentIsOk, contentIsApproved)
      }
      // val isContentOwner = ???
      bool {
        must(query, contentIsPublic)
      }
    } else {
      query
    }
  }

}
