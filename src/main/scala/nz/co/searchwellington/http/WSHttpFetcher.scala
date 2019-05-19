package nz.co.searchwellington.http

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.utils.HttpFetchResult
import org.apache.log4j.Logger
import org.springframework.core.task.TaskExecutor
import org.springframework.stereotype.Component
import play.api.libs.ws.ahc._

import scala.concurrent.{Await, ExecutionContext}

@Component
class WSHttpFetcher(feedReaderTaskExecutor: TaskExecutor) extends HttpFetcher with ReasonableWaits { // TODO seperate executor

  private val log = Logger.getLogger(classOf[WSHttpFetcher])

  implicit val executionContext = ExecutionContext.fromExecutor(feedReaderTaskExecutor)
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  private val wsClient = StandaloneAhcWSClient()

  override def httpFetch(url: String): HttpFetchResult = {
    val eventualResult = wsClient.url(url).get.map { r =>
      val result = new HttpFetchResult(r.status, r.body)
      log.info("Got HTTP fetch result from WS: " + result.getStatus)
      result
    }
    Await.result(eventualResult, OneMinute)
  }

  override def getUserAgent(): String = "TODO"

}