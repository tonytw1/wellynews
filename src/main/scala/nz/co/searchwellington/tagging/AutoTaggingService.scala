package nz.co.searchwellington.tagging

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.whakaoko.model.Category
import nz.co.searchwellington.model.taggingvotes.HandTagging
import nz.co.searchwellington.model.{Newsitem, Tag}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component class AutoTaggingService @Autowired()(placeAutoTagger: PlaceAutoTagger,
                                                 tagHintAutoTagger: TagHintAutoTagger,
                                                 mongoRepository: MongoRepository)
  extends ReasonableWaits {

  private val log = LogFactory.getLog(classOf[AutoTaggingService])

  private val AUTOTAGGER_PROFILE_NAME = "autotagger"

  def autoTagsForFeedCategories(feedItemCategories: Seq[Category])(implicit ec: ExecutionContext): Future[Set[HandTagging]] = {
    tagHintAutoTagger.suggestFeedCategoryTags(feedItemCategories).flatMap( suggestedTags =>
      toHandTagging(suggestedTags, Some("RSS category")))
  }

  def autotag(resource: Newsitem)(implicit ec: ExecutionContext): Future[Set[HandTagging]] = {
    val eventualSuggestedPlaces = placeAutoTagger.suggestTags(resource)
    val eventualAutoTags = tagHintAutoTagger.suggestTags(resource)
    for {
      suggestedPlaces <- eventualSuggestedPlaces
      suggestedAutoTags <- eventualAutoTags
      suggestedTags = {
        val suggestedTags = suggestedPlaces ++ suggestedAutoTags
        log.info("Suggested tags for '" + resource.title + "' are: " + suggestedTags.map(_.id).mkString(","))
        suggestedTags
      }
      handTaggings <- toHandTagging(suggestedTags)
    } yield {
      handTaggings
    }
  }

  private def toHandTagging(suggestedTags: Set[Tag], reason: Option[String] = None)(implicit ec: ExecutionContext): Future[Set[HandTagging]] = {
    mongoRepository.getUserByProfilename(AUTOTAGGER_PROFILE_NAME).map { maybyAutotagUser =>
      maybyAutotagUser.map { autotagUser =>
        suggestedTags.map(t => HandTagging(tag = t, user = autotagUser, reason = reason))
      }.getOrElse {
        log.warn("Could not find auto tagger user: " + AUTOTAGGER_PROFILE_NAME + "; not autotagging.")
        Set.empty
      }
    }
  }

}
