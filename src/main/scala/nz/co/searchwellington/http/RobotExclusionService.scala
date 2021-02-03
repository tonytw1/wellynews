package nz.co.searchwellington.http

import java.net.URL

import org.apache.log4j.Logger
import org.springframework.stereotype.Component

@Component class RobotExclusionService() {

  private val log = Logger.getLogger(classOf[RobotExclusionService])

  def isUrlCrawlable(url: URL, userAgent: String): Boolean = {
    true // TODO not implemented
  }
  
}