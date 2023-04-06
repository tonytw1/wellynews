package nz.co.searchwellington.controllers.proxy

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.http.WSClient
import org.apache.commons.logging.LogFactory
import org.apache.http.HttpStatus
import org.springframework.http.{MediaType, ResponseEntity}
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{GetMapping, RequestParam}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Controller
class ImageProxy(wsClient: WSClient) extends ReasonableWaits {

  private val log = LogFactory.getLog(classOf[ImageProxy])

  @GetMapping(Array("/proxy"))
  def image(@RequestParam url: String): ResponseEntity[Array[Byte]] = {
    // Given a 3rd party image url which we are going to inline;
    // Fetch it, cache it and serve it from here
    // Classic open proxy

    log.info("Proxying: " + url)
    Await.result(wsClient.wsClient.url(url).withRequestTimeout(TenSeconds).get.map { r =>
      log.info("Got: " + r.status)
      r.status match {
        case HttpStatus.SC_OK =>
          // Echo these to response
          val bytes: Array[Byte] = r.bodyAsBytes.toArray
          org.springframework.http.ResponseEntity.ok().
            contentType(MediaType.valueOf(r.contentType)).
            body(bytes)
        case _ =>
          log.warn("Couldn't fetch: " + url)
          org.springframework.http.ResponseEntity.notFound().build()
      }
    }, TenSeconds)

  }

}
