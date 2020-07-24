package nz.co.searchwellington.twitter

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import twitter4j.Twitter
import twitter4j.TwitterFactory
import twitter4j.auth.AccessToken
import twitter4j.conf.Configuration
import twitter4j.conf.ConfigurationBuilder
import com.google.common.base.Strings

@Component
class TwitterApiFactory {
  @Value("${consumer.key}") private val consumerKey: String = null
  @Value("${consumer.secret}") private val consumerSecret: String = null

  def getOauthedTwitterApiForAccessToken(token: String, secret: String): Twitter = new TwitterFactory(buildConfig).getInstance(new AccessToken(token, secret))

  def getTwitterApi: Twitter = new TwitterFactory(buildConfig).getInstance

  def apiIsConfigured: Boolean = !Strings.isNullOrEmpty(consumerKey) && !Strings.isNullOrEmpty(consumerSecret)

  private def buildConfig: Configuration = new ConfigurationBuilder().setOAuthConsumerKey(consumerKey).setOAuthConsumerSecret(consumerSecret).build
}
