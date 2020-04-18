package nz.co.searchwellington.controllers

import javax.validation.Valid
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.forms.EditTag
import nz.co.searchwellington.model.{Tag, UrlWordsGenerator}
import nz.co.searchwellington.modification.{ContentUpdateService, TagModificationService}
import nz.co.searchwellington.repositories.TagDAO
import nz.co.searchwellington.repositories.elasticsearch.ElasticSearchIndexRebuildService
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
import reactivemongo.bson.BSONObjectID

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

@Controller
class EditTagController @Autowired()(contentUpdateService: ContentUpdateService,
                                     mongoRepository: MongoRepository, tagDAO: TagDAO,
                                     urlWordsGenerator: UrlWordsGenerator,
                                     urlBuilder: UrlBuilder,
                                     loggedInUserFilter: LoggedInUserFilter,
                                     tagModificationService: TagModificationService,
                                     elasticSearchIndexRebuildService: ElasticSearchIndexRebuildService)
  extends ReasonableWaits with Errors with InputParsing {

  private val log = Logger.getLogger(classOf[EditTagController])

  @RequestMapping(value = Array("/edit-tag/{id}"), method = Array(RequestMethod.GET))
  def prompt(@PathVariable id: String): ModelAndView = {
    Await.result(mongoRepository.getTagById(id), TenSeconds).map { tag =>
      renderEditForm(tag, new EditTag(tag.display_name,
        tag.description.getOrElse(""),
        tag.parent.map(_.stringify).orNull,
        tag.getAutotagHints.orNull,
        tag.isFeatured,
      ))

    }.getOrElse {
      NotFound
    }
  }

  @RequestMapping(value = Array("/edit-tag/{id}"), method = Array(RequestMethod.POST))
  def submit(@PathVariable id: String, @Valid @ModelAttribute("editTag") editTag: EditTag, result: BindingResult): ModelAndView = {
    Await.result(mongoRepository.getTagById(id), TenSeconds).map { tag =>
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
          featured =  editTag.getFeatured,
        )

        Await.result(mongoRepository.saveTag(updatedTag), TenSeconds)
        log.info("Updated feed: " + updatedTag)

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

  private def renderEditForm(tag: Tag, editTag: EditTag): ModelAndView = {
    import scala.collection.JavaConverters._
    val possibleParents = Await.result(tagDAO.getAllTags, TenSeconds).filterNot(_ == tag)
    new ModelAndView("editTag").
      addObject("tag", tag).
      addObject("parents", possibleParents.asJava).
      addObject("editTag", editTag)
  }

}