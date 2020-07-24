package nz.co.searchwellington.model

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SiteInformation() {
  @Value("${areaname}") private val areaname: String = null
  @Value("${installed}") private val url: String = null
  @Value("${twitter.username}") private val twitterUsername: String = null
  @Value("${imageroot}") private val imageRoot: String = null
  @Value("${staticroot}") private val staticRoot: String = null

  def getAreaname: String = areaname

  def getSitename: String = "Search " + this.areaname

  def getTagline: String = areaname + " in a box"

  def getUrl: String = url

  def getTwitterUsername: String = twitterUsername

  def getStaticRoot: String = staticRoot

  def getImageRoot: String = imageRoot

  def isTwitterEnabled: Boolean = twitterUsername != null && twitterUsername.nonEmpty

}
