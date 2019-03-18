package nz.co.searchwellington.controllers.admin

import java.util.UUID
import java.util.regex.Pattern

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import nz.co.searchwellington.controllers.{CommonModelObjectsService, LoggedInUserFilter, SubmissionProcessingService, UrlStack}
import nz.co.searchwellington.filters.AdminRequestFilter
import nz.co.searchwellington.model.{Feed, Tag, UrlWordsGenerator, User}
import nz.co.searchwellington.modification.TagModificationService
import nz.co.searchwellington.permissions.EditPermissionService
import nz.co.searchwellington.repositories.TagDAO
import nz.co.searchwellington.widgets.TagsWidgetFactory
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod}
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

@Controller class TagEditController @Autowired() (requestFilter: AdminRequestFilter, tagWidgetFactory: TagsWidgetFactory, urlStack: UrlStack, tagDAO: TagDAO, tagModifcationService: TagModificationService, loggedInUserFilter: LoggedInUserFilter, editPermissionService: EditPermissionService, submissionProcessingService: SubmissionProcessingService, commonModelObjectsService: CommonModelObjectsService, urlWordsGenerator: UrlWordsGenerator) {

  private val log = Logger.getLogger(classOf[TagEditController])
  private val pattern = Pattern.compile("^/edit/tag/(.*)$")

  @RequestMapping(Array("/edit/tag/*")) def edit(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val mv = new ModelAndView("editTag")
    commonModelObjectsService.populateCommonLocal(mv)
    mv.addObject("heading", "Editing a Tag")

    tagFromPage(request).map { tag =>
      mv.addObject("tag", tag)
      //var children = editTag.getChildren.asScala.toSet
      //mv.addObject("tag_select", tagWidgetFactory.createTagSelect("parent", editTag.getParent, children).toString)
      //mv.addObject("related_feed_select", tagWidgetFactory.createRelatedFeedSelect("feed", editTag.getRelatedFeed))
    }.getOrElse {
      null
    }
  }

  @RequestMapping(Array("/edit/tag/submit")) def submit(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val mv = new ModelAndView("submitTag")
    mv.addObject("heading", "Submitting a Tag")
    commonModelObjectsService.populateCommonLocal(mv)
    return mv
  }

