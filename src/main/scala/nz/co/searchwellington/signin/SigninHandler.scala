package nz.co.searchwellington.signin

import nz.co.searchwellington.model.User
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

trait SigninHandler {

  def getLoginView(request: HttpServletRequest, response: HttpServletResponse): ModelAndView
  def getExternalUserIdentifierFromCallbackRequest(request: HttpServletRequest): Option[Any]
  def getUserByExternalIdentifier(externalIdentifier: Any): Option[User]
  def decorateUserWithExternalSigninIdentifier(user: User, externalIdentifier: Any): User
}