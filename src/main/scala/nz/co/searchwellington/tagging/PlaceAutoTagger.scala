package nz.co.searchwellington.tagging

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.{Resource, Tag}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component class PlaceAutoTagger @Autowired() (val mongoRepository: MongoRepository) extends TagAncestors
  with ReasonableWaits {

  private final val PLACES_TAG_NAME = "places"

  def suggestTags(resource: Resource)(implicit ec: ExecutionContext): Future[Set[Tag]] = {

    def getAllPlaces: Future[Seq[Tag]] = {
      mongoRepository.getTagByUrlWords(PLACES_TAG_NAME).flatMap { maybePlaceTag =>
        maybePlaceTag.map(descendantsOf).getOrElse(Future.successful(Seq.empty))
      }
    }

    def checkForMatchingTag(resource: Resource, tag: Tag): Boolean = {
      val headlineMatchesTag = resource.title.toLowerCase.contains(tag.getDisplayName.toLowerCase)
      val bodyMatchesTag = resource.description.exists(d => d.toLowerCase.contains(tag.getDisplayName.toLowerCase))
      headlineMatchesTag || bodyMatchesTag
    }

    getAllPlaces.map { places =>
      places.filter(p => checkForMatchingTag(resource, p)).toSet
    }
  }

}
