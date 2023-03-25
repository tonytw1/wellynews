package nz.co.searchwellington.signin

import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
import nz.co.searchwellington.controllers.{AnonUserService, LoggedInUserFilter, LoginResourceOwnershipService, UrlStack}
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

import scala.concurrent.ExecutionContext.Implicits.global

@Controller class SigninController @Autowired()(loggedInUserFilter: LoggedInUserFilter, mongoRepository: MongoRepository, anonUserService: AnonUserService, loginResourceOwnershipService: LoginResourceOwnershipService, urlStack: UrlStack, signinHandler: SigninHandler) {

  private val log = LogFactory.getLog(classOf[SigninController])

  @GetMapping(Array("/twitter/login"))
  def login(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    log.info("Twitter login called")
    val loginView = signinHandler.getLoginView(request, response)
    if (loginView != null) return loginView
    signinErrorView(request)
  }

  @GetMapping(Array("/twitter/callback"))
  def callback(request: HttpServletRequest): ModelAndView = {
    signinHandler.getExternalUserIdentifierFromCallbackRequest(request).map { externalIdentifier =>
      log.info("External user identifier is: " + externalIdentifier.toString)

      val userToSignIn = signinHandler.getUserByExternalIdentifier(externalIdentifier).map { user =>
        // This was an opportunity to sync external user details everytime we signed in.
        //loggedInUserFilter.getLoggedInUser.map { loggedInUser =>
          //log.info("Attaching external identifier to current user: " + externalIdentifier)
          //signinHandler.decorateUserWithExternalSigninIdentifier(loggedInUser, externalIdentifier) // TODO why?
          //loggedInUser
        //}
        log.info(s"Found existing local user by external identifier $externalIdentifier: ${user.getId}")
        user

      }.getOrElse {
        createNewUser(externalIdentifier)
      }

      loggedInUserFilter.getLoggedInUser.foreach { alreadyLoggedInUser =>
        if (alreadyLoggedInUser.isUnlinkedAccount && !(userToSignIn == alreadyLoggedInUser)) {
          log.info("Reassigning resource ownership from unlinked user" + alreadyLoggedInUser.getProfilename + " to " + userToSignIn.getProfilename)
          loginResourceOwnershipService.reassignOwnership(alreadyLoggedInUser, userToSignIn)
        }
      }

      loggedInUserFilter.setLoggedInUser(request, userToSignIn)

      new ModelAndView(new RedirectView(urlStack.getExitUrlFromStack(request)))
    }

    signinErrorView(request)
  }

  private def signinErrorView(request: HttpServletRequest) = new ModelAndView(new RedirectView(urlStack.getExitUrlFromStack(request)))

  private def createNewUser(externalIdentifier: Any): User = {
    log.info("Creating new user with external identifier: " + externalIdentifier)
    val newUser = anonUserService.createAnonUser
    val withLinkedExternalIdentifier = signinHandler.decorateUserWithExternalSigninIdentifier(newUser, externalIdentifier)
    mongoRepository.saveUser(withLinkedExternalIdentifier)
    withLinkedExternalIdentifier
  }

}