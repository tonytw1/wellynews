package nz.co.searchwellington.controllers.proxy

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.http.WSClient
import nz.co.searchwellington.model.Newsitem
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.commons.logging.LogFactory
import org.apache.http.HttpStatus
import org.springframework.http.{CacheControl, MediaType, ResponseEntity}
import org.springframework.stereotype.Controller
import org.springframework.util.DigestUtils
import org.springframework.web.bind.annotation.{GetMapping, RequestParam}

import java.io._
import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.util.Try

@Controller
class ImageProxy(wsClient: WSClient, mongoRepository: MongoRepository, cache: FileSystemCache[(MediaType, Array[Byte])]) extends ReasonableWaits {

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

        val maybeCached = cache.get(url)
        maybeCached.map { cached =>
          log.info(s"Serving $url from cache")
          Future.successful(ResponseEntity.ok().contentType(cached._1).body(cached._2))

        }.getOrElse {
          fetchContentForOrigin(url).map {
            case Some((contentType, bytes)) =>
              val mediaType = MediaType.parseMediaType(contentType)
              cache.put(url, (mediaType, bytes))
              ResponseEntity.ok().
                contentType(mediaType).
                cacheControl(CacheControl.maxAge(24, TimeUnit.HOURS))
                .body(bytes)

            case None =>
              NotFound // TODO negative cache marker
          }
        }

      }.getOrElse {
        Future.successful(NotFound)
      }
    }

    Await.result(eventualResponse, TenSeconds)
  }

  private def fetchContentForOrigin(url: String) = {
    log.info(s"Fetching: $url from origin")
    wsClient.wsClient.url(url).withRequestTimeout(TenSeconds).get.map { r =>
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

}

class FileSystemCache[T](cacheDir: String) {

  private val log = LogFactory.getLog(classOf[FileSystemCache[T]])

  def get(key: String): Option[T] = {
    val file = new File(filenameFor(key))

    if (file.exists) {
      Try {
        val ois = new ObjectInputStream(new FileInputStream(file))
        val value = ois.readObject.asInstanceOf[T]
        ois.close()
        Some(value)

      }.toOption.flatten
    } else {
      None
    }
  }

  def put(key: String, value: T): Unit = {
    val filepath = filenameFor(key)
    log.info(s"Caching $key to $filepath")
    val oos = new ObjectOutputStream(new FileOutputStream(filepath))
    oos.writeObject(value)
    oos.close()
  }

  private def filenameFor(key: String) =
    cacheDir + "/" + DigestUtils.md5DigestAsHex(key.getBytes)
}