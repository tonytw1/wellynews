package nz.co.searchwellington.model

import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Component

@Component
class SiteInformation @Autowired()(@Value("${areaname}") areaname: String = "",
                                   @Value("${installed}") url: String = "",
                                   @Value("${twitter.username}") twitterUsername: String = "",
                                   @Value("${imageroot}") imageRoot: String = "",
                                   @Value("${staticroot}") staticRoot: String = "",
                                   @Value("${sitename}") sitename: String = "") {

  def getAreaname: String = areaname

  def getSitename: String = sitename

  def getUserAgent: String = "wellynews (" + url + "/about)"

  def getTagline: String = areaname + " in a box"

  def getUrl: String = url

  def getTwitterUsername: String = twitterUsername

  def getStaticRoot: String = staticRoot

  def getImageRoot: String = imageRoot

}
