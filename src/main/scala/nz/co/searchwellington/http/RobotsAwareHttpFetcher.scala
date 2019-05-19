package nz.co.searchwellington.http

import org.apache.commons.httpclient.HttpStatus
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class RobotsAwareHttpFetcher @Autowired()(robotExclusionService: RobotExclusionService, httpFetcher: HttpFetcher, exceptions: Array[String]) extends HttpFetcher {
  private val log = Logger.getLogger(classOf[RobotsAwareHttpFetcher])

  override def httpFetch(url: String): HttpFetchResult = {
    var overrideRobotDotTxt = false
    for (exception <- exceptions) {
      if (url.startsWith(exception)) overrideRobotDotTxt = true
    }
    if (overrideRobotDotTxt || robotExclusionService.isUrlCrawlable(url, getUserAgent)) return httpFetcher.httpFetch(url)
    log.info("Url is not allowed to be crawled: " + url)
    new HttpFetchResult(HttpStatus.SC_UNAUTHORIZED, null)
  }

  override def getUserAgent: String = httpFetcher.getUserAgent

}