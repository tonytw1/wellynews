package nz.co.searchwellington.controllers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.submission.EndUserInputs
import nz.co.searchwellington.forms.EditFeed
import nz.co.searchwellington.model.{Feed, UrlWordsGenerator, User}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.repositories.{HandTaggingService, TagDAO}
import nz.co.searchwellington.urls.{UrlBuilder, UrlCleaner}
import nz.co.searchwellington.views.Errors
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.{GetMapping, ModelAttribute, PathVariable, PostMapping}
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

import javax.validation.Valid
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.jdk.CollectionConverters._

@Controller
class EditFeedController @Autowired()(contentUpdateService: ContentUpdateService,
                                      mongoRepository: MongoRepository,
                                      urlWordsGenerator: UrlWordsGenerator,
                                      urlBuilder: UrlBuilder,
                                      val loggedInUserFilter: LoggedInUserFilter,
                                      tagDAO: TagDAO,
                                      handTaggingService: HandTaggingService,
                                      val urlCleaner: UrlCleaner) extends ReasonableWaits with AcceptancePolicyOptions
  with Errors with RequiringLoggedInUser with EndUserInputs with HeldSubmissions {

  private val log = LogFactory.getLog(classOf[EditFeedController])

  @GetMapping(Array("/edit-feed/{id}"))
  def prompt(@PathVariable id: String): ModelAndView = {
    def showForm(loggedInUser: User): ModelAndView = {
      getFeedById(id).map { f =>
        val publisher = f.publisher.flatMap(pid => Await.result(mongoRepository.getResourceByObjectId(pid), TenSeconds))

        val editFeed = new EditFeed()
        editFeed.setTitle(processTitle(f.title))
        editFeed.setUrl(f.page)
        editFeed.setPublisher(publisher.map(_.title).getOrElse(""))
        editFeed.setAcceptancePolicy(f.acceptance)

        val usersTags = f.resource_tags.filter(_.user_id == loggedInUser._id)
        editFeed.setTags(usersTags.map(_.tag_id.stringify).asJava)

        renderEditForm(f, editFeed)

      }.getOrElse {
        NotFound
      }
    }

    requiringAdminUser(showForm)
  }

  @PostMapping(Array("/edit-feed/{id}"))
  def submit(@PathVariable id: String, @Valid @ModelAttribute("formObject") formObject: EditFeed, result: BindingResult): ModelAndView = {
    def handleSubmission(loggedInUser: User): ModelAndView = {
      getFeedById(id).map { f =>
        if (result.hasErrors) {
          log.warn("Edit feed submission has errors: " + result)
          renderEditForm(f, formObject)

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
          val submittedTags = Await.result(tagDAO.loadTagsById(formObject.getTags.asScala.toSeq), TenSeconds).toSet

          val updated = handTaggingService.setUsersTagging(loggedInUser, submittedTags.map(_._id), updatedFeed)

          contentUpdateService.update(updated)
          log.info("Updated feed: " + updated)

          // TODO is the feed url has changed we will need to update Whakaoko
          // This would be easier of the feed knew it's whakaoko subscription id

          new ModelAndView(new RedirectView(urlBuilder.getFeedUrl(updatedFeed)))
        }
      }.getOrElse(NotFound)
    }

    requiringAdminUser(handleSubmission)
  }

  private def getFeedById(id: String): Option[Feed] = {
    Await.result(mongoRepository.getResourceById(id), TenSeconds).flatMap { r =>
      r match {
        case f: Feed => Some(f)
        case _ => None
      }
    }
  }

  private def renderEditForm(f: Feed, formObject: EditFeed): ModelAndView = {
    new ModelAndView("editFeed").
      addObject("feed", f).
      addObject("formObject", formObject).
      addObject("acceptancePolicyOptions", acceptancePolicyOptions.asJava).
      addObject("tags", Await.result(tagDAO.getAllTags, TenSeconds).asJava)
  }

}
