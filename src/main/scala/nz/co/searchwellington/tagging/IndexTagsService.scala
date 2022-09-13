package nz.co.searchwellington.tagging

import nz.co.searchwellington.model.geo.Geocode
import nz.co.searchwellington.model.taggingvotes.TaggingVote
import nz.co.searchwellington.model.{Resource, Tag}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component
class IndexTagsService @Autowired()(val taggingReturnsOfficerService: TaggingReturnsOfficerService) {

  def getIndexTagsForResource(resource: Resource)(implicit ec: ExecutionContext): Future[Seq[Tag]] = {
    taggingReturnsOfficerService.getTaggingsVotesForResource(resource).map(indexTagsForTaggingVotes)
  }


  def indexTagsForTaggingVotes(taggingVotes: Seq[TaggingVote])(implicit ec: ExecutionContext): Seq[Tag] = {
    taggingVotes.map(_.tag).distinct
  }

  def getIndexGeocodeForResource(resource: Resource)(implicit ec: ExecutionContext): Future[Option[Geocode]] = {
    taggingReturnsOfficerService.getGeotagVotesForResource(resource).map { geotaggingVotes =>
      geotaggingVotes.headOption.map(_.geocode)
    }
  }

}
