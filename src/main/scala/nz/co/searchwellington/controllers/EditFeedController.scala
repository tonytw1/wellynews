package nz.co.searchwellington.controllers

import io.opentelemetry.api.trace.Span
import jakarta.validation.Valid
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.submission.EndUserInputs
import nz.co.searchwellington.forms.EditFeed
import nz.co.searchwellington.model.{Feed, Resource, UrlWordsGenerator, User}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.queues.ElasticIndexQueue
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.repositories.{ContentRetrievalService, HandTaggingService, TagDAO}
import nz.co.searchwellington.urls.{UrlBuilder, UrlCleaner}
import nz.co.searchwellington.views.Errors
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.{GetMapping, ModelAttribute, PathVariable, PostMapping}
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters._

@Controller
class EditFeedController @Autowired()(contentUpdateService: ContentUpdateService,
                                      mongoRepository: MongoRepository,
                                      urlWordsGenerator: UrlWordsGenerator,
                                      urlBuilder: UrlBuilder,
                                      val loggedInUserFilter: LoggedInUserFilter,
                                      val tagDAO: TagDAO,
                                      val contentRetrievalService: ContentRetrievalService,
                                      handTaggingService: HandTaggingService,
                                      elasticIndexQueue: ElasticIndexQueue,
                                      val urlCleaner: UrlCleaner) extends EditScreen with ReasonableWaits with AcceptancePolicyOptions
  with Errors with RequiringLoggedInUser with EndUserInputs with HeldSubmissions {

  private val log = LogFactory.getLog(classOf[EditFeedController])

  @GetMapping(Array("/edit-feed/{id}"))
  def prompt(@PathVariable id: String): ModelAndView = {
    def showForm(loggedInUser: User): ModelAndView = {
      implicit val currentSpan: Span = Span.current()

      getFeedById(id).map { f =>
        val publisher = f.publisher.flatMap(pid => Await.result(mongoRepository.getResourceByObjectId(pid), TenSeconds))

        val editFeed = new EditFeed()
        editFeed.setTitle(processTitle(f.title))
        editFeed.setUrl(f.page)
        editFeed.setPublisher(publisher.map(_.title).getOrElse(""))
        editFeed.setAcceptancePolicy(f.acceptance)

        val usersTags = f.resource_tags.filter(_.user_id == loggedInUser._id)
        editFeed.setTags(usersTags.map(_.tag_id.stringify).asJava)

        renderEditForm(f, editFeed, loggedInUser)

      }.getOrElse {
        NotFound
      }
    }

    requiringAdminUser(showForm)
  }

  @PostMapping(Array("/edit-feed/{id}"))
  def submit(@PathVariable id: String, @Valid @ModelAttribute("formObject") formObject: EditFeed, result: BindingResult): ModelAndView = {
    def handleSubmission(loggedInUser: User): ModelAndView = {
      implicit val currentSpan: Span = Span.current()

      getFeedById(id).map { f =>
        if (result.hasErrors) {
          log.warn("Edit feed submission has errors: " + result)
          renderEditForm(f, formObject, loggedInUser)

        } else {
          log.info("Got valid edit feed submission: " + formObject)

          val publisherName = if (formObject.getPublisher.trim.nonEmpty) {
            log.info("Publisher is: " + formObject.getPublisher.trim)
            Some(formObject.getPublisher.trim)
          } else {
            None
          }

          val publisher = publisherName.flatMap { publisherName =>
            Await.result(mongoRepository.getWebsiteByName(publisherName), TenSeconds)
          }
          log.info("Resolved publisher: " + publisher)

          val uf = f.copy(
            title = formObject.getTitle,
            page = formObject.getUrl,
            publisher = publisher.map(_._id),
            acceptance = formObject.getAcceptancePolicy,
            held = submissionShouldBeHeld(loggedInUser)
          )
          val updatedFeed = uf.copy(url_words = Some(urlWordsGenerator.makeUrlWordsFor(uf, publisher)))
          val requestedTags = Await.result(tagDAO.loadTagsById(formObject.getTags.asScala.toSeq), TenSeconds).toSet
          val withUpdatedTags = handTaggingService.setUsersTagging(loggedInUser, requestedTags.map(_._id), updatedFeed)

          contentUpdateService.update(withUpdatedTags).flatMap { result =>
            log.info("Update result was: " + result)
            if (result) {
              log.info("Updated feed: " + withUpdatedTags)
              reindexForTagChanges(f, updatedFeed, withUpdatedTags)
            } else {
              Future.successful(true)
            }
          }
          new ModelAndView(new RedirectView(urlBuilder.getFeedUrl(updatedFeed)))
        }
      }.getOrElse(NotFound)
    }

    requiringAdminUser(handleSubmission)
  }

  // Trying to get this generalised
  private def reindexForTagChanges(feed: Feed, updatedFeed: Feed, withUpdatedTags: Resource): Future[Boolean] = {
    // TODO withUpdatedTags is a smell.

    def changeEffectsChildren: Boolean = {
      val tagsHaveChanged = feed.resource_tags.map(_.tag_id).toSet != withUpdatedTags.resource_tags.map(_.tag_id).toSet
      val publisherHasChanged = feed.publisher != updatedFeed.publisher
      tagsHaveChanged || publisherHasChanged
    }

    if (changeEffectsChildren) {
      // TODO is the feed url has changed we will need to update Whakaoko
      // This would be easier of the feed knew it's whakaoko subscription id
      mongoRepository.getResourcesIdsAcceptedFrom(feed).map { taggedResourceIds =>
        taggedResourceIds.forall(elasticIndexQueue.add)
      }.map { i =>
        log.info("Reindexed feed newsitems after feed tag or publisher change: " + i)
        true
      }
    } else {
      Future.successful(false)
    }
  }

  private def getFeedById(id: String): Option[Feed] = {
    Await.result(mongoRepository.getResourceById(id), TenSeconds).flatMap { r =>
      r match {
        case f: Feed => Some(f)
        case _ => None
      }
    }
  }

  private def renderEditForm(f: Feed, formObject: EditFeed, loggedInUser: User)(implicit currentSpan: Span): ModelAndView = {
    editScreen("editFeed", "Editing a feed", Some(loggedInUser)).
      addObject("feed", f).
      addObject("formObject", formObject).
      addObject("acceptancePolicyOptions", acceptancePolicyOptions.asJava)
  }

}
