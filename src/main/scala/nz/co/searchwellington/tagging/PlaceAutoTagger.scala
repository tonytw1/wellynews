package nz.co.searchwellington.tagging

import nz.co.searchwellington.model.{Resource, Tag}
import nz.co.searchwellington.repositories.TagDAO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class PlaceAutoTagger @Autowired() (tagDAO: TagDAO) {

  private final val PLACES_TAG_NAME = "places"

  def suggestTags(resource: Resource): Set[Tag] = {

    def getPlaces: Set[Tag] = {
      tagDAO.loadTagByName(PLACES_TAG_NAME).map { placesTag =>
        tagDAO.loadTagsByParent(placesTag.id)
      }.getOrElse {
        Seq()
      }.toSet
    }

    def checkForMatchingTag(resource: Resource, tag: Tag): Boolean = {
      val headlineMatchesTag = resource.title.map(t => t.toLowerCase.contains(tag.getDisplayName.toLowerCase)).getOrElse(false)
      val bodyMatchesTag = resource.description.map(d => d.toLowerCase.contains(tag.getDisplayName.toLowerCase)).getOrElse(false)
      headlineMatchesTag || bodyMatchesTag
    }

    getPlaces.filter(p => checkForMatchingTag(resource, p))
  }

}
