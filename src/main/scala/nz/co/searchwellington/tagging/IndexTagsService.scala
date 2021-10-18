package nz.co.searchwellington.tagging

import nz.co.searchwellington.model.{Geocode, Resource, Tag}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component
class IndexTagsService @Autowired()(val taggingReturnsOfficerService: TaggingReturnsOfficerService) {

  def getIndexTagsForResource(resource: Resource): Future[Seq[Tag]] = {
    taggingReturnsOfficerService.getTaggingsVotesForResource(resource).map { taggingVotes =>
      taggingVotes.map(_.tag).distinct
    }
  }

  def getIndexGeocodeForResource(resource: Resource): Future[Option[Geocode]] = {
    taggingReturnsOfficerService.getGeotagVotesForResource(resource).map { geotaggingVotes =>
      geotaggingVotes.headOption.map(_.geocode)
    }
  }

}
