package nz.co.searchwellington.controllers

import javax.validation.Valid
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.forms.EditFeed
import nz.co.searchwellington.model.{Feed, UrlWordsGenerator, User}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.{HandTaggingService, TagDAO}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.UrlBuilder
import nz.co.searchwellington.views.Errors
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.{ModelAttribute, PathVariable, RequestMapping, RequestMethod}
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Controller
class EditFeedController @Autowired()(contentUpdateService: ContentUpdateService,
                                      mongoRepository: MongoRepository,
                                      urlWordsGenerator: UrlWordsGenerator,
                                      urlBuilder: UrlBuilder,
                                      val loggedInUserFilter: LoggedInUserFilter,
                                      tagDAO: TagDAO,
                                      handTaggingService: HandTaggingService) extends ReasonableWaits with AcceptancePolicyOptions with Errors with RequiringLoggedInUser {

  private val log = Logger.getLogger(classOf[EditFeedController])

  @RequestMapping(value = Array("/edit-feed/{id}"), method = Array(RequestMethod.GET))
  def prompt(@PathVariable id: String): ModelAndView = {
    def showForm(loggedInUser: User): ModelAndView = {
      getFeedById(id).map { f =>
        val publisher = f.publisher.flatMap(pid => Await.result(mongoRepository.getResourceByObjectId(pid), TenSeconds))

        val editFeed = new EditFeed()
        editFeed.setTitle(f.title.getOrElse(""))
        editFeed.setUrl(f.page)
        editFeed.setPublisher(publisher.flatMap(_.title).getOrElse(""))
        editFeed.setAcceptancePolicy(f.acceptance)

        val usersTags = f.resource_tags.filter(_.user_id == loggedInUser._id)
        import scala.collection.JavaConverters._
        editFeed.setTags(usersTags.map(_.tag_id.stringify).asJava)

        renderEditForm(f, editFeed)

      }.getOrElse {
        NotFound
      }
    }

    requiringAdminUser(showForm)
  }

  @RequestMapping(value = Array("/edit-feed/{id}"), method = Array(RequestMethod.POST))
  def submit(@PathVariable id: String, @Valid @ModelAttribute("editFeed") editFeed: EditFeed, result: BindingResult): ModelAndView = {
    def handleSubmission(loggedInUser: User): ModelAndView = {
      getFeedById(id).map { f =>
        if (result.hasErrors) {
          log.warn("Edit feed submission has errors: " + result)
          renderEditForm(f, editFeed)

        } else {
          log.info("Got valid edit feed submission: " + editFeed)

          val publisherName = if (editFeed.getPublisher.trim.nonEmpty) {
            log.info("Publisher is: " + editFeed.getPublisher.trim)
            Some(editFeed.getPublisher.trim)
          } else {
            None
          }

          val publisher = publisherName.flatMap { publisherName =>
            Await.result(mongoRepository.getWebsiteByName(publisherName), TenSeconds)
          }
          log.info("Resolved publisher: " + publisher)

          val updatedFeed = f.copy(
            title = Some(editFeed.getTitle),
            page = editFeed.getUrl,
            url_words = Some(urlWordsGenerator.makeUrlWordsFromName(editFeed.getTitle)),
            publisher = publisher.map(_._id),
            acceptance = editFeed.getAcceptancePolicy,
            held = submissionShouldBeHeld(loggedInUser)
          )

          import scala.collection.JavaConverters._
          val submittedTags = Await.result(tagDAO.loadTagsById(editFeed.getTags.asScala), TenSeconds).toSet

          val updated = handTaggingService.setUsersTagging(loggedInUser, submittedTags.map(_._id), updatedFeed)

          contentUpdateService.update(updated)
          log.info("Updated feed: " + updated)

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

  // TODO duplication
  private def submissionShouldBeHeld(loggerInUser: User): Boolean = {
    !loggerInUser.isAdmin
  }

  private def renderEditForm(f: Feed, editFeed: EditFeed): ModelAndView = {
    import scala.collection.JavaConverters._
    new ModelAndView("editFeed").
      addObject("feed", f).
      addObject("editFeed", editFeed).
      addObject("acceptancePolicyOptions", acceptancePolicyOptions.asJava)
  }

}
