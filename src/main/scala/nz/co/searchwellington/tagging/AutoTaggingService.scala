package nz.co.searchwellington.tagging

import nz.co.searchwellington.model.Resource
import nz.co.searchwellington.repositories.{HandTaggingDAO, HibernateBackedUserDAO}
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class AutoTaggingService @Autowired() (placeAutoTagger: PlaceAutoTagger, tagHintAutoTagger: TagHintAutoTagger, handTaggingDAO: HandTaggingDAO, userDAO: HibernateBackedUserDAO) {

  private var log = Logger.getLogger(classOf[AutoTaggingService])
  private val AUTOTAGGER_PROFILE_NAME = "autotagger"

  def autotag(resource: Resource) {

    userDAO.getUserByProfileName(AUTOTAGGER_PROFILE_NAME).fold {
      log.warn("Could not find auto tagger user: " + AUTOTAGGER_PROFILE_NAME)

    } { autotagUser =>
      val suggestedTags = placeAutoTagger.suggestTags(resource) ++ tagHintAutoTagger.suggestTags(resource);
      log.debug("Suggested tags for '" + resource.title + "' are: " + suggestedTags)
      if (!suggestedTags.isEmpty) {
        handTaggingDAO.setUsersTagVotesForResource(resource, autotagUser, suggestedTags)
      }
    }
  }

}
