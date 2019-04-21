package nz.co.searchwellington.utils

import java.io.ByteArrayInputStream

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import nz.co.searchwellington.ReasonableWaits
import org.apache.log4j.Logger
import org.springframework.stereotype.Component
import play.api.libs.ws.ahc._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits._

@Component
class WSHttpFetcher extends HttpFetcher with ReasonableWaits{

  private val log = Logger.getLogger(classOf[WSHttpFetcher])

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  private val wsClient = StandaloneAhcWSClient()

  override def httpFetch(url: String): HttpFetchResult = {
    val eventualResult = wsClient.url(url).get.map { r =>
      val is = new ByteArrayInputStream(r.body.getBytes)  // TODO just return string
      val result = new HttpFetchResult(r.status, is)
      log.info("Got HTTP fetch result from WS: " + result.getStatus)
      result
    }
    Await.result(eventualResult, OneMinute)
  }

  override def getUserAgent(): String = "TODO"

}