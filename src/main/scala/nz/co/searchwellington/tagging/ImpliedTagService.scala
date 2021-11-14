package nz.co.searchwellington.tagging

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.taggingvotes.TaggingVote
import nz.co.searchwellington.model.{Tag, Tagged}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component
class ImpliedTagService @Autowired()(taggingReturnsOfficerService: TaggingReturnsOfficerService)
  extends ReasonableWaits {

  def alreadyHasTag(resource: Tagged, tag: Tag)(implicit ec: ExecutionContext): Future[Boolean] = {
    taggingReturnsOfficerService.getTaggingsVotesForResource(resource).map { existingVotes: Seq[TaggingVote] =>
      existingVotes.map(_.tag).contains(tag)
    }
  }

}
