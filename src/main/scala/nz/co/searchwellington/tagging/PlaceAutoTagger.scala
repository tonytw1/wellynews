package nz.co.searchwellington.tagging

import nz.co.searchwellington.model.{Resource, Tag}
import nz.co.searchwellington.repositories.TagDAO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.JavaConverters._

@Component class PlaceAutoTagger @Autowired() (tagDAO: TagDAO) {

  private final val PLACES_TAG_NAME: String = "places"

  def suggestTags(resource: Resource): Set[Tag] = {
    getPlaces.filter(place => checkForMatchingTag(resource, place))
  }

  def getPlaces: Set[Tag] = {
    val placesTag: Tag = tagDAO.loadTagByName(PLACES_TAG_NAME)
    if (placesTag == null) {
      return Set.empty
    }
    placesTag.getChildren.asScala.toSet
  }

  private def checkForMatchingTag(resource: Resource, tag: Tag): Boolean = {
    val headlineMatchesTag: Boolean = resource.getName.toLowerCase.contains(tag.getDisplayName.toLowerCase)
    val bodyMatchesTag: Boolean = resource.getDescription.toLowerCase.contains(tag.getDisplayName.toLowerCase)
    headlineMatchesTag || bodyMatchesTag
  }

}
