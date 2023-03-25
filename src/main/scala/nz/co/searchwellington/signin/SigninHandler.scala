package nz.co.searchwellington.signin

import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
import nz.co.searchwellington.model.User
import org.springframework.web.servlet.ModelAndView

trait SigninHandler {

  def getLoginView(request: HttpServletRequest, response: HttpServletResponse): ModelAndView
  def getExternalUserIdentifierFromCallbackRequest(request: HttpServletRequest): Option[Any]
  def getUserByExternalIdentifier(externalIdentifier: Any): Option[User]
  def decorateUserWithExternalSigninIdentifier(user: User, externalIdentifier: Any): User
}