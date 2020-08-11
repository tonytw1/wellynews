package nz.co.searchwellington.repositories.elasticsearch

import com.sksamuel.elastic4s.ElasticDsl.{must, termQuery}
import com.sksamuel.elastic4s.requests.searches.queries.Query
import nz.co.searchwellington.controllers.ShowBrokenDecisionService
import nz.co.searchwellington.model.User

trait ModeratedQueries extends ElasticFields {

  def showBrokenDecisionService: ShowBrokenDecisionService

  def withModeration(query: Query, loggedInUser: Option[User]): Query = {

    if (!showBrokenDecisionService.shouldShowBroken(loggedInUser)) {
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
      val contentIsPublic = must(contentIsOk, contentIsApproved)
      // val isContentOwner = ???
      must(query, contentIsPublic)

    } else {
      query
    }
  }

}
