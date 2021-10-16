package nz.co.searchwellington.controllers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.forms.EditWatchlist
import nz.co.searchwellington.geocoding.osm.GeoCodeService
import nz.co.searchwellington.model._
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.elasticsearch.ElasticSearchIndexRebuildService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.repositories.{HandTaggingService, TagDAO}
import nz.co.searchwellington.urls.UrlBuilder
import nz.co.searchwellington.views.Errors
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.{ModelAttribute, PathVariable, RequestMapping, RequestMethod}
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

import javax.validation.Valid
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Controller
class EditWatchlistController @Autowired()(contentUpdateService: ContentUpdateService,
                                           mongoRepository: MongoRepository,
                                           urlBuilder: UrlBuilder,
                                           val loggedInUserFilter: LoggedInUserFilter,
                                           tagDAO: TagDAO,
                                           val geocodeService: GeoCodeService,
                                           handTaggingService: HandTaggingService,
                                           elasticSearchIndexRebuildService: ElasticSearchIndexRebuildService
                                        ) extends ReasonableWaits with AcceptancePolicyOptions with Errors with GeotagParsing with RequiringLoggedInUser {

  private val log = Logger.getLogger(classOf[EditWatchlistController])

  @RequestMapping(value = Array("/edit-watchlist/{id}"), method = Array(RequestMethod.GET))
  def prompt(@PathVariable id: String): ModelAndView = {
    def showForm(loggedInUser: User): ModelAndView = {
      getWatchlistById(id).map { w =>
        val editWatchlist = new EditWatchlist()
        editWatchlist.setTitle(w.title.getOrElse(""))
        editWatchlist.setUrl(w.page)
        editWatchlist.setDescription(w.description.getOrElse(""))

        val usersTags = w.resource_tags.filter(_.user_id == loggedInUser._id)

        import scala.collection.JavaConverters._
        editWatchlist.setTags(usersTags.map(_.tag_id.stringify).asJava)

        renderEditForm(w, editWatchlist)

      }.getOrElse {
        NotFound
      }
    }

    requiringAdminUser(showForm)
  }

  @RequestMapping(value = Array("/edit-watchlist/{id}"), method = Array(RequestMethod.POST))
  def submit(@PathVariable id: String, @Valid @ModelAttribute("editWatchlist") editWatchlist: EditWatchlist, result: BindingResult): ModelAndView = {
    def handleSubmission(loggedInUser: User): ModelAndView = {
      getWatchlistById(id).map { w =>
        if (result.hasErrors) {
          log.warn("Edit watchlist submission has errors: " + result)
          renderEditForm(w, editWatchlist)

        } else {
          log.info("Got valid edit watchlist submission: " + editWatchlist)

          val updated = w.copy(
            title = Some(editWatchlist.getTitle),
            page = editWatchlist.getUrl,
            description = Some(editWatchlist.getDescription),
            held = submissionShouldBeHeld(loggedInUser)
          )

          import scala.collection.JavaConverters._
          val tags = Await.result(tagDAO.loadTagsById(editWatchlist.getTags.asScala), TenSeconds).toSet
          val withNewTags = handTaggingService.setUsersTagging(loggedInUser, tags.map(_._id), updated)

          contentUpdateService.update(withNewTags)
          log.info("Updated watchlist: " + withNewTags)

          new ModelAndView(new RedirectView("TODO"))
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

  private def submissionShouldBeHeld(owner: User): Boolean = {
    !owner.isAdmin
  }

  private def renderEditForm(w: Watchlist, editWatchlist: EditWatchlist): ModelAndView = {
    import scala.collection.JavaConverters._
    new ModelAndView("editWatchlist").
      addObject("title", "Editing a watchlist").
      addObject("watchlist", w).
      addObject("editWatchlist", editWatchlist).
      addObject("tags", Await.result(tagDAO.getAllTags, TenSeconds).asJava)
  }

}