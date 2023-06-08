package nz.co.searchwellington.http

import java.net.URL

import org.apache.http.HttpStatus
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component
class RobotsAwareHttpFetcher @Autowired()(robotExclusionService: RobotExclusionService, httpFetcher: WSHttpFetcher) extends HttpFetcher {

  private val log = LogFactory.getLog(classOf[RobotsAwareHttpFetcher])

  private val excludedUrlPrefixes = Seq.empty

  override def httpFetch(url: URL, followRedirects: Boolean = true)(implicit ec: ExecutionContext): Future[HttpFetchResult] = {
    val ignoreRobotDotTxt = excludedUrlPrefixes.exists(e => url.toExternalForm.startsWith(e))
    if (ignoreRobotDotTxt || robotExclusionService.isUrlCrawlable(url, getUserAgent)) {
      httpFetcher.httpFetch(url, followRedirects)

    } else {
      log.info("Url is not allowed to be crawled: " + url)
      Future.successful(HttpFetchResult(HttpStatus.SC_UNAUTHORIZED, null))
    }
  }

  override def getUserAgent: String = httpFetcher.getUserAgent

}