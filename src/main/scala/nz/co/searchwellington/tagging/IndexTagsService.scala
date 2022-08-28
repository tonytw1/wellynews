package nz.co.searchwellington.tagging

import nz.co.searchwellington.model.geo.Geocode
import nz.co.searchwellington.model.taggingvotes.TaggingVote
import nz.co.searchwellington.model.{Resource, Tag}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component
class IndexTagsService @Autowired()(val taggingReturnsOfficerService: TaggingReturnsOfficerService) {

  def getIndexTagsForResource(resource: Resource): Future[Seq[Tag]] = {
    taggingReturnsOfficerService.getTaggingsVotesForResource(resource).map(indexTagsForTaggingVotes)
  }


  def indexTagsForTaggingVotes(taggingVotes: Seq[TaggingVote]): Seq[Tag] = {
    taggingVotes.map(_.tag).distinct
  }

  def getIndexGeocodeForResource(resource: Resource): Future[Option[Geocode]] = {
    taggingReturnsOfficerService.getGeotagVotesForResource(resource).map { geotaggingVotes =>
      geotaggingVotes.headOption.map(_.geocode)
    }
  }

}
