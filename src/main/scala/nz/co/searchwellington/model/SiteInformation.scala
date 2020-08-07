package nz.co.searchwellington.model

import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Component

@Component
class SiteInformation @Autowired()(@Value("${areaname}") areaname: String = "",
                                   @Value("${installed}") url: String = "",
                                   @Value("${twitter.username}") twitterUsername: String = "",
                                   @Value("${imageroot}") imageRoot: String = "",
                                   @Value("${staticroot}") staticRoot: String = "") {

  def getAreaname: String = areaname

  def getSitename: String = "Search " + this.areaname

  def getTagline: String = areaname + " in a box"

  def getUrl: String = url

  def getTwitterUsername: String = twitterUsername

  def getStaticRoot: String = staticRoot

  def getImageRoot: String = imageRoot

  def isTwitterEnabled: Boolean = twitterUsername != null && twitterUsername.nonEmpty

}
