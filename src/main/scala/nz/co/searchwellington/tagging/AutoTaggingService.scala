package nz.co.searchwellington.tagging

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.Newsitem
import nz.co.searchwellington.model.taggingvotes.HandTagging
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

  def autotag(resource: Newsitem)(implicit ec: ExecutionContext): Future[Seq[HandTagging]] = { // TODO should return TaggingVotes
    val eventualTaggings = mongoRepository.getUserByProfilename(AUTOTAGGER_PROFILE_NAME).flatMap { maybyAutotagUser =>
      maybyAutotagUser.map { autotagUser =>
        val eventualSuggestedPlaces = placeAutoTagger.suggestTags(resource)
        val eventualAutoTags = tagHintAutoTagger.suggestTags(resource)
        for {
          suggestedPlaces <- eventualSuggestedPlaces
          suggestedAutoTags <- eventualAutoTags

        } yield {
          val suggestedTags = suggestedPlaces ++ suggestedAutoTags
          log.debug("Suggested tags for '" + resource.title + "' are: " + suggestedTags)
          suggestedTags.map(t => HandTagging(tag = t, user = autotagUser))
        }

      }.getOrElse {
        log.warn("Could not find auto tagger user: " + AUTOTAGGER_PROFILE_NAME + "; not autotagging.")
        Future.successful(Seq.empty)
      }
    }

    eventualTaggings.recover {
      case e: Exception =>
        log.error("Error while autotagging " + resource.title, e)
        throw (e)
    }
  }

}
