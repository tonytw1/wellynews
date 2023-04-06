package nz.co.searchwellington.controllers.proxy

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.http.WSClient
import org.apache.commons.logging.LogFactory
import org.apache.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{GetMapping, RequestParam}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Controller
class ImageProxy(wsClient: WSClient) extends ReasonableWaits {

  private val log = LogFactory.getLog(classOf[ImageProxy])

  @GetMapping(Array("/proxy"))
  def image(@RequestParam url: String): Unit = {
    // Given a 3rd party image url which we are going to inline;
    // Fetch it, cache it and serve it from here
    // Classic open proxy
    val self = wsClient.wsClient.url(url).withRequestTimeout(TenSeconds)
    Await.result(self.get.map { r =>
      r.status match {
        case HttpStatus.SC_OK => {
          // Echo these to response
          org.springframework.http.ResponseEntity.ok()
            // TODO headers
            .body(r.bodyAsBytes);
        }
        case _ =>
          log.warn("Couldn't fetch: " + url)
          // TODO error
          null
      }

    }, TenSeconds)

  }

}
