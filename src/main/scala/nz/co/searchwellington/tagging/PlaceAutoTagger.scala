package nz.co.searchwellington.tagging

import nz.co.searchwellington.model.{Resource, Tag}
import nz.co.searchwellington.repositories.TagDAO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.JavaConverters._

@Component class PlaceAutoTagger @Autowired() (tagDAO: TagDAO) {

  private final val PLACES_TAG_NAME = "places"

  def suggestTags(resource: Resource): Set[Tag] = {
    getPlaces.filter(p => checkForMatchingTag(resource, p))
  }

  def getPlaces: Set[Tag] = {
    tagDAO.loadTagByName(PLACES_TAG_NAME).map { placesTag =>
      tagDAO.loadTagsByParent(placesTag.id)
    }.getOrElse {
      Seq()
    }.toSet
  }

  private def checkForMatchingTag(resource: Resource, tag: Tag): Boolean = {
    val headlineMatchesTag: Boolean = resource.getName.toLowerCase.contains(tag.getDisplayName.toLowerCase)
    val bodyMatchesTag: Boolean = resource.getDescription.toLowerCase.contains(tag.getDisplayName.toLowerCase)
    headlineMatchesTag || bodyMatchesTag
  }

}
