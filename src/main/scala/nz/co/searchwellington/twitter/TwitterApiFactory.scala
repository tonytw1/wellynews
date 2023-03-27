package nz.co.searchwellington.twitter

import com.google.common.base.Strings
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import twitter4j.{AccessToken, OAuthAuthorization, Twitter}

@Component
class TwitterApiFactory {

  @Value("${consumer.key}") private val consumerKey: String = null
  @Value("${consumer.secret}") private val consumerSecret: String = null

  def getOauthedTwitterApiForAccessToken(accessToken: AccessToken): Twitter = Twitter.newBuilder().
    oAuthConsumer(consumerKey, consumerSecret).
    oAuthAccessToken(accessToken).build()

  def oauthAuthentication: OAuthAuthorization = OAuthAuthorization.newBuilder()
    .oAuthConsumer(consumerKey, consumerSecret).build()

  def apiIsConfigured: Boolean = !Strings.isNullOrEmpty(consumerKey) && !Strings.isNullOrEmpty(consumerSecret)

}
