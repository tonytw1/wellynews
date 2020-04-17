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

import scala.concurrent.ExecutionContext.Implicits.global

@Controller class SigninController @Autowired()(loggedInUserFilter: LoggedInUserFilter, mongoRepository: MongoRepository, anonUserService: AnonUserService, loginResourceOwnershipService: LoginResourceOwnershipService, urlStack: UrlStack, signinHandler: SigninHandler) {

  private val log = Logger.getLogger(classOf[SigninController])

  @RequestMapping(Array("/twitter/login"))
  def login(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    log.info("Twitter login called")
    val loginView = signinHandler.getLoginView(request, response)
    if (loginView != null) return loginView
    signinErrorView(request)
  }

  @RequestMapping(Array("/twitter/callback"))
  def callback(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    signinHandler.getExternalUserIdentifierFromCallbackRequest(request).map { externalIdentifier =>
      log.info("External user identifier is: " + externalIdentifier.toString)

      val userToSignIn = signinHandler.getUserByExternalIdentifier(externalIdentifier).map { user =>
        // TODO Don't know what this does
        loggedInUserFilter.getLoggedInUser.map { loggedInUser =>
          log.info("Attaching external identifier to current user: " + externalIdentifier.toString)
          //signinHandler.decorateUserWithExternalSigninIdentifier(loggedInUser, externalIdentifier) // TODO why?
          loggedInUser
        }
        user

      }.getOrElse {
        createNewUser(externalIdentifier)
      }

      loggedInUserFilter.getLoggedInUser.map { alreadyLoggedInUser =>
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

  private def createNewUser(externalIdentifier: Any): User = {
    log.info("Creating new user with external identifier: " + externalIdentifier.toString)
    val newUser = anonUserService.createAnonUser
    val withLinkedExternalIdentifier = signinHandler.decorateUserWithExternalSigninIdentifier(newUser, externalIdentifier)
    mongoRepository.saveUser(withLinkedExternalIdentifier)
    withLinkedExternalIdentifier
  }

}