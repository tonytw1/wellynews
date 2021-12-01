package nz.co.searchwellington.tagging

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.whakaoko.model.Category
import nz.co.searchwellington.model.taggingvotes.HandTagging
import nz.co.searchwellington.model.{Newsitem, Tag, User}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component class AutoTaggingService @Autowired()(placeAutoTagger: PlaceAutoTagger,
                                                 tagHintAutoTagger: TagHintAutoTagger,
                                                 mongoRepository: MongoRepository)
  extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[AutoTaggingService])
  private val AUTOTAGGER_PROFILE_NAME = "autotagger"

  def autoTagsForFeedCategories(feedItemCategories: Seq[Category])(implicit ec: ExecutionContext): Future[Set[HandTagging]] = {
    mongoRepository.getUserByProfilename(AUTOTAGGER_PROFILE_NAME).flatMap { maybyAutotagUser =>
      maybyAutotagUser.map { autotagUser =>
        tagHintAutoTagger.suggestFeedCategoryTags(feedItemCategories).map { tags =>
          toHandTagging(autotagUser = autotagUser, tags)
        }
      }.getOrElse {
        log.warn("Could not find auto tagger user: " + AUTOTAGGER_PROFILE_NAME + "; not autotagging.")
        Future.successful(Set.empty)
      }
    }
  }

  def autotag(resource: Newsitem)(implicit ec: ExecutionContext): Future[Set[HandTagging]] = {
    mongoRepository.getUserByProfilename(AUTOTAGGER_PROFILE_NAME).flatMap { maybyAutotagUser =>
      maybyAutotagUser.map { autotagUser =>
        val eventualSuggestedPlaces = placeAutoTagger.suggestTags(resource)
        val eventualAutoTags = tagHintAutoTagger.suggestTags(resource)
        for {
          suggestedPlaces <- eventualSuggestedPlaces
          suggestedAutoTags <- eventualAutoTags

        } yield {
          val suggestedTags = suggestedPlaces ++ suggestedAutoTags
          log.info("Suggested tags for '" + resource.title + "' are: " + suggestedTags)
          toHandTagging(autotagUser, suggestedTags)
        }

      }.getOrElse {
        log.warn("Could not find auto tagger user: " + AUTOTAGGER_PROFILE_NAME + "; not autotagging.")
        Future.successful(Set.empty)
      }
    }
  }

  private def toHandTagging(autotagUser: User, suggestedTags: Set[Tag]): Set[HandTagging] = {
    suggestedTags.map(t => HandTagging(tag = t, user = autotagUser))
  }

}
