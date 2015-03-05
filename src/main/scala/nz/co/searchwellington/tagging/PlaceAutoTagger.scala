package nz.co.searchwellington.tagging

import java.util
import java.util.Set

import com.google.common.collect.Sets
import nz.co.searchwellington.model.{Resource, Tag}
import nz.co.searchwellington.repositories.TagDAO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._

@Component class PlaceAutoTagger @Autowired() (tagDAO: TagDAO) {

  private final val PLACES_TAG_NAME: String = "places"

  def suggestTags(resource: Resource): Set[Tag] = {
    val places: util.Set[Tag] = getPlaces
    val toSet = places.filter(place => checkForMatchingTag(resource, place))
    return toSet
  }

  def getPlaces: util.Set[Tag] = {
    val placesTag: Tag = tagDAO.loadTagByName(PLACES_TAG_NAME)
    if(placesTag != null) placesTag.getChildren else (Sets.newHashSet())
  }

  private def checkForMatchingTag(resource: Resource, tag: Tag): Boolean = {
    val headlineMatchesTag: Boolean = resource.getName.toLowerCase.contains(tag.getDisplayName.toLowerCase)
    val bodyMatchesTag: Boolean = resource.getDescription.toLowerCase.contains(tag.getDisplayName.toLowerCase)
    return headlineMatchesTag || bodyMatchesTag
  }

}
