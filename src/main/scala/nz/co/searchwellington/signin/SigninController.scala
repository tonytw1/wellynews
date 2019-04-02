package nz.co.searchwellington.signin

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import nz.co.searchwellington.controllers.{AnonUserService, LoggedInUserFilter, LoginResourceOwnershipService, UrlStack}
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

@Controller class SigninController @Autowired()(loggedInUserFilter: LoggedInUserFilter, mongoRepository: MongoRepository, anonUserService: AnonUserService, loginResourceOwnershipService: LoginResourceOwnershipService, urlStack: UrlStack, signinHandler: SigninHandler) {

  private val log = Logger.getLogger(classOf[SigninController])

  @RequestMapping(Array("/twitter/login"))
  @throws[Exception]
  def login(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    log.info("Twitter login called")
    val loginView = signinHandler.getLoginView(request, response)
    if (loginView != null) return loginView
    signinErrorView(request)
  }

  @RequestMapping(Array("/twitter/callback"))
  @throws[Exception]
  def callback(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    signinHandler.getExternalUserIdentifierFromCallbackRequest(request).map { externalIdentifier =>
      log.info("External user identifier is: " + externalIdentifier.toString)

      val maybeUser = signinHandler.getUserByExternalIdentifier(externalIdentifier)
      log.info("Maybe user: " + maybeUser)

      val userToSignIn: User = maybeUser.map { user =>
        // Don't know what this does
        val loggedInUser = loggedInUserFilter.getLoggedInUser
        if (loggedInUserFilter.getLoggedInUser == null) {
          log.info("Attaching external identifier to current user: " + externalIdentifier.toString)
          //signinHandler.decorateUserWithExternalSigninIdentifier(loggedInUser, externalIdentifier) // TODO why?
          loggedInUser
        }
        user

      }.getOrElse {
        // No existing user for this identity.
        createNewUser(externalIdentifier)
      }

      val alreadyLoggedInUser = loggedInUserFilter.getLoggedInUser
      if (alreadyLoggedInUser != null) {
        if (alreadyLoggedInUser.isUnlinkedAccount && !(userToSignIn == alreadyLoggedInUser)) {
          log.info("Reassigning resource ownership from " + alreadyLoggedInUser.getProfilename + " to " + userToSignIn.getProfilename)
          loginResourceOwnershipService.reassignOwnership(alreadyLoggedInUser, userToSignIn)
        }
      }

      loggedInUserFilter.setLoggedInUser(request, userToSignIn)


      new ModelAndView(new RedirectView(urlStack.getExitUrlFromStack(request)))
    }

    signinErrorView(request)
  }

  private def signinErrorView(request: HttpServletRequest) = new ModelAndView(new RedirectView(urlStack.getExitUrlFromStack(request)))

  // TODO replace with something meaningful
  private def createNewUser(externalIdentifier: Any): User = {
    val newUser = anonUserService.createAnonUser
    signinHandler.decorateUserWithExternalSigninIdentifier(newUser, externalIdentifier)
    mongoRepository.saveUser(newUser)
    log.info("Created new user with external identifier: " + externalIdentifier.toString)
    newUser
  }

}