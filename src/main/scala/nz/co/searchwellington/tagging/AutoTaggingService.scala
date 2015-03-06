package nz.co.searchwellington.tagging

import java.util.Set

import nz.co.searchwellington.model.{Resource, Tag, User}
import nz.co.searchwellington.repositories.{HandTaggingDAO, HibernateBackedUserDAO}
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class AutoTaggingService @Autowired() (placeAutoTagger: PlaceAutoTagger, tagHintAutoTagger: TagHintAutoTagger, handTaggingDAO: HandTaggingDAO, userDAO: HibernateBackedUserDAO) {

  private var log: Logger = Logger.getLogger(classOf[AutoTaggingService])
  private val AUTOTAGGER_PROFILE_NAME: String = "autotagger"

  def autotag(resource: Resource) {
    val autotaggerUser: User = userDAO.getUserByProfileName(AUTOTAGGER_PROFILE_NAME)
    if (autotaggerUser == null) {
      log.warn("Could not find auto tagger user: " + AUTOTAGGER_PROFILE_NAME)
      return
    }

    val suggestedTags: Set[Tag] = placeAutoTagger.suggestTags(resource)
    suggestedTags.addAll(tagHintAutoTagger.suggestTags(resource))
    log.debug("Suggested tags for '" + resource.getName + "' are: " + suggestedTags.toString)
    if (!suggestedTags.isEmpty) {
      handTaggingDAO.setUsersTagVotesForResource(resource, autotaggerUser, suggestedTags)
    }
  }

}