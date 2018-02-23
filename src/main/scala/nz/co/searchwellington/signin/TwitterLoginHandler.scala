package nz.co.searchwellington.signin

import java.util
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.HibernateBackedUserDAO
import nz.co.searchwellington.twitter.TwitterApiFactory
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView
import twitter4j.Twitter
import twitter4j.TwitterException
import twitter4j.auth.AccessToken
import twitter4j.auth.RequestToken
import com.google.common.collect.Maps

@Component class TwitterLoginHandler @Autowired()(var userDAO: HibernateBackedUserDAO, var twitterApiFactory: TwitterApiFactory, val urlBuilder: UrlBuilder) extends SigninHandler {

  private val log = Logger.getLogger(classOf[TwitterLoginHandler])

  private var tokens: util.Map[String, RequestToken] = Maps.newConcurrentMap()

  @throws[Exception]
  override def getLoginView(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    if (!(twitterApiFactory.apiIsConfigured)) {
      log.warn("Twitter API is not configured - not attempting to get send user to twitter")
      return null
    }
    try {
      log.info("Getting request token")
      val twitterApi: Twitter = twitterApiFactory.getTwitterApi
      val requestToken: RequestToken = twitterApi.getOAuthRequestToken(urlBuilder.getHomeUrl + "/twitter/callback")
      log.info("Got request token: " + requestToken.getToken)
      tokens.put(requestToken.getToken, requestToken)
      val authorizeUrl: String = requestToken.getAuthenticationURL
      log.info("Redirecting user to authorize url : " + authorizeUrl)
      return new ModelAndView(new RedirectView(authorizeUrl))
    } catch {
      case e: Exception =>
        log.warn("Failed to obtain request token.", e)
    }
    return null
  }

  override def getExternalUserIdentifierFromCallbackRequest(request: HttpServletRequest): Option[Any] = {
    if (twitterApiFactory.apiIsConfigured) {
      if (request.getParameter("oauth_token") != null && request.getParameter("oauth_verifier") != null) {
        val token = request.getParameter("oauth_token")
        val verifier = request.getParameter("oauth_verifier")
        log.info("oauth_token: " + token)
        log.info("oauth_verifier: " + verifier)
        log.info("Looking for request token: " + token)

        Option(tokens.get(token)).flatMap { requestToken =>
          log.info("Found stored request token: " + requestToken)
          try {
            log.debug("Exchanging request token for access token")
            Option(twitterApiFactory.getTwitterApi.getOAuthAccessToken(requestToken, verifier)).flatMap { accessToken =>
              log.info("Got access token: '" + accessToken.getToken + "', '" + accessToken.getTokenSecret + "'")
              tokens.remove(requestToken.getToken)
              log.info("Using access token to lookup twitter user details")

              getTwitteUserCredentials(accessToken).map { twitterUser =>
                log.info("Authenticated user is: " + twitterUser.getName)
                Some(twitterUser) // TODO needs to be the return

              }.getOrElse {
                log.warn("Failed up obtain twitter user details")
                None
              }
            }

          } catch {
            case e: TwitterException =>
              log.error(e)
              log.warn("Could not get access token for: " + requestToken.getToken)
              None
          }
        }

      } else {
        log.error("oauth token or verifier missing from callback request")
        None
      }

    } else {
      log.warn("Twitter API is not configured - not attempting to get external user")
      None
    }
  }

  override def getUserByExternalIdentifier(externalIdentifier: Any): Option[User] = {
    val twitterUser: twitter4j.User = externalIdentifier.asInstanceOf[twitter4j.User]
    log.info("Looking of local user by twitter id: " + twitterUser.getId)
    userDAO.getUserByTwitterId(twitterUser.getId)
  }

  override def decorateUserWithExternalSigninIdentifier(user: User, externalIdentifier: Any): Unit = {
    val twitterUser: twitter4j.User = externalIdentifier.asInstanceOf[twitter4j.User]
    if (user.getProfilename == null || user.isUnlinkedAccount) {
      val twitterScreenName: String = twitterUser.getScreenName()
      if (userDAO.getUserByProfileName(twitterScreenName) == null) {
       // user.setProfilename(twitterScreenName) TODO
      }
    }
    // user.setTwitterId(twitterUser.getId) TODO
  }

  private def getTwitteUserCredentials(accessToken: AccessToken): Option[twitter4j.User] = {
    val twitterApi = twitterApiFactory.getOauthedTwitterApiForAccessToken(accessToken.getToken, accessToken.getTokenSecret)
    try {
      Some(twitterApi.verifyCredentials)
    } catch {
      case e: TwitterException =>
        log.warn("Failed up obtain twitter user details due to Twitter exception: " + e.getMessage)
        None
    }
  }

}