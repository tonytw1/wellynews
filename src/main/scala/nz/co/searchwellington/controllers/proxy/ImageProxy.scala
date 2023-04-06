package nz.co.searchwellington.controllers.proxy

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.http.WSClient
import nz.co.searchwellington.model.Newsitem
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.commons.logging.LogFactory
import org.apache.http.HttpStatus
import org.springframework.http.{MediaType, ResponseEntity}
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{GetMapping, RequestParam}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

@Controller
class ImageProxy(wsClient: WSClient, mongoRepository: MongoRepository) extends ReasonableWaits {

  private val log = LogFactory.getLog(classOf[ImageProxy])

  private val NotFound = ResponseEntity.status(HttpStatus.SC_NOT_FOUND).body("Not Found".getBytes)

  @GetMapping(Array("/cardimage"))
  def image(@RequestParam id: String): ResponseEntity[Array[Byte]] = {
    // Given the id of a content item, with a social image url which we are going to inline;
    // Fetch it, cache it and serve it from here

    val eventualResponse = mongoRepository.getResourceById(id).flatMap { maybeResource =>
      val maybeCardImageUrl: Option[String] = maybeResource.flatMap { resource =>
        resource match {
          case n: Newsitem => n.twitterImage
          case _ => None
        }
      }

      maybeCardImageUrl.map { url =>
        log.info("Proxying: " + url)
        fetchContentForOrigin(url).map {
          case Some((contentType, bytes)) =>
            ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(bytes)
          case None =>
            NotFound
        }

      }.getOrElse {
        Future.successful(NotFound)
      }
    }

    Await.result(eventualResponse, TenSeconds)
  }

  private def fetchContentForOrigin(url: String) = wsClient.wsClient.url(url).withRequestTimeout(TenSeconds).get.map { r =>
    log.info("Got: " + r.status)
    r.status match {
      case HttpStatus.SC_OK =>
        // Echo these to response
        val bytes: Array[Byte] = r.bodyAsBytes.toArray
        Some((r.contentType, bytes))

      case _ =>
        log.warn("Couldn't fetch: " + url)
        None
    }
  }

}
