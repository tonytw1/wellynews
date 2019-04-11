package nz.co.searchwellington.controllers

import java.io.{IOException, UnsupportedEncodingException}
import java.util.UUID

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import com.google.common.base.Strings
import nz.co.searchwellington.feeds.reading.WhakaokoService
import nz.co.searchwellington.feeds.{FeedItemAcceptor, FeeditemToNewsitemService, RssfeedNewsitemService}
import nz.co.searchwellington.filters.AdminRequestFilter
import nz.co.searchwellington.htmlparsing.SnapshotBodyExtractor
import nz.co.searchwellington.model._
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.modification.{ContentDeletionService, ContentUpdateService}
import nz.co.searchwellington.permissions.EditPermissionService
import nz.co.searchwellington.queues.LinkCheckerQueue
import nz.co.searchwellington.repositories.{HandTaggingDAO, ResourceFactory}
import nz.co.searchwellington.spam.SpamFilter
import nz.co.searchwellington.tagging.AutoTaggingService
import nz.co.searchwellington.widgets.{AcceptanceWidgetFactory, TagsWidgetFactory}
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod}
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem

@Controller class ResourceEditController @Autowired() (rssfeedNewsitemService: RssfeedNewsitemService, adminRequestFilter: AdminRequestFilter, tagWidgetFactory: TagsWidgetFactory, autoTagger: AutoTaggingService, acceptanceWidgetFactory: AcceptanceWidgetFactory, loggedInUserFilter: LoggedInUserFilter, editPermissionService: EditPermissionService, urlStack: UrlStack, submissionProcessingService: SubmissionProcessingService, contentUpdateService: ContentUpdateService, contentDeletionService: ContentDeletionService, snapBodyExtractor: SnapshotBodyExtractor, anonUserService: AnonUserService, tagVoteDAO: HandTaggingDAO, feedItemAcceptor: FeedItemAcceptor, resourceFactory: ResourceFactory, commonModelObjectsService: CommonModelObjectsService, feednewsItemToNewsitemService: FeeditemToNewsitemService, urlWordsGenerator: UrlWordsGenerator, whakaoroService: WhakaokoService, frontendResourceMapper: FrontendResourceMapper, spamFilter: SpamFilter, linkCheckerQueue: LinkCheckerQueue) {

  private val log = Logger.getLogger(classOf[ResourceEditController])
  private val ACCEPTANCE = "acceptance"

