package nz.co.searchwellington.http

import org.apache.commons.httpclient.HttpStatus
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class RobotsAwareHttpFetcher @Autowired()(robotExclusionService: RobotExclusionService, httpFetcher: HttpFetcher) extends HttpFetcher {
  private val log = Logger.getLogger(classOf[RobotsAwareHttpFetcher])

  private val excludedUrlPrefixes = Seq.empty

  override def httpFetch(url: String): HttpFetchResult = {
    val ignoreRobotDotTxt = excludedUrlPrefixes.exists(e => url.startsWith(e))

    if (ignoreRobotDotTxt || robotExclusionService.isUrlCrawlable(url, getUserAgent)) {
      httpFetcher.httpFetch(url)
    } else {
      log.info("Url is not allowed to be crawled: " + url)
      new HttpFetchResult(HttpStatus.SC_UNAUTHORIZED, null)
    }
  }

  override def getUserAgent: String = httpFetcher.getUserAgent

}