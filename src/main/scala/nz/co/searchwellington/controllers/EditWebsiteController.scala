package nz.co.searchwellington.controllers

import jakarta.validation.Valid
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.submission.{EndUserInputs, GeotagParsing}
import nz.co.searchwellington.forms.EditWebsite
import nz.co.searchwellington.geocoding.osm.GeoCodeService
import nz.co.searchwellington.model._
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.elasticsearch.ElasticSearchIndexRebuildService
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

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.jdk.CollectionConverters._

@Controller
class EditWebsiteController @Autowired()(contentUpdateService: ContentUpdateService,
                                         mongoRepository: MongoRepository,
                                         urlBuilder: UrlBuilder,
                                         val loggedInUserFilter: LoggedInUserFilter,
                                         tagDAO: TagDAO,
                                         val geocodeService: GeoCodeService,
                                         handTaggingService: HandTaggingService,
                                         elasticSearchIndexRebuildService: ElasticSearchIndexRebuildService,
                                         val urlCleaner: UrlCleaner
                                        ) extends ReasonableWaits with AcceptancePolicyOptions with Errors with GeotagParsing
  with RequiringLoggedInUser with EndUserInputs with HeldSubmissions {

  private val log = LogFactory.getLog(classOf[EditWebsiteController])

  @GetMapping(Array("/edit-website/{id}"))
  def prompt(@PathVariable id: String): ModelAndView = {
    def showForm(loggedInUser: User): ModelAndView = {
      getWebsiteById(id).map { w =>
        val editWebsite = new EditWebsite()
        editWebsite.setTitle(w.title)
        editWebsite.setUrl(w.page)
        editWebsite.setDescription(w.description.getOrElse(""))

        w.geocode.foreach { g =>
          editWebsite.setGeocode(g.getAddress)
          val osmId = g.osmId.map(osmToString)
          editWebsite.setOsm(osmId.getOrElse(""))
        }

        val usersTags = w.resource_tags.filter(_.user_id == loggedInUser._id)

        editWebsite.setTags(usersTags.map(_.tag_id.stringify).asJava)

        renderEditForm(w, editWebsite)

      }.getOrElse {
        NotFound
      }
    }

    requiringAdminUser(showForm)
  }

  @PostMapping(Array("/edit-website/{id}"))
  def submit(@PathVariable id: String, @Valid @ModelAttribute("formObject") editWebsite: EditWebsite, result: BindingResult): ModelAndView = {
    def handleSubmission(loggedInUser: User): ModelAndView = {
      getWebsiteById(id).map { w =>
        if (result.hasErrors) {
          log.warn("Edit website submission has errors: " + result)
          renderEditForm(w, editWebsite)

        } else {
          log.info("Got valid edit website submission: " + editWebsite)

          val geocode = Option(editWebsite.getGeocode).flatMap { address =>
            Option(editWebsite.getOsm).flatMap { osmId =>
              parseGeotag(address, osmId)
            }
          }

          val updated = w.copy(
            title = processTitle(editWebsite.getTitle),
            page = editWebsite.getUrl,
            description = Some(editWebsite.getDescription),
            geocode = geocode,
            held = submissionShouldBeHeld(loggedInUser)
          )

          val requestedTags = Await.result(tagDAO.loadTagsById(editWebsite.getTags.asScala.toSeq), TenSeconds).toSet
          val withUpdatedTags = handTaggingService.setUsersTagging(loggedInUser, requestedTags.map(_._id), updated)

          contentUpdateService.update(withUpdatedTags).map { result =>
            if (result) {
              log.info("Updated website: " + withUpdatedTags)
              val tagsHaveChanged = w.resource_tags.map(_.tag_id).toSet != withUpdatedTags.resource_tags.map(_.tag_id).toSet
              if (tagsHaveChanged) {
                mongoRepository.getResourcesIdsForPublisher(w).flatMap { taggedResourceIds =>
                  elasticSearchIndexRebuildService.reindexResources(taggedResourceIds, totalResources = taggedResourceIds.size)
                }.map { i =>
                  log.info("Reindexed publisher resources after publisher tag change: " + i)
                }
              }
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

  private def renderEditForm(w: Website, editWebsite: EditWebsite): ModelAndView = {
    new ModelAndView("editWebsite").
      addObject("title", "Editing a website").
      addObject("website", w).
      addObject("formObject", editWebsite).
      addObject("tags", Await.result(tagDAO.getAllTags, TenSeconds).asJava)
  }

}