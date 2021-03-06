package nz.co.searchwellington.controllers

import javax.validation.Valid
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.forms.EditWebsite
import nz.co.searchwellington.geocoding.osm.CachingNominatimResolveOsmIdService
import nz.co.searchwellington.model._
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.elasticsearch.ElasticSearchIndexRebuildService
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
class EditWebsiteController @Autowired()(contentUpdateService: ContentUpdateService,
                                         mongoRepository: MongoRepository,
                                         urlBuilder: UrlBuilder,
                                         val loggedInUserFilter: LoggedInUserFilter,
                                         tagDAO: TagDAO,
                                         val cachingNominatimResolveOsmIdService: CachingNominatimResolveOsmIdService,
                                         handTaggingService: HandTaggingService,
                                         elasticSearchIndexRebuildService: ElasticSearchIndexRebuildService
                                        ) extends ReasonableWaits with AcceptancePolicyOptions with Errors with GeotagParsing with RequiringLoggedInUser {

  private val log = Logger.getLogger(classOf[EditWebsiteController])

  @RequestMapping(value = Array("/edit-website/{id}"), method = Array(RequestMethod.GET))
  def prompt(@PathVariable id: String): ModelAndView = {
    def showForm(loggedInUser: User): ModelAndView = {
      getWebsiteById(id).map { w =>
        val editWebsite = new EditWebsite()
        editWebsite.setTitle(w.title.getOrElse(""))
        editWebsite.setUrl(w.page)
        editWebsite.setDescription(w.description.getOrElse(""))

        w.geocode.map { g =>
          editWebsite.setGeocode(g.getAddress)
          val osmId = g.osmId.map(osmToString)
          editWebsite.setSelectedGeocode(osmId.getOrElse(""))
        }

        val usersTags = w.resource_tags.filter(_.user_id == loggedInUser._id)

        import scala.collection.JavaConverters._
        editWebsite.setTags(usersTags.map(_.tag_id.stringify).asJava)

        renderEditForm(w, editWebsite)

      }.getOrElse {
        NotFound
      }
    }

    requiringAdminUser(showForm)
  }

  @RequestMapping(value = Array("/edit-website/{id}"), method = Array(RequestMethod.POST))
  def submit(@PathVariable id: String, @Valid @ModelAttribute("editWebsite") editWebsite: EditWebsite, result: BindingResult): ModelAndView = {
    def handleSubmission(loggedInUser: User): ModelAndView = {
      getWebsiteById(id).map { w =>
        if (result.hasErrors) {
          log.warn("Edit website submission has errors: " + result)
          renderEditForm(w, editWebsite)

        } else {
          log.info("Got valid edit website submission: " + editWebsite)

          val geocode = Option(editWebsite.getGeocode).flatMap { address =>
            Option(editWebsite.getSelectedGeocode).flatMap { osmId =>
              parseGeotag(address, osmId)
            }
          }

          val updated = w.copy(
            title = Some(editWebsite.getTitle),
            page = editWebsite.getUrl,
            description = Some(editWebsite.getDescription),
            geocode = geocode,
            held = submissionShouldBeHeld(loggedInUser)
          )

          import scala.collection.JavaConverters._
          val tags = Await.result(tagDAO.loadTagsById(editWebsite.getTags.asScala), TenSeconds).toSet
          val withNewTags = handTaggingService.setUsersTagging(loggedInUser, tags.map(_._id), updated)

          contentUpdateService.update(withNewTags)
          log.info("Updated website: " + withNewTags)

          val tagsHaveChanged = tags.map(_._id) == withNewTags.resource_tags.map(_.tag_id).toSet
          if (tagsHaveChanged) {
            mongoRepository.getResourcesIdsForPublisher(w).flatMap { taggedResourceIds =>
              elasticSearchIndexRebuildService.reindexResources(taggedResourceIds)
            }.map { i =>
              log.info("Reindexed publisher resources after publisher tag change: " + i)
            }
          }

          new ModelAndView(new RedirectView(urlBuilder.getPublisherUrl(updated)))
        }

      }.getOrElse(NotFound)
    }

    requiringAdminUser(handleSubmission)
  }

  private def getWebsiteById(id: String): Option[Website] = {
    Await.result(mongoRepository.getResourceById(id), TenSeconds).flatMap { r =>
      r match {
        case w: Website => Some(w)
        case _ => None
      }
    }
  }

  private def submissionShouldBeHeld(owner: User): Boolean = {
    !owner.isAdmin
  }

  private def renderEditForm(w: Website, editWebsite: EditWebsite): ModelAndView = {
    import scala.collection.JavaConverters._
    new ModelAndView("editWebsite").
      addObject("title", "Editing a website").
      addObject("website", w).
      addObject("editWebsite", editWebsite).
      addObject("tags", Await.result(tagDAO.getAllTags, TenSeconds).asJava)
  }

}