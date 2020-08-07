package nz.co.searchwellington.signin

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.twitter.TwitterApiFactory
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView
import twitter4j.TwitterException
import twitter4j.auth.{AccessToken, RequestToken}

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Component class TwitterLoginHandler @Autowired()(mongoRepository: MongoRepository, twitterApiFactory: TwitterApiFactory,
                                                  urlBuilder: UrlBuilder) extends SigninHandler with ReasonableWaits {

  private val log = Logger.getLogger(classOf[TwitterLoginHandler])

  private val tokens = mutable.Map.empty[String, RequestToken]

  override def getLoginView(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    if (twitterApiFactory.apiIsConfigured) {
      try {
        val callbackUrl = urlBuilder.getTwitterCallbackUrl
        log.info("Getting request token with callback url: " + callbackUrl)

        val twitterApi = twitterApiFactory.getTwitterApi
        val requestToken = twitterApi.getOAuthRequestToken(callbackUrl)

        log.info("Got request token: " + requestToken.getToken)
        tokens.put(requestToken.getToken, requestToken)
        val authorizeUrl: String = requestToken.getAuthenticationURL
        log.info("Redirecting user to authorize url : " + authorizeUrl)
        new ModelAndView(new RedirectView(authorizeUrl))

      } catch {
        case e: Exception =>
          log.warn("Failed to obtain request token.", e)
          null
      }

    } else {
      log.warn("Twitter API is not configured - not attempting to get send user to twitter")
      null
    }
  }

  override def getExternalUserIdentifierFromCallbackRequest(request: HttpServletRequest): Option[twitter4j.User] = {
    if (twitterApiFactory.apiIsConfigured) {
      if (request.getParameter("oauth_token") != null && request.getParameter("oauth_verifier") != null) {
        val token = request.getParameter("oauth_token")
        val verifier = request.getParameter("oauth_verifier")
        log.info("oauth_token: " + token)
        log.info("oauth_verifier: " + verifier)
        log.info("Looking for request token: " + token)

        tokens.get(token).flatMap { requestToken =>
          log.info("Found stored request token: " + requestToken)
          try {
            log.debug("Exchanging request token for access token")
            Option(twitterApiFactory.getTwitterApi.getOAuthAccessToken(requestToken, verifier)).flatMap { accessToken =>
              log.info("Got access token: '" + accessToken.getToken + "', '" + accessToken.getTokenSecret + "'")
              tokens.remove(requestToken.getToken)
              log.info("Using access token to lookup twitter user details")

              getTwitterUserCredentials(accessToken).map { twitterUser =>
                log.info("Authenticated user is: " + twitterUser.getName)
                Some(twitterUser)
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
    val twitterUser = externalIdentifier.asInstanceOf[twitter4j.User]
    log.info("Looking of local user by twitter id: " + twitterUser.getId)
    Await.result(mongoRepository.getUserByTwitterId(twitterUser.getId), TenSeconds)
  }

  override def decorateUserWithExternalSigninIdentifier(user: User, externalIdentifier: Any): User = {
    if (user.twitterid.isEmpty) {
      externalIdentifier match {
        case twitterUser: twitter4j.User =>
          user.copy(twitterid = Some(twitterUser.getId.toInt)) // TODO persistance should be Long
        // val twitterScreenName: String = twitterUser.getScreenName()
        //if (userDAO.getUserByProfileName(twitterScreenName) == null) {
        // user.setProfilename(twitterScreenName) TODO
        //}
        case _ =>
          user
      }
    } else {
      user
    }
  }

  private def getTwitterUserCredentials(accessToken: AccessToken): Option[twitter4j.User] = {
    try {
      val twitterApi = twitterApiFactory.getOauthedTwitterApiForAccessToken(accessToken.getToken, accessToken.getTokenSecret)
      Some(twitterApi.verifyCredentials)
    } catch {
      case e: TwitterException =>
        log.warn("Failed up obtain twitter user details due to Twitter exception: " + e.getMessage)
        None
    }
  }

}