package nz.co.searchwellington.http

import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class RobotExclusionService() {

  private val log = Logger.getLogger(classOf[RobotExclusionService])

  def isUrlCrawlable(url: String, userAgent: String): Boolean = {
    true // TODO not implemented
  }
  
}