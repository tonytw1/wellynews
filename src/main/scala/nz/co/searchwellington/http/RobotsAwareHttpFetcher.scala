package nz.co.searchwellington.http

import org.apache.http.HttpStatus
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component
class RobotsAwareHttpFetcher @Autowired()(robotExclusionService: RobotExclusionService, httpFetcher: WSHttpFetcher) extends HttpFetcher {

  private val log = Logger.getLogger(classOf[RobotsAwareHttpFetcher])

  private val excludedUrlPrefixes = Seq.empty

  override def httpFetch(url: String)(implicit ec: ExecutionContext): Future[HttpFetchResult] = {
    val ignoreRobotDotTxt = excludedUrlPrefixes.exists(e => url.startsWith(e))
    if (ignoreRobotDotTxt || robotExclusionService.isUrlCrawlable(url, getUserAgent)) {
      httpFetcher.httpFetch(url)

    } else {
      log.info("Url is not allowed to be crawled: " + url)
      Future.successful(HttpFetchResult(HttpStatus.SC_UNAUTHORIZED, null))
    }
  }

  override def getUserAgent: String = httpFetcher.getUserAgent

}