  @RequestMapping(Array("/edit")) def edit(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    log.info("Starting resource edit method")
    response.setCharacterEncoding("UTF-8")
    adminRequestFilter.loadAttributesOntoRequest(request)
    val resource: Resource = request.getAttribute("resource").asInstanceOf[Resource]
    if (request.getAttribute("resource") == null) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND)
      log.info("No resource attribute found on request; returning 404")
      return null
    }
    val loggedInUser: User = loggedInUserFilter.getLoggedInUser
    if (!userIsAllowedToEdit(resource, request, loggedInUser)) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN)
      log.info("No logged in user or user not allowed to edit resource; returning 403")
      return null
    }
    val mv: ModelAndView = new ModelAndView("editResource")
    commonModelObjectsService.populateCommonLocal(mv)
    mv.addObject("heading", "Editing a Resource")
    mv.addObject("resource", resource)
    mv.addObject("tag_select", tagWidgetFactory.createMultipleTagSelect(tagVoteDAO.getHandpickedTagsForThisResourceByUser(loggedInUser, resource)))
    mv.addObject("show_additional_tags", 1)
    val userIsLoggedIn: Boolean = loggedInUser != null
    populatePublisherField(mv, userIsLoggedIn, resource)
    if (resource.`type` == "F") {
      // mv.addObject("acceptance_select", acceptanceWidgetFactory.createAcceptanceSelect((resource.asInstanceOf[Feed]).getAcceptancePolicy))
    }
    return mv
  }

  @RequestMapping(Array("/edit/viewsnapshot")) def viewSnapshot(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    adminRequestFilter.loadAttributesOntoRequest(request)
    if (request.getAttribute("resource") == null) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND)
      return null
    }
    val resource: Resource = request.getAttribute("resource").asInstanceOf[Resource]
    val loggedInUser: User = loggedInUserFilter.getLoggedInUser
    if (!userIsAllowedToEdit(resource, request, loggedInUser)) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN)
      return null
    }
    val editResource: Resource = request.getAttribute("resource").asInstanceOf[Resource]
    if (request.getAttribute("resource") != null && userIsAllowedToEdit(editResource, request, loggedInUser)) {
      val mv: ModelAndView = new ModelAndView("viewSnapshot")
      commonModelObjectsService.populateCommonLocal(mv)
      mv.addObject("heading", "Resource snapshot")
      mv.addObject("resource", editResource)
      mv.addObject("body", snapBodyExtractor.extractLatestSnapshotBodyTextFor(editResource))
      mv.addObject("tag_select", tagWidgetFactory.createMultipleTagSelect(tagVoteDAO.getHandpickedTagsForThisResourceByUser(loggedInUser, editResource)))
      mv.addObject("show_additional_tags", 1)
      return mv
    }
    return new ModelAndView(new RedirectView(urlStack.getExitUrlFromStack(request)))
  }

  /*  TODO reimplement
  @RequestMapping(Array("/edit/accept"))
  @throws[IllegalArgumentException]
  @throws[IOException]
  def accept(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    response.setCharacterEncoding("UTF-8")
    adminRequestFilter.loadAttributesOntoRequest(request)
    val loggedInUser: User = loggedInUserFilter.getLoggedInUser
    if (!editPermissionService.canAcceptFeedItems(loggedInUser)) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN)
      return null
    }
    val url: String = request.getParameter("url")
    if (url == null) {
      log.warn("No feeditem url given")
      response.setStatus(HttpServletResponse.SC_NOT_FOUND)
      return null
    }
    val feed: Feed = request.getAttribute("feedAttribute").asInstanceOf[Feed]
    if (feed == null) {
      throw new RuntimeException("Could not find feed")
    }

    val maybeTuple: Option[(FeedItem, Feed)] = rssfeedNewsitemService.getFeedNewsitemByUrl(feed, url)
    maybeTuple.fold{
      log.warn("No matching newsitem found for url: " + url)
      response.setStatus(HttpServletResponse.SC_NOT_FOUND)
      return null

    }{ feeditemToAccept =>
      val acceptedNewsitem = feedItemAcceptor.acceptFeedItem(loggedInUser, feeditemToAccept)
      val modelAndView = new ModelAndView("acceptResource")
      commonModelObjectsService.populateCommonLocal(modelAndView)
      modelAndView.addObject("heading", "Accepting a submission")
      modelAndView.addObject("resource", acceptedNewsitem)
      modelAndView.addObject("publisher_select", "1")
      // modelAndView.addObject("tag_select", tagWidgetFactory.createMultipleTagSelect(Set()))
      // modelAndView.addObject("acceptedFromFeed", urlWordsGenerator.makeUrlWordsFromName(if (acceptedNewsitem.getFeed != null) acceptedNewsitem.getFeed.getName
      // else null))
      modelAndView
    }
  }
  */

  @RequestMapping(Array("/edit/submit/website")) def submitWebsite(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val modelAndView: ModelAndView = new ModelAndView("submitWebsite")
    modelAndView.addObject("heading", "Submitting a Website")
    val editResource = Website(id = UUID.randomUUID().toString)
    modelAndView.addObject("resource", editResource)
    populateSubmitCommonElements(request, modelAndView)
    modelAndView.addObject("publisher_select", null)
    return modelAndView
  }

  @RequestMapping(Array("/edit/submit/newsitem")) def submitNewsitem(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val modelAndView: ModelAndView = new ModelAndView("submitNewsitem")
    modelAndView.addObject("heading", "Submitting a Newsitem")
    val editResource = Newsitem(id = UUID.randomUUID().toString)
    modelAndView.addObject("resource", editResource)
    populateSubmitCommonElements(request, modelAndView)
    return modelAndView
  }

  @RequestMapping(Array("/edit/submit/feed")) def submitFeed(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val modelAndView: ModelAndView = new ModelAndView("submitFeed")
    modelAndView.addObject("heading", "Submitting a Feed")
    val editResource = Feed(id = UUID.randomUUID().toString)
    modelAndView.addObject("resource", editResource)
    modelAndView.addObject("acceptance_select", acceptanceWidgetFactory.createAcceptanceSelect(null))
    populateSubmitCommonElements(request, modelAndView)
    return modelAndView
  }

  @RequestMapping(Array("/edit/submit/watchlist")) def submitWatchlist(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val modelAndView: ModelAndView = new ModelAndView("submitWatchlist")
    modelAndView.addObject("heading", "Submitting a Watchlist Item")
    val editResource = Website(id = UUID.randomUUID().toString)
    modelAndView.addObject("resource", editResource)
    populateSubmitCommonElements(request, modelAndView)
    return modelAndView
  }

  @RequestMapping(Array("/delete")) def delete(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val modelAndView: ModelAndView = new ModelAndView("deletedResource")
    commonModelObjectsService.populateCommonLocal(modelAndView)
    modelAndView.addObject("heading", "Resource Deleted")
    adminRequestFilter.loadAttributesOntoRequest(request)
    var editResource: Resource = request.getAttribute("resource").asInstanceOf[Resource]
    if (editResource != null && editPermissionService.canDelete(editResource)) {
      modelAndView.addObject("resource", editResource)
      editResource = request.getAttribute("resource").asInstanceOf[Resource]
      contentDeletionService.performDelete(editResource)
      if (editResource.`type` == "F") {
        urlStack.setUrlStack(request, "")
      }
    }
    modelAndView
  }

  @RequestMapping(value = Array("/save"), method = Array(RequestMethod.POST))
  @throws[UnsupportedEncodingException]
  def save(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    request.setCharacterEncoding("UTF-8")
    response.setCharacterEncoding("UTF-8")
    val modelAndView: ModelAndView = new ModelAndView("savedResource")
    commonModelObjectsService.populateCommonLocal(modelAndView)
    modelAndView.addObject("heading", "Resource Saved")
    var loggedInUser: User = loggedInUserFilter.getLoggedInUser
    adminRequestFilter.loadAttributesOntoRequest(request)
    var editResource: Resource = null
    if (request.getAttribute("resource") != null) {
      editResource = request.getAttribute("resource").asInstanceOf[Resource]
    }
    else {
      log.info("Creating new resource.")
      if (request.getParameter("type") != null) {
        val `type`: String = request.getParameter("type")
        if (`type` == "W") {
          editResource = Website(id = UUID.randomUUID().toString)
        }
        else if (`type` == "N") {
          editResource = Newsitem(id = UUID.randomUUID().toString)
        }
        else if (`type` == "F") {
          editResource = Feed(id = UUID.randomUUID().toString)
        }
        else if (`type` == "L") {
          editResource = Watchlist(id = UUID.randomUUID().toString)
        }
        else {
          editResource = Newsitem(id = UUID.randomUUID().toString)
        }
      }
    }
    log.info("In save")
    if (editResource != null) {
      val newSubmission: Boolean = editResource.id == 0 // TODO
      if (loggedInUser == null) {
        loggedInUser = createAndSetAnonUser(request)
      }
      if (newSubmission) {
        editResource.setOwner(loggedInUser)
      }
      submissionProcessingService.processUrl(request, editResource)
      submissionProcessingService.processTitle(request, editResource)
      // editResource.setGeocode(submissionProcessingService.processGeocode(request))
      submissionProcessingService.processDate(request, editResource)
      submissionProcessingService.processHeld(request, editResource)
      submissionProcessingService.processEmbargoDate(request, editResource)
      submissionProcessingService.processDescription(request, editResource)
      submissionProcessingService.processPublisher(request, editResource)
      if (editResource.`type` == "N") {
        submissionProcessingService.processImage(request, editResource.asInstanceOf[Newsitem], loggedInUser)
        submissionProcessingService.processAcceptance(request, editResource, loggedInUser)
      }
      if (editResource.`type` == "W" || editResource.`type` == "F") {
        editResource.title.map { t =>
          editResource.setUrlWords(urlWordsGenerator.makeUrlWordsFromName(t))
        }
      }
      processFeedAcceptancePolicy(request, editResource)
      val isSpamUrl: Boolean = spamFilter.isSpam(editResource)
      val isPublicSubmission: Boolean = loggedInUser == null || (loggedInUser.isUnlinkedAccount)
      if (isPublicSubmission) {
        log.info("This is a public submission; marking as held")
        editResource.setHeld(true)
      }
      if (editResource.`type` == "F") {
        editResource.page.map { p =>
          if (!Strings.isNullOrEmpty(p)) {
            whakaoroService.createFeedSubscription(p).map { createdFeedSubscription =>
              log.info("Created whakaoko subscription: " + createdFeedSubscription)
            }
          }
        }
      }

      val okToSave = !newSubmission || !isSpamUrl || loggedInUser != null
      if (okToSave) {
        saveResource(request, loggedInUser, editResource)
        log.info("Saved resource; id is now: " + editResource.id)
        submissionProcessingService.processTags(request, editResource, loggedInUser)
        if (newSubmission) {
          log.info("Applying the auto tagger to new submission.")
          // autoTagger.autotag(editResource) TODO
        }
        saveResource(request, loggedInUser, editResource)
        linkCheckerQueue.add(editResource.id)
      }
      else {
        log.info("Could not save resource. Spam question not answered?")
      }
      modelAndView.addObject("item", frontendResourceMapper.createFrontendResourceFrom(editResource))
    }
    else {
      log.warn("No edit resource could be setup.")
    }
    return modelAndView
  }

  private def createAndSetAnonUser(request: HttpServletRequest): User = {
    log.info("Creating new anon user for resource submission")
    val loggedInUser: User = anonUserService.createAnonUser
    loggedInUserFilter.setLoggedInUser(request, loggedInUser)
    loggedInUserFilter.loadLoggedInUser(request)
    return loggedInUser
  }

  private def processFeedAcceptancePolicy(request: HttpServletRequest, editResource: Resource) {
    /*
    if (editResource.getType == "F") {
      (editResource.asInstanceOf[Feed]).setAcceptancePolicy(FeedAcceptancePolicy.IGNORE)
      if (request.getParameter(ACCEPTANCE) != null) {
        (editResource.asInstanceOf[Feed]).setAcceptancePolicy(FeedAcceptancePolicy.valueOf(request.getParameter(ACCEPTANCE)))
        log.debug("Feed acceptance policy set to: " + (editResource.asInstanceOf[Feed]).getAcceptancePolicy)
      }
    }
    */
  }

  private def saveResource(request: HttpServletRequest, loggedInUser: User, editResource: Resource) {
    contentUpdateService.update(editResource)
  }

  private def userIsAllowedToEdit(editResource: Resource, request: HttpServletRequest, loggedInUser: User): Boolean = {
    return editPermissionService.canEdit(editResource)
  }

  private def populateSubmitCommonElements(request: HttpServletRequest, modelAndView: ModelAndView) {
    commonModelObjectsService.populateCommonLocal(modelAndView)
    modelAndView.addObject("tag_select", tagWidgetFactory.createMultipleTagSelect(Set()))
    val loggedInUser: User = loggedInUserFilter.getLoggedInUser
    val userIsLoggedIn: Boolean = loggedInUser != null
    modelAndView.addObject("publisher_select", "1")
    if (userIsLoggedIn) {
      modelAndView.addObject("show_additional_tags", 1)
    }
  }

  private def populatePublisherField(modelAndView: ModelAndView, userIsLoggedIn: Boolean, editResource: Resource) {
    val isPublishedResource: Boolean = editResource.isInstanceOf[PublishedResource]
    if (isPublishedResource) {
      modelAndView.addObject("publisher_select", "1")
    }
    else {
      log.info("Edit resource is not a publisher resource.")
    }
  }
}
