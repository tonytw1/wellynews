package nz.co.searchwellington.controllers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.forms.EditTag
import nz.co.searchwellington.geocoding.osm.GeoCodeService
import nz.co.searchwellington.model.{Geocode, Tag, UrlWordsGenerator, User}
import nz.co.searchwellington.modification.{ContentUpdateService, TagModificationService}
import nz.co.searchwellington.repositories.TagDAO
import nz.co.searchwellington.repositories.elasticsearch.ElasticSearchIndexRebuildService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.UrlBuilder
import nz.co.searchwellington.views.Errors
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.validation.{BindingResult, ObjectError}
import org.springframework.web.bind.annotation.{ModelAttribute, PathVariable, RequestMapping, RequestMethod}
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView
import reactivemongo.api.bson.BSONObjectID

import javax.validation.Valid
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Controller
class EditTagController @Autowired()(contentUpdateService: ContentUpdateService,
                                     mongoRepository: MongoRepository, tagDAO: TagDAO,
                                     urlWordsGenerator: UrlWordsGenerator,
                                     urlBuilder: UrlBuilder,
                                     val loggedInUserFilter: LoggedInUserFilter,
                                     tagModificationService: TagModificationService,
                                     elasticSearchIndexRebuildService: ElasticSearchIndexRebuildService,
                                     val geocodeService: GeoCodeService)
  extends ReasonableWaits with Errors with InputParsing with GeotagParsing with RequiringLoggedInUser {

  private val log = Logger.getLogger(classOf[EditTagController])

  @RequestMapping(value = Array("/edit-tag/{id}"), method = Array(RequestMethod.GET))
  def prompt(@PathVariable id: String): ModelAndView = {

    def promptForEditTag(loggedInUser: User): ModelAndView = {
      Await.result(mongoRepository.getTagById(id), TenSeconds).map { tag =>
        val editTag = new EditTag(tag.display_name,
          tag.description.getOrElse(""),
          tag.parent.map(_.stringify).orNull,
          tag.getAutotagHints.orNull,
          tag.isFeatured,
        )
        tag.geocode.foreach { g =>
          editTag.setGeocode(g.getAddress)
          val osmId = g.osmId.map(osmToString)
          editTag.setSelectedGeocode(osmId.getOrElse(""))
        }

        renderEditForm(tag, editTag)

      }.getOrElse {
        NotFound
      }
    }

    requiringAdminUser(promptForEditTag)
  }

  @RequestMapping(value = Array("/edit-tag/{id}"), method = Array(RequestMethod.POST))
  def submit(@PathVariable id: String, @Valid @ModelAttribute("editTag") editTag: EditTag, result: BindingResult): ModelAndView = {

    def submitEditTag(loggedInUser: User): ModelAndView = {
      Await.result(mongoRepository.getTagById(id), TenSeconds).map { tag =>
        val resolvedGeocode = for {
          address <- Option(editTag.getGeocode)
          osmId <- Option(editTag.getSelectedGeocode)
          geocode <- parseGeotag(address, osmId)
        } yield {
          geocode
        }

        log.info("Resolved geocode: " + resolvedGeocode)
        if (Option(editTag.getSelectedGeocode).nonEmpty && resolvedGeocode.isEmpty) {
          result.addError(new ObjectError("geocode", "Could not resolve geocode"))
        }

        if (result.hasErrors) {
          log.warn("Edit tag submission has errors: " + result)
          renderEditForm(tag, editTag)

        } else {

          def optionalBsonObjectId(i: String): Option[BSONObjectID] = {
            optionalInputString(i).flatMap { bid =>
              val maybeParsed = BSONObjectID.parse(bid)
              if (maybeParsed.isSuccess) {
                Some(maybeParsed.get)
              } else {
                None // TODO push error up
              }
            }
          }

          val parentTag = optionalBsonObjectId(editTag.getParent).flatMap { p =>
            val maybeTag = Await.result(tagDAO.loadTagByObjectId(p), TenSeconds)
            log.info("Found parent for tag id " + p.stringify + ": " + maybeTag)
            maybeTag
          }

          val updatedTag = tag.copy(
            display_name = editTag.getDisplayName,
            description = Option(editTag.getDescription),
            parent = parentTag.map(_._id),
            autotag_hints = Some(editTag.getAutotagHints),
            featured = editTag.getFeatured,
            geocode = resolvedGeocode,
          )

          Await.result(mongoRepository.saveTag(updatedTag), TenSeconds)
          log.info("Updated tag: " + updatedTag)

          val parentHasChanged = tag.parent != updatedTag.parent
          if (parentHasChanged) {
            mongoRepository.getResourceIdsByTag(tag).flatMap { taggedResourceIds =>
              elasticSearchIndexRebuildService.reindexResources(taggedResourceIds)
            }.map { i =>
              log.info("Reindexed resource after tag parent change: " + i)
            }
          }

          new ModelAndView(new RedirectView(urlBuilder.getTagUrl(tag)))
        }
      }.getOrElse {
        NotFound
      }
    }

    requiringAdminUser(submitEditTag)
  }

  private def renderEditForm(tag: Tag, editTag: EditTag): ModelAndView = {
    import scala.collection.JavaConverters._
    val possibleParents = Await.result(tagDAO.getAllTags, TenSeconds).filterNot(_ == tag)
    new ModelAndView("editTag").
      addObject("tag", tag).
      addObject("parents", possibleParents.asJava).
      addObject("editTag", editTag)
  }

}