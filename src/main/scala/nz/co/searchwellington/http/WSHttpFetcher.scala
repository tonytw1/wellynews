package nz.co.searchwellington.http

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.SiteInformation
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.task.TaskExecutor
import org.springframework.stereotype.Component
import play.api.libs.ws.ahc._

import scala.concurrent.{ExecutionContext, Future}

@Component
class WSHttpFetcher @Autowired()(taskExecutor: TaskExecutor, siteInformation: SiteInformation) extends HttpFetcher with ReasonableWaits { // TODO seperate executor

  private val log = Logger.getLogger(classOf[WSHttpFetcher])

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  private val wsClient = StandaloneAhcWSClient()

  override def httpFetch(url: String)(implicit ec: ExecutionContext): Future[HttpFetchResult] = {
    wsClient.url(url).withRequestTimeout(TenSeconds).get.map { r =>
      log.info("Got HTTP response code " + r.status + " for url: " + url)
      HttpFetchResult(r.status, r.body)
    }
  }

  override def getUserAgent(): String = siteInformation.getUserAgent

}