  @RequestMapping(Array("/edit/tag/delete")) def delete(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val loggedInUser: User = loggedInUserFilter.getLoggedInUser
    if (!editPermissionService.canDeleteTags(loggedInUser)) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN)
      return null
    }
    requestFilter.loadAttributesOntoRequest(request)
    if (request.getAttribute("tag") == null) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND)
      return null
    }
    val mv: ModelAndView = new ModelAndView("deleteTag")
    mv.addObject("heading", "Editing a Tag")
    commonModelObjectsService.populateCommonLocal(mv)
    val tag: Tag = request.getAttribute("tag").asInstanceOf[Tag]
    mv.addObject("tag", tag)
    tagModifcationService.deleteTag(tag)
    urlStack.setUrlStack(request, "")
    mv
  }

  @RequestMapping(Array("/edit/tag/add")) def add(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val modelAndView = new ModelAndView("savedTag")
    modelAndView.addObject("heading", "Tag Added")
    val displayName: String = request.getParameter("displayName")
    if (displayName != null) {
      val tagUrlWords: String = urlWordsGenerator.makeUrlWordsFromName(displayName)
      if (tagDAO.loadTagByName(tagUrlWords) == null) {
        val newTag = tagDAO.createNewTag(tagUrlWords, displayName)
        log.info("Adding new tag: " + tagUrlWords)
        tagDAO.saveTag(newTag)
        modelAndView.addObject("tag", newTag)
        modelAndView
      } else {
        log.info("A tag already exists with url words: " + tagUrlWords + ". Not adding.")
        new ModelAndView(new RedirectView(urlStack.getExitUrlFromStack(request)))
      }
    } else {
      new ModelAndView(new RedirectView(urlStack.getExitUrlFromStack(request)))
    }
  }

  @RequestMapping(value = Array("/edit/tag/save"), method = Array(RequestMethod.POST)) def save(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val mv = new ModelAndView("savedTag")
    mv.addObject("heading", "Tag Saved")
    commonModelObjectsService.populateCommonLocal(mv)
    var editTag: Tag = null
    requestFilter.loadAttributesOntoRequest(request)
    if (request.getAttribute("tag") != null) {
      editTag = request.getAttribute("tag").asInstanceOf[Tag]
      log.info("Found tag " + editTag.getName + " on request.")
    }
    else {
      log.info("No tag seen on request; creating a new instance.")
      editTag = Tag(UUID.randomUUID().toString)
    }
    editTag.setName(request.getParameter("name"))
    editTag.setDisplayName(request.getParameter("displayName"))
    editTag.setDescription(request.getParameter("description"))
    val isFeatured: Boolean = request.getParameter("featured") != null
    editTag.setFeatured(isFeatured)
    //editTag.setGeocode(submissionProcessingService.processGeocode(request))
    populateRelatedTwitter(request, editTag)
    populateAutotagHints(request, editTag)
    var relatedFeed: Feed = null
    if (request.getAttribute("feedAttribute") != null) {
      relatedFeed = request.getAttribute("feedAttribute").asInstanceOf[Feed]
    }
    log.info("Setting related feed to: " + relatedFeed)
    //editTag.setRelatedFeed(relatedFeed)
    readImageFieldFromRequest(editTag, request)
    val parentTag: Tag = request.getAttribute("parent_tag").asInstanceOf[Tag]
    if (parentTag != null) {
      //val parentTagHasChanged: Boolean = parentTag ne editTag.getParent
      //if (parentTagHasChanged) {
        //val newParentIsOneOfOurChildren: Boolean = editTag.getChildren.contains(parentTag)
        //if (!newParentIsOneOfOurChildren) {
        //  tagModifcationService.updateTagParent(editTag, parentTag)
       // }
        //else {
         // log.warn("Not setting parent to one of our current children; this would be a circular reference")
       // }
      //}
    }
    else {
      log.info("Making top level tag; setting parent to null.")
      // editTag.setParent(null)
    }
    tagDAO.saveTag(editTag)
    mv.addObject("tag", editTag)
    commonModelObjectsService.populateCommonLocal(mv)
    return mv
  }

  private def populateAutotagHints(request: HttpServletRequest, editTag: Tag) {
    val autotagHints: String = request.getParameter("autotag_hints")
    if (autotagHints != null && !(autotagHints.trim == "")) {
      editTag.setAutotagHints(autotagHints)
    }
    else {
      editTag.setAutotagHints(null)
    }
  }

  private def populateRelatedTwitter(request: HttpServletRequest, editTag: Tag) {
    val requestTwitter: String = request.getParameter("twitter")
    if (requestTwitter != null && !(requestTwitter.trim == "")) {
      editTag.setRelatedTwitter(requestTwitter)
    }
    else {
      editTag.setRelatedTwitter(null)
    }
  }

  private def readImageFieldFromRequest(editTag: Tag, request: HttpServletRequest) {
    var mainImage: String = request.getParameter("main_image")
    var secondaryImage: String = request.getParameter("secondary_image")
    if (mainImage != null && mainImage.trim == "") {
      mainImage = null
    }
    if (secondaryImage != null && secondaryImage.trim == "") {
      secondaryImage = null
    }
    editTag.setMainImage(mainImage)
    editTag.setSecondaryImage(secondaryImage)
  }

  private def tagFromPage(request: HttpServletRequest): Option[Tag] = {
    val matcher = pattern.matcher(request.getPathInfo)
    if (matcher.matches) {
      val tagname = matcher.group(1)
      tagDAO.loadTagByName(tagname)
    } else {
      None
    }
  }

}
