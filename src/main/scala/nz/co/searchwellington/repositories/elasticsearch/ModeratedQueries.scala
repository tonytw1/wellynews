package nz.co.searchwellington.repositories.elasticsearch

import com.sksamuel.elastic4s.ElasticDsl.{must, should, termQuery}
import com.sksamuel.elastic4s.requests.searches.queries.Query
import nz.co.searchwellington.controllers.ShowBrokenDecisionService
import nz.co.searchwellington.model.User

trait ModeratedQueries extends ElasticFields {

  def showBrokenDecisionService: ShowBrokenDecisionService

  def withModeration(query: Query, loggedInUser: Option[User]): Query = {
    if (!showBrokenDecisionService.shouldShowBroken(loggedInUser)) {
      val contentIsOk = termQuery(HttpStatus, 200)
      val contentIsApproved = termQuery(Held, false)
      val contentIsPublic = must(contentIsOk, contentIsApproved)

      val moderationClause = loggedInUser.map { user: User =>
        val isContentOwner = termQuery(Owner, user._id.stringify)
        should(contentIsPublic, isContentOwner).minimumShouldMatch(1)
      }.getOrElse {
        contentIsPublic
      }
      must(query, moderationClause)

    } else {
      // No moderation required
      query
    }
  }

}
