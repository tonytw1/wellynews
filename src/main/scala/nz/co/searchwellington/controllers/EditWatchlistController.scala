package nz.co.searchwellington.controllers

import jakarta.validation.Valid
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.submission.{EndUserInputs, GeotagParsing}
import nz.co.searchwellington.forms.EditWatchlist
import nz.co.searchwellington.geocoding.osm.GeoCodeService
import nz.co.searchwellington.model._
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

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters._

@Controller
class EditWatchlistController @Autowired()(contentUpdateService: ContentUpdateService,
                                           mongoRepository: MongoRepository,
                                           urlBuilder: UrlBuilder,
                                           val loggedInUserFilter: LoggedInUserFilter,
                                           val tagDAO: TagDAO,
                                           val geocodeService: GeoCodeService,
                                           handTaggingService: HandTaggingService,
                                           val urlCleaner: UrlCleaner
                                        ) extends EditScreen with ReasonableWaits with AcceptancePolicyOptions with Errors with GeotagParsing
  with RequiringLoggedInUser with EndUserInputs with HeldSubmissions {

  private val log = LogFactory.getLog(classOf[EditWatchlistController])

  @GetMapping(Array("/edit-watchlist/{id}"))
  def prompt(@PathVariable id: String): ModelAndView = {
    def showForm(loggedInUser: User): ModelAndView = {
      getWatchlistById(id).map { w =>
        val editWatchlist = mapToForm(w)
        val usersTags = w.resource_tags.filter(_.user_id == loggedInUser._id)

        editWatchlist.setTags(usersTags.map(_.tag_id.stringify).asJava)

        renderEditForm(w, editWatchlist, loggedInUser)

      }.getOrElse {
        NotFound
      }
    }

    requiringAdminUser(showForm)
  }

  private def mapToForm(w: Watchlist): EditWatchlist = {
    val eventualPublisher = w.publisher.map { pid =>
      mongoRepository.getResourceByObjectId(pid).map { ro =>
        ro.flatMap { r =>
          r match {
            case w: Website => Some(w)
            case _ => None
          }
        }
      }

    }.getOrElse(Future.successful(None))
    val publisherName= Await.result(eventualPublisher, TenSeconds).map(p => p.title).getOrElse("")

    val editWatchlist = new EditWatchlist()
    editWatchlist.setTitle(processTitle(w.title))
    editWatchlist.setUrl(w.page)
    editWatchlist.setPublisher(publisherName)
    editWatchlist.setDescription(w.description.getOrElse(""))
    editWatchlist
  }

  @PostMapping(Array("/edit-watchlist/{id}"))
  def submit(@PathVariable id: String, @Valid @ModelAttribute("editWatchlist") editWatchlist: EditWatchlist, result: BindingResult): ModelAndView = {
    def handleSubmission(loggedInUser: User): ModelAndView = {
      getWatchlistById(id).map { w =>
        if (result.hasErrors) {
          log.warn("Edit watchlist submission has errors: " + result)
          renderEditForm(w, editWatchlist, loggedInUser)

        } else {
          log.info("Got valid edit watchlist submission: " + editWatchlist)

          val publisherName = if (editWatchlist.getPublisher.trim.nonEmpty) {
            Some(editWatchlist.getPublisher.trim)
          } else {
            None
          }
          val publisher = publisherName.flatMap { publisherName =>
            Await.result(mongoRepository.getWebsiteByName(publisherName), TenSeconds)
          }

          val updated = w.copy(
            title = editWatchlist.getTitle,
            page = editWatchlist.getUrl,
            publisher = publisher.map(_._id),
            description = Some(editWatchlist.getDescription),
            held = submissionShouldBeHeld(loggedInUser)
          )

          val tags = Await.result(tagDAO.loadTagsById(editWatchlist.getTags.asScala.toSeq), TenSeconds).toSet
          val withNewTags = handTaggingService.setUsersTagging(loggedInUser, tags.map(_._id), updated)

          contentUpdateService.update(withNewTags)
          log.info("Updated watchlist: " + withNewTags)

          new ModelAndView(new RedirectView(urlBuilder.getWatchlistUrl))
        }

      }.getOrElse(NotFound)
    }

    requiringAdminUser(handleSubmission)
  }

  private def getWatchlistById(id: String): Option[Watchlist] = {
    Await.result(mongoRepository.getResourceById(id), TenSeconds).flatMap { r =>
      r match {
        case w: Watchlist => Some(w)
        case _ => None
      }
    }
  }

  private def renderEditForm(w: Watchlist, editWatchlist: EditWatchlist, loggedInUser: User): ModelAndView = {
    editScreen("editWatchlist", "Editing a watchlist", Some(loggedInUser)).
      addObject("watchlist", w).
      addObject("formObject", editWatchlist)
  }

}