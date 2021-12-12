package nz.co.searchwellington.http

import java.net.URL

import org.apache.commons.logging.LogFactory
import org.springframework.stereotype.Component

@Component class RobotExclusionService() {

  private val log = LogFactory.getLog(classOf[RobotExclusionService])

  def isUrlCrawlable(url: URL, userAgent: String): Boolean = {
    true // TODO not implemented
  }
  
}