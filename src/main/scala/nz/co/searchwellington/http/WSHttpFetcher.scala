package nz.co.searchwellington.http

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.SiteInformation
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.net.URL
import scala.concurrent.{ExecutionContext, Future}

@Component
class WSHttpFetcher @Autowired()(siteInformation: SiteInformation, wsClient: WSClient) extends HttpFetcher with ReasonableWaits {

  private val log = LogFactory.getLog(classOf[WSHttpFetcher])

  override def httpFetch(url: URL, followRedirects: Boolean = true)(implicit ec: ExecutionContext): Future[HttpFetchResult] = {
    wsClient.wsClient.url(url.toExternalForm).withRequestTimeout(TenSeconds).withFollowRedirects(followRedirects).get.map { r =>
      log.info("Got HTTP response code " + r.status + " for url: " + url)
      HttpFetchResult(r.status, r.body)
    }
  }

  override def getUserAgent: String = siteInformation.getUserAgent

}